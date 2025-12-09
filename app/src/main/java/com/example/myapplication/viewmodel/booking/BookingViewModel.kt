package com.example.myapplication.viewmodel.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.trainings.Appointment
import com.example.myapplication.data.model.trainings.TrainingSlot
import com.example.myapplication.data.model.trainings.WeeklySchedule
import com.example.myapplication.data.repository.ScheduleRepository
import com.example.myapplication.data.repository.TrainerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class BookingUiState(
    val isLoading: Boolean = false,
    val selectedDate: LocalDate = LocalDate.now(),
    val availableSlots: List<TrainingSlot> = emptyList(),
    val selectedSlot: TrainingSlot? = null,
    val trainerId: String = "",
    val schedule: WeeklySchedule = WeeklySchedule(),
    val appointments: List<Appointment> = emptyList(),
    val categories: List<String> = emptyList(),
    val selectedCategory: String? = null
)

@HiltViewModel
class BookingViewModel @Inject constructor(
    private val repository: ScheduleRepository,
    private val trainerRepository: TrainerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookingUiState())
    val uiState: StateFlow<BookingUiState> = _uiState.asStateFlow()

    private val weeklyScheduleObserver = { schedule: WeeklySchedule ->
        _uiState.update {
            it.copy(schedule = schedule)
        }
        calculateSlotsForDate(_uiState.value.selectedDate, schedule, repository.appointments.value ?: emptyList())
    }

    private val appointmentsObserver = { appointments: List<Appointment> ->
        _uiState.update {
            it.copy(appointments = appointments)
        }
        calculateSlotsForDate(_uiState.value.selectedDate, repository.weeklySchedule.value ?: WeeklySchedule(), appointments)
    }

    fun init(trainerId: String) {
        _uiState.update { it.copy(trainerId = trainerId, isLoading = true) }

        repository.weeklySchedule.observeForever(weeklyScheduleObserver)
        repository.appointments.observeForever(appointmentsObserver)

        repository.getWeeklySchedule(trainerId)
        repository.getAppointments(trainerId)

        viewModelScope.launch {
            try {
                val trainer = trainerRepository.getTrainerById(trainerId)
                _uiState.update {
                    it.copy(
                        categories = trainer.getOrNull()?.categories ?: emptyList(),
                        selectedCategory = trainer.getOrNull()?.categories?.firstOrNull()
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun onDateSelected(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date, selectedSlot = null) }
        calculateSlotsForDate(
            date,
            repository.weeklySchedule.value ?: WeeklySchedule(),
            repository.appointments.value ?: emptyList()
        )
    }

    fun onCategorySelected(category: String) {
        _uiState.update { it.copy(selectedCategory = category) }
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
            DayOfWeek.MONDAY -> schedule.monday
            DayOfWeek.TUESDAY -> schedule.tuesday
            DayOfWeek.WEDNESDAY -> schedule.wednesday
            DayOfWeek.THURSDAY -> schedule.thursday
            DayOfWeek.FRIDAY -> schedule.friday
            DayOfWeek.SATURDAY -> schedule.saturday
            DayOfWeek.SUNDAY -> schedule.sunday
            else -> emptyList()
        } ?: emptyList()

        val takenTimes = appointments.filter { appointment ->
            val appointmentDate = appointment.date?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate()
            appointmentDate == date
        }.mapNotNull { it.time }

        val nowTime = LocalTime.now()
        val isToday = date == LocalDate.now()

        val availableSlots = rawSlots.filter { slot ->
            val isTaken = takenTimes.contains(slot.time)

            val slotTime = try {
                LocalTime.parse(slot.time, DateTimeFormatter.ofPattern("HH:mm"))
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