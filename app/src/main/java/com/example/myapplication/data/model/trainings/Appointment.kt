package com.example.myapplication.data.model.trainings

data class Appointment(
    var trainerId: String? = null,
    var clientName: String? = null,
    var dayOfWeek: DayOfTheWeek? = null,
    var time: String? = null,
    var duration: Int? = null,
    var title: String? = null
)

enum class DayOfTheWeek() {
    MONDAY,
    TUESDAY,
    WEDNESDAY,
    THURSDAY,
    FRIDAY,
    SATURDAY,
    SUNDAY,
}