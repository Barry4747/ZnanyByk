package com.example.myapplication.utils

import com.example.myapplication.data.model.chats.Message
import java.text.SimpleDateFormat
import java.util.*




fun formatTimestamp(current: Long, previous: Long?): String {
    previous ?: return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(current))

    val diff = kotlin.math.abs(current - previous)
    return if (diff >= 24 * 60 * 60 * 1000) {
        SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(current))
    } else {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(current))
    }
}

fun shouldShowTimestamp(currentTimestamp: Long, previousTimestamp: Long?): Boolean {
    if (previousTimestamp == null) return true

    val diff = currentTimestamp - previousTimestamp
    val tenMinutesInMillis = 10 * 60 * 1000

    return diff > tenMinutesInMillis
}


val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
val dateFormat = SimpleDateFormat("dd MMM HH:mm", Locale.getDefault())

fun getSmartTimestamp(currentTimestamp: Long, previousTimestamp: Long?): String {
    if (previousTimestamp == null) {
        return dateFormat.format(Date(currentTimestamp))
    }

    if (!isSameDay(currentTimestamp, previousTimestamp)) {
        return dateFormat.format(Date(currentTimestamp))
    }

    val diff = currentTimestamp - previousTimestamp
    val tenMinutesInMillis = 10 * 60 * 1000

    if (diff > tenMinutesInMillis) {
        return timeFormat.format(Date(currentTimestamp))
    }

    return ""
}

fun isSameDay(time1: Long, time2: Long): Boolean {
    val cal1 = Calendar.getInstance().apply { timeInMillis = time1 }
    val cal2 = Calendar.getInstance().apply { timeInMillis = time2 }

    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

fun shouldShowProfile(
    index: Int,
    messages: List<Message>,
    currentUserId: String?
): Boolean {
    val currentMessage = messages[index]

    if (currentMessage.senderId == currentUserId) return false

    val newerMessage = messages.getOrNull(index - 1)

    if (newerMessage == null) return true

    return newerMessage.senderId != currentMessage.senderId
}