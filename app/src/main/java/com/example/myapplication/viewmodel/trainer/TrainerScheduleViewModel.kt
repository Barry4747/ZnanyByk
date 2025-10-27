package com.example.myapplication.viewmodel.trainer

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.myapplication.data.model.trainings.Appointment
import com.example.myapplication.data.model.trainings.TrainingSlot
import com.example.myapplication.data.model.trainings.WeeklySchedule
import com.example.myapplication.data.repository.AuthRepository
import com.example.myapplication.data.repository.ScheduleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val repository: ScheduleRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val currentUserId = authRepository.getCurrentUserId()

    val weeklySchedule: LiveData<WeeklySchedule> = repository.weeklySchedule
    val appointments: LiveData<List<Appointment>> = repository.appointments

    fun loadSchedule() {
        repository.getWeeklySchedule(currentUserId.toString())
    }

    fun updateSchedule(schedule: WeeklySchedule) {
        repository.setWeeklySchedule(currentUserId, schedule)
    }

    fun addNewSlot(day: String, slot: TrainingSlot) {
        val currentSchedule = weeklySchedule.value ?: WeeklySchedule()

        val updatedSchedule = when (day.lowercase()) {
            "monday" -> currentSchedule.copy(
                monday = (currentSchedule.monday.orEmpty() + slot).sortedByTime()
            )
            "tuesday" -> currentSchedule.copy(
                tuesday = (currentSchedule.tuesday.orEmpty() + slot).sortedByTime()
            )
            "wednesday" -> currentSchedule.copy(
                wednesday = (currentSchedule.wednesday.orEmpty() + slot).sortedByTime()
            )
            "thursday" -> currentSchedule.copy(
                thursday = (currentSchedule.thursday.orEmpty() + slot).sortedByTime()
            )
            "friday" -> currentSchedule.copy(
                friday = (currentSchedule.friday.orEmpty() + slot).sortedByTime()
            )
            "saturday" -> currentSchedule.copy(
                saturday = (currentSchedule.saturday.orEmpty() + slot).sortedByTime()
            )
            "sunday" -> currentSchedule.copy(
                sunday = (currentSchedule.sunday.orEmpty() + slot).sortedByTime()
            )
            else -> currentSchedule
        }

        repository.setWeeklySchedule(currentUserId, updatedSchedule)
    }

    fun removeSlot(day: String, slot: TrainingSlot) {
        val currentSchedule = weeklySchedule.value ?: WeeklySchedule()

        val updatedSchedule = when (day.lowercase()) {
            "monday" -> currentSchedule.copy(
                monday = currentSchedule.monday.orEmpty().filterNot { it == slot }.sortedByTime()
            )
            "tuesday" -> currentSchedule.copy(
                tuesday = currentSchedule.tuesday.orEmpty().filterNot { it == slot }.sortedByTime()
            )
            "wednesday" -> currentSchedule.copy(
                wednesday = currentSchedule.wednesday.orEmpty().filterNot { it == slot }.sortedByTime()
            )
            "thursday" -> currentSchedule.copy(
                thursday = currentSchedule.thursday.orEmpty().filterNot { it == slot }.sortedByTime()
            )
            "friday" -> currentSchedule.copy(
                friday = currentSchedule.friday.orEmpty().filterNot { it == slot }.sortedByTime()
            )
            "saturday" -> currentSchedule.copy(
                saturday = currentSchedule.saturday.orEmpty().filterNot { it == slot }.sortedByTime()
            )
            "sunday" -> currentSchedule.copy(
                sunday = currentSchedule.sunday.orEmpty().filterNot { it == slot }.sortedByTime()
            )
            else -> currentSchedule
        }

        repository.setWeeklySchedule(currentUserId, updatedSchedule)
    }


    fun loadAppointments() {
        repository.getAppointments(currentUserId.toString())
    }

    fun addNewAppointment(appointment: Appointment) {
        repository.addAppointment(appointment)
    }

    fun loadAppointmentsForDay(dayOfWeek: String) {
        repository.getAppointmentsForDay(currentUserId.toString(), dayOfWeek)
    }

    private fun List<TrainingSlot>.sortedByTime(): List<TrainingSlot> {
        return this.sortedBy {
            val parts = it.time?.split(":")?.mapNotNull { part -> part.toIntOrNull() }
            val hour = parts?.getOrNull(0) ?: 0
            val minute = parts?.getOrNull(1) ?: 0
            hour * 60 + minute
        }
    }
}
