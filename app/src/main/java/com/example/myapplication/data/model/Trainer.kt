package com.example.myapplication.data.model

data class Trainer(
    val email: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val phoneNumber: String? = null,
    val description: String? = null,
    val specialities: List<String>? = null,
    val location: String? = null,
    val ratings: List<Int>? = null,
    var avgRating: String? = null,
    val pricePerHour: Int? = null,
    val experience: Int? = null,
)