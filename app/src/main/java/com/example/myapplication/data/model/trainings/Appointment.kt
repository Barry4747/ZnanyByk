package com.example.myapplication.data.model.trainings

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Appointment(
    var trainerId: String? = null,
    var clientId: String? = null,
    @ServerTimestamp
    var createdAt: Date? = null,
    var date: Date? = null,
    var dayOfWeek: DayOfTheWeek? = null,
    var time: String? = null,
    var duration: Int? = null,
    var title: String? = null
)

enum class DayOfTheWeek {
    MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
}
