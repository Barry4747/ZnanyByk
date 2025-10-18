package com.example.myapplication.data.model

data class Trainer (
    val trainer_id: String,
    val description: String="",
    val specialities: List<String> = emptyList(),
    val location: String,
    val ratings: List<Int> = emptyList(),
    val priceHour: Int,
    val experience: Float
)