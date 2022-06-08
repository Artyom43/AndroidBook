package com.example.petdagger

fun main() {
    val appComponent: AppComponent = DaggerAppComponent.create()
    print(appComponent.computer)
}