package com.example.criminalintent

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.criminalintent.databinding.FragmentCrimeListBinding
import com.example.criminalintent.databinding.ListItemCrimeBinding
import java.util.*

class CrimeListFragment : Fragment() {

    interface Callbacks {
        fun onCrimeSelected(crimeId: UUID)
    }

    private var callbacks: Callbacks? = null

    private var _binding: FragmentCrimeListBinding? = null
    private val binding get() = _binding!!

    private val adapter: CrimeAdapter = CrimeAdapter(emptyList())

    private val crimeListViewModel: CrimeListViewModel by lazy {
        ViewModelProviders.of(this).get(CrimeListViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCrimeListBinding.inflate(inflater, container, false)

        binding.crimeRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.crimeRecyclerView.adapter = adapter

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        crimeListViewModel.crimesListLiveData.observe(
            viewLifecycleOwner,
            Observer { crimes ->
                crimes?.let {
                    Log.i(TAG, "Got crimes ${crimes.size}")
                    updateUI(crimes)
                }
            }
        )
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_crime_list, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.new_crime -> {
                val crime = Crime()
                crimeListViewModel.addCrime(crime)
                callbacks?.onCrimeSelected(crime.id)
                true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun updateUI(crimes: List<Crime>) {
        val adapter = CrimeAdapter(crimes)
        binding.crimeRecyclerView.adapter = adapter
    }

    companion object {

        private const val TAG = "CrimeListFragment"

        fun newInstance(): CrimeListFragment {
            return CrimeListFragment()
        }
    }


    private inner class CrimeHolder(val viewBinding: ListItemCrimeBinding) :
        RecyclerView.ViewHolder(viewBinding.root), View.OnClickListener {

        init {
            itemView.setOnClickListener(this)
        }

        private lateinit var crime: Crime

        fun bind(crime: Crime) {
            this.crime = crime
            viewBinding.crimeTitle.text = this.crime.title
            viewBinding.crimeDate.text = this.crime.date.toString()
            viewBinding.crimeImage.visibility = if (crime.isSolved) View.VISIBLE else View.GONE
        }

        override fun onClick(v: View?) {
            callbacks?.onCrimeSelected(crime.id)
        }

    }

    private inner class CrimeAdapter(var crimes: List<Crime>) :
        RecyclerView.Adapter<CrimeHolder>() {


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CrimeHolder {
            val itemBinding =
                ListItemCrimeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return CrimeHolder(itemBinding)
        }

        override fun onBindViewHolder(holder: CrimeHolder, position: Int) {
            val crime = crimes[position]
            holder.bind(crime)
        }

        override fun getItemCount() = crimes.size
    }
}