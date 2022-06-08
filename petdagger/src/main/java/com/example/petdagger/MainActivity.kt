package com.example.petdagger

import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import javax.inject.Inject

class MainApp : Application() {

    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()

        appComponent = DaggerAppComponent.create()
    }

}

val Context.appComponent: AppComponent
    get() = when (this) {
        is MainApp -> appComponent
        else -> this.applicationContext.appComponent
    }


class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var computer: Computer


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        appComponent.inject(this)
//        val computer = appComponent.computer -- вместо этого @Inject
    }
}