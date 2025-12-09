package com.example.myapplication.viewmodel.trainer

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.gyms.Gym
import com.example.myapplication.data.model.trainings.Appointment
import com.example.myapplication.data.model.trainings.TrainingSlot
import com.example.myapplication.data.model.users.User
import com.example.myapplication.data.model.trainings.WeeklySchedule
import com.example.myapplication.data.repository.AuthRepository
import com.example.myapplication.data.repository.GymRepository
import com.example.myapplication.data.repository.ScheduleRepository
import com.example.myapplication.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class BulkScheduleConfig(
    val selectedDays: Set<String>,
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val durationMinutes: Int,
    val breakMinutes: Int
)

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val repository: ScheduleRepository,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val gymRepository: GymRepository,
) : ViewModel() {

    val currentUserId = authRepository.getCurrentUserId()

    val weeklySchedule: LiveData<WeeklySchedule> = repository.weeklySchedule
    val appointments: LiveData<List<Appointment>> = repository.appointments

    fun loadSchedule() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getWeeklySchedule(currentUserId.toString())
        }
    }

    fun updateSchedule(schedule: WeeklySchedule) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.setWeeklySchedule(currentUserId, schedule)
        }
    }

    fun addNewSlot(day: String, slot: TrainingSlot) {
        viewModelScope.launch(Dispatchers.Default) {
            val currentSchedule = weeklySchedule.value ?: WeeklySchedule()

            val updatedSchedule = when (day.lowercase()) {
                "monday" -> currentSchedule.copy(monday = (currentSchedule.monday.orEmpty() + slot).sortedByTime())
                "tuesday" -> currentSchedule.copy(tuesday = (currentSchedule.tuesday.orEmpty() + slot).sortedByTime())
                "wednesday" -> currentSchedule.copy(wednesday = (currentSchedule.wednesday.orEmpty() + slot).sortedByTime())
                "thursday" -> currentSchedule.copy(thursday = (currentSchedule.thursday.orEmpty() + slot).sortedByTime())
                "friday" -> currentSchedule.copy(friday = (currentSchedule.friday.orEmpty() + slot).sortedByTime())
                "saturday" -> currentSchedule.copy(saturday = (currentSchedule.saturday.orEmpty() + slot).sortedByTime())
                "sunday" -> currentSchedule.copy(sunday = (currentSchedule.sunday.orEmpty() + slot).sortedByTime())
                else -> currentSchedule
            }

            withContext(Dispatchers.IO) {
                repository.setWeeklySchedule(currentUserId, updatedSchedule)
            }
        }
    }

    fun removeSlot(day: String, slot: TrainingSlot) {
        viewModelScope.launch(Dispatchers.Default) {
            val currentSchedule = weeklySchedule.value ?: WeeklySchedule()

            val updatedSchedule = when (day.lowercase()) {
                "monday" -> currentSchedule.copy(monday = currentSchedule.monday.orEmpty().filterNot { it == slot }.sortedByTime())
                "tuesday" -> currentSchedule.copy(tuesday = currentSchedule.tuesday.orEmpty().filterNot { it == slot }.sortedByTime())
                "wednesday" -> currentSchedule.copy(wednesday = currentSchedule.wednesday.orEmpty().filterNot { it == slot }.sortedByTime())
                "thursday" -> currentSchedule.copy(thursday = currentSchedule.thursday.orEmpty().filterNot { it == slot }.sortedByTime())
                "friday" -> currentSchedule.copy(friday = currentSchedule.friday.orEmpty().filterNot { it == slot }.sortedByTime())
                "saturday" -> currentSchedule.copy(saturday = currentSchedule.saturday.orEmpty().filterNot { it == slot }.sortedByTime())
                "sunday" -> currentSchedule.copy(sunday = currentSchedule.sunday.orEmpty().filterNot { it == slot }.sortedByTime())
                else -> currentSchedule
            }

            withContext(Dispatchers.IO) {
                repository.setWeeklySchedule(currentUserId, updatedSchedule)
            }
        }
    }

    private fun generateSlots(config: BulkScheduleConfig): Map<String, List<TrainingSlot>> {
        val result = mutableMapOf<String, MutableList<TrainingSlot>>()
        val startTotalMinutes = config.startHour * 60 + config.startMinute
        val endTotalMinutes = config.endHour * 60 + config.endMinute

        config.selectedDays.forEach { day ->
            val daySlots = mutableListOf<TrainingSlot>()
            var currentMinutes = startTotalMinutes

            while (currentMinutes + config.durationMinutes <= endTotalMinutes) {
                val h = currentMinutes / 60
                val m = currentMinutes % 60
                val timeString = String.format("%02d:%02d", h, m)
                daySlots.add(TrainingSlot(timeString, config.durationMinutes))
                currentMinutes += config.durationMinutes + config.breakMinutes
            }
            result[day] = daySlots
        }
        return result
    }

    fun applyBulkSchedule(config: BulkScheduleConfig) {
        viewModelScope.launch(Dispatchers.Default) {
            val newSlotsMap = generateSlots(config)
            val currentSchedule = weeklySchedule.value ?: WeeklySchedule()

            fun mergeSlots(existing: List<TrainingSlot>?, new: List<TrainingSlot>?): List<TrainingSlot> {
                if (new.isNullOrEmpty()) return existing.orEmpty()
                return (existing.orEmpty() + new)
                    .distinctBy { it.time }
                    .sortedByTime()
            }

            val updatedSchedule = currentSchedule.copy(
                monday = mergeSlots(currentSchedule.monday, newSlotsMap["Monday"]),
                tuesday = mergeSlots(currentSchedule.tuesday, newSlotsMap["Tuesday"]),
                wednesday = mergeSlots(currentSchedule.wednesday, newSlotsMap["Wednesday"]),
                thursday = mergeSlots(currentSchedule.thursday, newSlotsMap["Thursday"]),
                friday = mergeSlots(currentSchedule.friday, newSlotsMap["Friday"]),
                saturday = mergeSlots(currentSchedule.saturday, newSlotsMap["Saturday"]),
                sunday = mergeSlots(currentSchedule.sunday, newSlotsMap["Sunday"])
            )

            withContext(Dispatchers.IO) {
                repository.setWeeklySchedule(currentUserId, updatedSchedule)
            }
        }
    }

    fun loadAppointments() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getAppointments(currentUserId.toString())
        }
    }

    fun addNewAppointment(appointment: Appointment) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addAppointment(appointment)
        }
    }

    fun loadAppointmentsForMonthYear(month: Int, year: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getAppointmentsForMonthYear(currentUserId.toString(), month = month, year = year)
        }
    }

    suspend fun getUserById(id: String): User? {
        return withContext(Dispatchers.IO) {
            userRepository.getUserSync(id)
        }
    }

    suspend fun getGymById(id: String): Gym? {
        return gymRepository.getGymById(id).getOrNull()
    }

    private fun List<TrainingSlot>.sortedByTime(): List<TrainingSlot> {
        return this.sortedBy { slot ->
            if (slot.time == null) return@sortedBy 0

            try {
                val parts = slot.time!!.split(":")
                val hour = parts[0].toInt()
                val minute = parts[1].toInt()
                hour * 60 + minute
            } catch (e: Exception) {
                0
            }
        }
    }
}