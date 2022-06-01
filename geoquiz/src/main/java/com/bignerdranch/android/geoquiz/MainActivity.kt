package com.bignerdranch.android.geoquiz

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.bignerdranch.android.geoquiz.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val quizViewModel by lazy {
        ViewModelProviders.of(this).get(QuizViewModel::class.java)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate called")
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        val currentIndex = savedInstanceState?.getInt(KEY_INDEX) ?: 0
//        quizViewModel.currentIndex = currentIndex

        updateQuestionText()

        binding.trueButton.setOnClickListener {
            checkAnswer(true)
            disableButtons()
        }

        binding.falseButton.setOnClickListener {
            checkAnswer(false)
            disableButtons()
        }

        binding.backButton.setOnClickListener {
            quizViewModel.moveToPrevious()
            updateQuestionText()
        }

        binding.cheatButton.setOnClickListener {
            val answerIsTrue = quizViewModel.currentQuestionAnswer
            val intent = CheatActivity.newIntent(this@MainActivity, answerIsTrue)
            startActivityForResult(intent, REQUEST_CODE_CHEAT)
        }

        binding.nextButton.setOnClickListener(showNextAnswer)

        binding.questionTextView.setOnClickListener(showNextAnswer)
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        Log.i(TAG, "onSaveInstanceState")
        outState.putInt(KEY_INDEX, quizViewModel.currentIndex)
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart called")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume called")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause called")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop called")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy called")
    }

    private val showNextAnswer = { _: View ->
        quizViewModel.moveToNext()
        enableButtons()
        updateQuestionText()
    }

    private fun disableButtons() {
        binding.trueButton.isEnabled = false
        binding.falseButton.isEnabled = false
    }

    private fun enableButtons() {
        binding.trueButton.isEnabled = true
        binding.falseButton.isEnabled = true
    }

    private fun checkAnswer(userAnswer: Boolean) {
        val correctAnswer = quizViewModel.currentQuestionAnswer

        val messageResId = when {
            quizViewModel.isCheater -> R.string.judgement_toast
            userAnswer == correctAnswer -> R.string.correct_toast
            else -> R.string.incorrect_toast
        }

        showToast(messageResId)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK) {
            return
        }

        if (requestCode == REQUEST_CODE_CHEAT) {
            quizViewModel.isCheater =
                data?.getBooleanExtra(CheatActivity.EXTRA_ANSWER_SHOWN, false) ?: false
        }
    }

    private fun updateQuestionText() {
        val questionTextResId = quizViewModel.currentQuestionText
        binding.questionTextView.setText(questionTextResId)
    }

    private fun showToast(resId: Int) {
        val toast = Toast.makeText(this, resId, Toast.LENGTH_SHORT)
        toast.setGravity(Gravity.TOP, 0, 0)
        toast.show()
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val KEY_INDEX = "index"
        private const val REQUEST_CODE_CHEAT = 0
    }
}