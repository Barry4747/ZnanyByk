package com.example.myapplication.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.Trainer
import com.example.myapplication.data.repository.AuthRepository
import com.example.myapplication.data.repository.TrainerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TrainerRegistrationState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val userName: String = ""
)

@HiltViewModel
class TrainerRegistrationViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val trainerRepository: TrainerRepository
) : ViewModel() {

    private val _state = MutableStateFlow(TrainerRegistrationState())
    val state: StateFlow<TrainerRegistrationState> = _state.asStateFlow()

    init {
        loadUserName()
    }

    private fun loadUserName() {
        val cachedUser = authRepository.getCachedUser()?.second
        val userName = when {
            cachedUser != null -> "${cachedUser.firstName} ${cachedUser.lastName}"
            else -> authRepository.getCurrentUserEmail() ?: ""
        }
        _state.value = _state.value.copy(userName = userName)
    }

    private fun validateTrainerInput(
        hourlyRate: String,
        experienceYears: String
    ): Pair<Int?, String?> {
        val hourlyRateInt = hourlyRate.toIntOrNull()
        if (hourlyRateInt == null || hourlyRateInt <= 0) {
            return null to "Kwota za godzinę musi być liczbą większą od 0"
        }

        val experienceInt = experienceYears.toIntOrNull()
        if (experienceInt == null || experienceInt < 0) {
            return null to "Doświadczenie musi być liczbą większą lub równą 0"
        }

        return hourlyRateInt to null
    }

    fun submitTrainerProfile(
        hourlyRate: String,
        gymId: String?,
        description: String,
        experienceYears: String
    ) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)

            val currentUserId = authRepository.getCurrentUserId()
            if (currentUserId == null) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Brak zalogowanego użytkownika"
                )
                return@launch
            }

            val cachedUser = authRepository.getCachedUser()?.second
            if (cachedUser == null) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Nie znaleziono danych użytkownika"
                )
                return@launch
            }

            val (hourlyRateInt, validationError) = validateTrainerInput(hourlyRate, experienceYears)
            if (validationError != null) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = validationError
                )
                return@launch
            }

            val experienceInt = experienceYears.toIntOrNull()!!

            val trainer = Trainer(
                email = cachedUser.email,
                firstName = cachedUser.firstName,
                lastName = cachedUser.lastName,
                phoneNumber = cachedUser.phoneNumber,
                description = description.ifBlank { null },
                pricePerHour = hourlyRateInt!!,
                experience = experienceInt,
                location = gymId,
                specialities = null,
                ratings = null,
                avgRating = null
            )

            trainerRepository.addTrainer(trainer, currentUserId)
                .onSuccess { trainerId ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        successMessage = "Profil trenera został utworzony pomyślnie!",
                        errorMessage = null
                    )
                }
                .onFailure { exception ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = "Błąd podczas tworzenia profilu: ${exception.message}"
                    )
                    Log.e("TrainerRegistration", "Błąd dodawania trenera dla userId=$currentUserId", exception)
                }
        }
    }

    fun clearMessages() {
        _state.value = _state.value.copy(
            errorMessage = null,
            successMessage = null
        )
    }
}
