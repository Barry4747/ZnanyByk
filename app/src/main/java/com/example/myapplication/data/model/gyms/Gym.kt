package com.example.myapplication.data.model.gyms

import com.google.firebase.firestore.DocumentId

data class Gym(
    @DocumentId val id: String = "",
    val gymName: String = "",
    val gymLocation: GymLocation = GymLocation()
)

data class GymLocation(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val formattedAddress: String = "",
    val shortFormattedAddress: String? = "",
)
