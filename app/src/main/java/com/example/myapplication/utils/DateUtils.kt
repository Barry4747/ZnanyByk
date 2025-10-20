package com.example.myapplication.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

data class DateParseResult(
    val date: Date?,
    val error: String?
)

fun parseBirthDate(text: String): DateParseResult {
    if (text.isBlank()) return DateParseResult(null, null)

    return try {
        dateFormatter.isLenient = false
        val date = dateFormatter.parse(text)

        if (date != null && date.after(Date())) {
            DateParseResult(null, "Data nie może być w przyszłości")
        } else {
            DateParseResult(date, null)
        }
    } catch (e: Exception) {
        DateParseResult(null, "Nieprawidłowy format (dd/MM/yyyy)")
    }
}