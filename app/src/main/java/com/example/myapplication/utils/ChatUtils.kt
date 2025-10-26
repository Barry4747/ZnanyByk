package com.example.myapplication.utils

import com.example.myapplication.data.model.Message
import java.text.SimpleDateFormat
import java.util.*


fun shouldShowProfile(index: Int, messages: List<Message>, currentUserId: String?): Boolean {
    val message = messages[index]
    val nextMessage = messages.getOrNull(index - 1)
    val isCurrentUser = message.senderId == currentUserId
    return !isCurrentUser && (nextMessage == null || nextMessage.senderId != message.senderId)
}


fun formatTimestamp(current: Long, previous: Long?): String {
    previous ?: return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(current))

    val diff = kotlin.math.abs(current - previous)
    return if (diff >= 24 * 60 * 60 * 1000) {
        SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(current))
    } else {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(current))
    }
}

fun shouldShowTimestamp(current: Long, previous: Long?): Boolean {
    previous ?: return true
    val diff = kotlin.math.abs(current - previous)
    return diff >= 15 * 60 * 1000
}