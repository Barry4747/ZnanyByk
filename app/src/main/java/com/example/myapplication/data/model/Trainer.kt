package com.example.myapplication.data.model

data class Trainer(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val specialities: List<String> = emptyList(),
    val location: String = "",
    val ratings: List<Int> = emptyList(),
    var avgRating: String = "",
    val pricePerHour: Int = 0,
    val experience: Int = 0,
    val email: String = ""
) {

    constructor() : this("", "", "", emptyList(), "", emptyList(), "0.00",0, 0, "")
}