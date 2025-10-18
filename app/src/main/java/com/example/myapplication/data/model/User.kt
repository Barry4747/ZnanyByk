package com.example.myapplication.data.model

import java.sql.Date

data class User (
    val u_id: String,
    val first_name: String,
    val last_name: String,
    val email: String,
    val password: String,
    val role: Role,
    val birthDate: Date,
    val phone_number: String,
    val avatarUrl: String? = null
)


enum class Role {
    TRAINER,
    CLIENT,
    ADMIN
}