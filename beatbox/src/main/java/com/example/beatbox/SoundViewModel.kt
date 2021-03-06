package com.example.beatbox

import BeatBox
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.lifecycle.MutableLiveData

class SoundViewModel(private val beatBox: BeatBox)
//    : BaseObservable()
{
    val title: MutableLiveData<String?> = MutableLiveData()

    fun onButtonClicked() {
        sound?.let {
            beatBox.play(it)
        }
    }

    var sound: Sound? = null
        set(sound) {
            field = sound
//            notifyChange()
            title.postValue(sound?.name)
        }

//    @get: Bindable
//    val title: String?
//        get() = sound?.name
}