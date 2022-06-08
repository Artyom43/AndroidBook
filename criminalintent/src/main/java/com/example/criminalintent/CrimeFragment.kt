package com.example.criminalintent

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.example.criminalintent.databinding.FragmentCrimeBinding
import java.io.File
import java.util.*

class CrimeFragment : Fragment(), DatePickerFragment.Callbacks {

    private lateinit var crime: Crime
    private lateinit var photoFile: File
    private lateinit var photoUri: Uri

    private var _binding: FragmentCrimeBinding? = null
    private val binding get() = _binding!!

    private val crimeDetailViewModel: CrimeDetailViewModel by lazy {
        ViewModelProviders.of(this).get(CrimeDetailViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        crime = Crime()
        val crimeId = arguments?.getSerializable(ARG_CRIME_ID) as UUID
        crimeDetailViewModel.loadCrime(crimeId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCrimeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        crimeDetailViewModel.crimeLiveData.observe(
            viewLifecycleOwner
        ) { crime ->
            crime?.let {
                this.crime = crime
                photoFile = crimeDetailViewModel.getPhotoFile(crime)
                photoUri = FileProvider.getUriForFile(
                    requireActivity(),
                    "com.bignerdranch.android.fileprovider",
                    photoFile
                )
                updateUI()
            }
        }
    }

    override fun onStart() {
        super.onStart()

        val titleWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) =
                Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                crime.title = s.toString()
            }

            override fun afterTextChanged(s: Editable?) = Unit
        }

        binding.crimeDate.setOnClickListener {
            DatePickerFragment.newInstance(crime.date).apply {
                setTargetFragment(this@CrimeFragment, REQUEST_DATE)
                show(this@CrimeFragment.requireFragmentManager(), DIALOG_DATE)
            }
        }

        binding.crimeSuspect.apply {
            val pickContactIntent =
                Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
            val packageManager = requireActivity().packageManager

            setOnClickListener {
                startActivityForResult(pickContactIntent, REQUEST_CONTACT)
            }

            val resolvedActivity =
                packageManager.resolveActivity(pickContactIntent, PackageManager.MATCH_DEFAULT_ONLY)
            if (resolvedActivity == null) {
                isEnabled = false
            }
        }

        binding.crimeReport.setOnClickListener {
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, getCrimeReport())
                putExtra(
                    Intent.EXTRA_SUBJECT,
                    getString(R.string.crime_report_subject)
                ).also {
                    val chooserIntent =
                        Intent.createChooser(it, getString(R.string.send_report))
                    startActivity(chooserIntent)
                }
            }
        }

        binding.crimeCamera.apply {
            val packageManager = requireActivity().packageManager

            val captureImage = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val resolvedActivity = packageManager.resolveActivity(
                captureImage, PackageManager.MATCH_DEFAULT_ONLY
            )
            if (resolvedActivity == null) {
                isEnabled = false
            }

            setOnClickListener {
                captureImage.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)

                val cameraActivities = packageManager.queryIntentActivities(
                    captureImage, PackageManager.MATCH_DEFAULT_ONLY
                )

                for (cameraActivity in cameraActivities) {
                    requireActivity().grantUriPermission(
                        cameraActivity.activityInfo.packageName,
                        photoUri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                }
                startActivityForResult(captureImage, REQUEST_PHOTO)
            }
        }

        binding.crimeSolved.apply {
            setOnCheckedChangeListener { _, isChecked ->
                crime.isSolved = isChecked
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when {
            resultCode != Activity.RESULT_OK -> return

            requestCode == REQUEST_CONTACT && data != null -> {
                val contactUri = data.data
                val queryFields = arrayOf(ContactsContract.Contacts.DISPLAY_NAME)
                val cursor = requireActivity().contentResolver
                    .query(contactUri ?: Uri.EMPTY, queryFields, null, null, null)
                cursor?.use {
                    if (it.count == 0) {
                        return
                    }

                    it.moveToFirst()
                    val suspect = it.getString(0)
                    crime.suspect = suspect
                    crimeDetailViewModel.saveCrime(crime)
                    binding.crimeSuspect.text = suspect
                }
            }

            requestCode == REQUEST_PHOTO -> {
                requireActivity().revokeUriPermission(
                    photoUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
                updatePhotoView()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        crimeDetailViewModel.saveCrime(crime)
    }

    override fun onDateSelected(date: Date) {
        crime.date = date
        updateUI()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onDetach() {
        super.onDetach()
        requireActivity().revokeUriPermission(
            photoUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )
    }

    private fun updateUI() {
        binding.crimeTitle.setText(crime.title)
        binding.crimeDate.text = crime.date.toString()
        if (crime.suspect.isNotEmpty()) {
            binding.crimeSuspect.text = crime.suspect
        }
        updatePhotoView()
    }

    private fun getCrimeReport(): String {
        val solvedString = if (crime.isSolved) {
            getString(R.string.crime_report_solved)
        } else {
            getString(R.string.crime_report_unsolved)
        }

        val dateString = DateFormat.format(DATE_FORMAT, crime.date).toString()
        var suspect = if (crime.suspect.isBlank()) {
            getString(R.string.crime_report_suspect)
        } else {
            getString(R.string.crime_report_no_suspect)
        }

        return getString(R.string.crime_report, crime.title, dateString, solvedString, suspect)
    }

    private fun updatePhotoView() {
        if (photoFile.exists()) {
            val bitmap = getScaledBitmap(photoFile.path, requireActivity())
            binding.crimePhoto.setImageBitmap(bitmap)
            binding.crimePhoto.contentDescription =
                getString(R.string.crime_photo_image_description)
        } else {
            binding.crimePhoto.setImageDrawable(null)
            binding.crimePhoto.contentDescription =
                getString(R.string.crime_photo_no_image_description)
        }
    }

    companion object {

        private const val ARG_CRIME_ID = "crime_id"
        private const val TAG = "CrimeFragment"
        private const val DIALOG_DATE = "DialogDate"
        private const val REQUEST_DATE = 0
        private const val DATE_FORMAT = "EEE, MMM, dd"
        private const val REQUEST_CONTACT = 1
        private const val REQUEST_PHOTO = 2

        fun newInstance(crimeId: UUID): CrimeFragment {
            val args = Bundle().apply {
                putSerializable(ARG_CRIME_ID, crimeId)
            }
            return CrimeFragment().apply {
                arguments = args
            }
        }
    }
}