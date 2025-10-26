package com.example.myapplication.viewmodel.trainer

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.myapplication.data.model.trainings.Appointment
import com.example.myapplication.data.model.trainings.WeeklySchedule
import com.example.yourapp.repository.ScheduleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val repository: ScheduleRepository
): ViewModel() {

    val weeklySchedule: LiveData<WeeklySchedule> = repository.weeklySchedule
    val appointments: LiveData<List<Appointment>> = repository.appointments

    fun loadSchedule(trainerId: String) {
        repository.getWeeklySchedule(trainerId)
    }

    fun updateSchedule(trainerId: String, schedule: WeeklySchedule) {
        repository.setWeeklySchedule(trainerId, schedule)
    }

    fun loadAppointments(trainerId: String) {
        repository.getAppointments(trainerId)
    }

    fun addNewAppointment(appointment: Appointment) {
        repository.addAppointment(appointment)
    }

    fun loadAppointmentsForDay(trainerId: String, dayOfWeek: String) {
        repository.getAppointmentsForDay(trainerId, dayOfWeek)
    }
}