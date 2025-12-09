package com.example.myapplication.viewmodel.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.trainings.Appointment
import com.example.myapplication.data.model.trainings.DayOfTheWeek
import com.example.myapplication.data.repository.AuthRepository
import com.example.myapplication.data.repository.ScheduleRepository
import com.example.myapplication.data.repository.TrainerRepository
import com.example.myapplication.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.util.Date
import javax.inject.Inject

sealed class PaymentUiState {
    object Idle : PaymentUiState()
    object Processing : PaymentUiState()
    object Success : PaymentUiState()
    data class Error(val message: String) : PaymentUiState()
}

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val repository: ScheduleRepository,
    private val authRepository: AuthRepository,
    private val trainerRepository: TrainerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<PaymentUiState>(PaymentUiState.Idle)
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

    fun processPayment(trainerId: String, dateMillis: Long, time: String, title: String) {
        viewModelScope.launch {
            _uiState.value = PaymentUiState.Processing

            delay(2000)

            val currentUserId = authRepository.getCurrentUserId()

            if (currentUserId == null) {
                _uiState.value = PaymentUiState.Error("Użytkownik nie jest zalogowany")
                return@launch
            }

            val dateObj = Date(dateMillis)

            val localDate = Instant.ofEpochMilli(dateMillis)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()

            val dayOfWeekEnum = when(localDate.dayOfWeek) {
                java.time.DayOfWeek.MONDAY -> DayOfTheWeek.MONDAY
                java.time.DayOfWeek.TUESDAY -> DayOfTheWeek.TUESDAY
                java.time.DayOfWeek.WEDNESDAY -> DayOfTheWeek.WEDNESDAY
                java.time.DayOfWeek.THURSDAY -> DayOfTheWeek.THURSDAY
                java.time.DayOfWeek.FRIDAY -> DayOfTheWeek.FRIDAY
                java.time.DayOfWeek.SATURDAY -> DayOfTheWeek.SATURDAY
                java.time.DayOfWeek.SUNDAY -> DayOfTheWeek.SUNDAY
                else -> null
            }

            val gymId = trainerRepository.getTrainerById(trainerId).getOrNull()?.gymId


            val newAppointment = Appointment(
                trainerId = trainerId,
                clientId = currentUserId,
                date = dateObj,
                time = time,
                duration = 60,
                dayOfWeek = dayOfWeekEnum,
                title = title,
                createdAt = null,
                gymId = gymId
            )

            repository.addAppointment(newAppointment, onSuccess = {
                _uiState.value = PaymentUiState.Success
            })
        }
    }
}