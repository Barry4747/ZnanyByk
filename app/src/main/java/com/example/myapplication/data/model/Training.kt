package com.example.myapplication.data.model

import java.util.Date


data class Training (
    val trainingsID: String = "",
    val date: Date? = null,
    val price: Double? = null,
    val clientId: String = "",
    val trainerId: String = "",
    val location: String = "",
    val paymentId: String? = null
)