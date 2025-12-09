package com.example.myapplication.data.model.users

data class UserLocation(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val fullAddress: String = "",
    val city: String? = null,
    val postalCode: String? = null,
    val country: String? = null
)

