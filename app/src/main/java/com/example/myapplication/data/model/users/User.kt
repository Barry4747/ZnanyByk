package com.example.myapplication.data.model.users

import java.util.Date

data class User(
    val email: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val role: Role = Role.CLIENT,
    val birthDate: Date? = null,
    val phoneNumber: String? = null,
)

enum class Role {
    TRAINER,
    CLIENT
}
