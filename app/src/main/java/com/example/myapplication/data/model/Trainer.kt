package com.example.myapplication.data.model

data class Trainer(
    val id: String = "",
    val name: String = "", 
    val description: String = "",
    val specialities: List<String> = emptyList(),
    val location: String = "",
    val ratings: List<Int> = emptyList(),
    val pricePerHour: Int = 0,
    val experience: Float = 0f,
    val email: String = ""
) {

    constructor() : this("", "", "", emptyList(), "", emptyList(), 0, 0f, "")
}