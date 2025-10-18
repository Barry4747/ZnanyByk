package com.example.myapplication.data.model

import java.util.Date

data class User (
    val u_id: String = "",
    val first_name: String = "",
    val last_name: String = "",
    val email: String? = null,
    val password: String? = null,
    val role: Role? = null,
    val birthDate: Date? = null,
    val phone_number: String? = null,
    val avatarUrl: String? = null
)


enum class Role {
    TRAINER,
    CLIENT,
}