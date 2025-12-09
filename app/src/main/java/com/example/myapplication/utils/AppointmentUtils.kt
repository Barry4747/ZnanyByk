package com.example.myapplication.utils

import com.example.myapplication.data.model.trainings.Appointment
import java.util.Calendar
import java.util.Date

fun calculateAppointmentStatus(appointment: Appointment): Pair<Boolean, Boolean> {
    if (appointment.date == null) return Pair(false, false)

    val now = Calendar.getInstance()

    val startCalendar = Calendar.getInstance()
    startCalendar.time = appointment.date!!

    val timeParts = appointment.time?.split(":")
    val hour = timeParts?.getOrNull(0)?.toIntOrNull() ?: 0
    val minute = timeParts?.getOrNull(1)?.toIntOrNull() ?: 0

    startCalendar.set(Calendar.HOUR_OF_DAY, hour)
    startCalendar.set(Calendar.MINUTE, minute)
    startCalendar.set(Calendar.SECOND, 0)
    startCalendar.set(Calendar.MILLISECOND, 0)

    val endCalendar = startCalendar.clone() as Calendar
    endCalendar.add(Calendar.MINUTE, appointment.duration ?: 0)

    val isPast = now.after(endCalendar)

    val isToday = now.get(Calendar.YEAR) == startCalendar.get(Calendar.YEAR) &&
            now.get(Calendar.DAY_OF_YEAR) == startCalendar.get(Calendar.DAY_OF_YEAR)

    return Pair(isPast, isToday)
}