package com.bignerdranch.android.geoquiz

import android.util.Log
import androidx.lifecycle.ViewModel

class QuizViewModel : ViewModel() {

    init {
        Log.d(TAG, "ViewModel instance created")
    }

    var currentIndex = 0
    var isCheater = false

    private val questionBank = listOf(
        Question(R.string.question_australia, true),
        Question(R.string.question_oceans, true),
        Question(R.string.question_mideast, false),
        Question(R.string.question_africa, false),
        Question(R.string.question_americas, true),
        Question(R.string.question_asia, true),
    )

    val currentQuestionAnswer: Boolean
        get() = questionBank[currentIndex].answer

    val currentQuestionText: Int
        get() = questionBank[currentIndex].textResId

    fun moveToNext() {
        currentIndex = (currentIndex + 1) % questionBank.size
    }

    fun moveToPrevious() {
        currentIndex =
            if (currentIndex != 0) (currentIndex - 1) % questionBank.size else questionBank.size - 1
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "ViewModel instace about to be destroyed")
    }

    companion object {
        private const val TAG = "QuizViewModel"
    }
}