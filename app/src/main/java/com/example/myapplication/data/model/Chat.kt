package com.example.myapplication.data.model

data class Chat(
    val id: String = "",
    val users: List<String> = emptyList(),
    val lastMessage: String = "",
    val lastTimestamp: Long = 0L
)
