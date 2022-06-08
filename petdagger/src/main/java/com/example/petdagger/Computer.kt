package com.example.petdagger

class Processor {

    override fun toString(): String = "AB2021"
}

class Motherboard {

    override fun toString(): String = "X7 3000"
}

class RAM {

    override fun toString(): String = "16 GB"
}

data class Computer(
    val processor: Processor,
    val motherboard: Motherboard,
    val ram: RAM,
)