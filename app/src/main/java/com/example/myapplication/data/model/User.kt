package com.example.myapplication.data.model

import com.google.firebase.Timestamp

data class User(
    val email: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val role: Role = Role.CLIENT,
    val birthDate: Timestamp? = null,
    val phoneNumber: String? = null,
)

enum class Role {
    TRAINER,
    CLIENT
}
