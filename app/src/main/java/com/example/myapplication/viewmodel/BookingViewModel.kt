package com.example.myapplication.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.trainings.Appointment
import com.example.myapplication.data.model.trainings.TrainingSlot
import com.example.myapplication.data.model.trainings.WeeklySchedule
import com.example.myapplication.data.repository.ScheduleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import javax.inject.Inject

data class BookingUiState(
    val isLoading: Boolean = false,
    val selectedDate: LocalDate = LocalDate.now(),
    val availableSlots: List<TrainingSlot> = emptyList(),
    val selectedSlot: TrainingSlot? = null,
    val trainerId: String = ""
)

@HiltViewModel
class BookingViewModel @Inject constructor(
    private val repository: ScheduleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookingUiState())
    val uiState: StateFlow<BookingUiState> = _uiState.asStateFlow()

    private val weeklyScheduleObserver = { schedule: WeeklySchedule ->
        calculateSlotsForDate(_uiState.value.selectedDate, schedule, repository.appointments.value ?: emptyList())
    }

    private val appointmentsObserver = { appointments: List<Appointment> ->
        calculateSlotsForDate(_uiState.value.selectedDate, repository.weeklySchedule.value ?: WeeklySchedule(), appointments)
    }

    fun init(trainerId: String) {
        _uiState.update { it.copy(trainerId = trainerId, isLoading = true) }

        repository.weeklySchedule.observeForever(weeklyScheduleObserver)
        repository.appointments.observeForever(appointmentsObserver)

        repository.getWeeklySchedule(trainerId)
        repository.getAppointments(trainerId)
    }

    fun onDateSelected(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date, selectedSlot = null) }
        calculateSlotsForDate(
            date,
            repository.weeklySchedule.value ?: WeeklySchedule(),
            repository.appointments.value ?: emptyList()
        )
    }

    fun onSlotSelected(slot: TrainingSlot) {
        _uiState.update { it.copy(selectedSlot = slot) }
    }

    private fun calculateSlotsForDate(
        date: LocalDate,
        schedule: WeeklySchedule,
        appointments: List<Appointment>
    ) {
        val dayOfWeek = date.dayOfWeek

        val rawSlots = when (dayOfWeek) {
            java.time.DayOfWeek.MONDAY -> schedule.monday
            java.time.DayOfWeek.TUESDAY -> schedule.tuesday
            java.time.DayOfWeek.WEDNESDAY -> schedule.wednesday
            java.time.DayOfWeek.THURSDAY -> schedule.thursday
            java.time.DayOfWeek.FRIDAY -> schedule.friday
            java.time.DayOfWeek.SATURDAY -> schedule.saturday
            java.time.DayOfWeek.SUNDAY -> schedule.sunday
            else -> emptyList()
        } ?: emptyList()

        val takenTimes = appointments.filter { appointment ->
            val appointmentDate = appointment.date?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate()
            appointmentDate == date
        }.mapNotNull { it.time }

        val nowTime = java.time.LocalTime.now()
        val isToday = date == LocalDate.now()

        val availableSlots = rawSlots.filter { slot ->
            val isTaken = takenTimes.contains(slot.time)

            val slotTime = try {
                java.time.LocalTime.parse(slot.time, DateTimeFormatter.ofPattern("HH:mm"))
            } catch (e: Exception) { null }

            val isPast = if (isToday && slotTime != null) slotTime.isBefore(nowTime) else false

            !isTaken && !isPast
        }

        _uiState.update {
            it.copy(availableSlots = availableSlots, isLoading = false)
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        repository.weeklySchedule.removeObserver(weeklyScheduleObserver)
        repository.appointments.removeObserver(appointmentsObserver)
    }
}