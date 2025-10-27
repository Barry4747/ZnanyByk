package com.example.myapplication.viewmodel.registration

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.users.Role
import com.example.myapplication.data.model.users.Trainer
import com.example.myapplication.data.repository.AuthRepository
import com.example.myapplication.data.repository.TrainerRepository
import com.example.myapplication.data.repository.UserRepository
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
    private val trainerRepository: TrainerRepository,
    private val userRepository: UserRepository
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
        context: Context,
        hourlyRate: String,
        gymId: String?,
        description: String,
        experienceYears: String,
        selectedCategories: List<String>,
        images: List<Uri>
    ) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)

            val currentUserId = getCurrentUserId() ?: return@launch
            val cachedUser = getCachedUser() ?: return@launch
            val (hourlyRateInt, experienceInt) = validateAndParseInput(hourlyRate, experienceYears) ?: return@launch

            val (uploadedUrls, failedUris) = uploadImages(context, currentUserId, images) ?: return@launch

            addTrainer(cachedUser, description, hourlyRateInt, experienceInt, gymId, selectedCategories, uploadedUrls, failedUris)
            //DO IT HERE
        }
    }

    private fun getCurrentUserId(): String? {
        val currentUserId = authRepository.getCurrentUserId()
        if (currentUserId == null) {
            _state.value = _state.value.copy(
                isLoading = false,
                errorMessage = "Brak zalogowanego użytkownika"
            )
        }
        return currentUserId
    }

    private fun getCachedUser(): com.example.myapplication.data.model.users.User? {
        val cachedUser = authRepository.getCachedUser()?.second
        if (cachedUser == null) {
            _state.value = _state.value.copy(
                isLoading = false,
                errorMessage = "Nie znaleziono danych użytkownika"
            )
        }
        return cachedUser
    }

    private fun validateAndParseInput(hourlyRate: String, experienceYears: String): Pair<Int, Int>? {
        val (hourlyRateInt, validationError) = validateTrainerInput(hourlyRate, experienceYears)
        if (validationError != null) {
            _state.value = _state.value.copy(
                isLoading = false,
                errorMessage = validationError
            )
            return null
        }
        val experienceInt = experienceYears.toIntOrNull()!!
        return Pair(hourlyRateInt!!, experienceInt)
    }

    private suspend fun uploadImages(context: Context, currentUserId: String, images: List<Uri>): Pair<List<String>, List<Uri>>? {
        val uploadResult = try {
            trainerRepository.uploadImages(context, currentUserId, images)
        } catch (e: Exception) {
            Result.failure<Pair<List<String>, List<Uri>>>(e)
        }

        if (uploadResult.isFailure) {
            _state.value = _state.value.copy(
                isLoading = false,
                errorMessage = "Błąd podczas wysyłania zdjęć: ${uploadResult.exceptionOrNull()?.message}"
            )
            Log.e("TrainerRegistrationVM", "Upload images failed for user=$currentUserId", uploadResult.exceptionOrNull())
            return null
        }

        return uploadResult.getOrNull() ?: (emptyList<String>() to emptyList<Uri>())
    }

    private suspend fun addTrainer(
        cachedUser: com.example.myapplication.data.model.users.User,
        description: String,
        hourlyRateInt: Int,
        experienceInt: Int,
        gymId: String?,
        selectedCategories: List<String>,
        uploadedUrls: List<String>,
        failedUris: List<Uri>
    ) {
        val trainer = Trainer(
            email = cachedUser.email,
            firstName = cachedUser.firstName,
            lastName = cachedUser.lastName,
            phoneNumber = cachedUser.phoneNumber,
            description = description.ifBlank { null },
            pricePerHour = hourlyRateInt,
            experience = experienceInt,
            location = gymId,
            categories = selectedCategories,
            ratings = null,
            avgRating = null,
            images = uploadedUrls.ifEmpty { null }
        )

        trainerRepository.addTrainer(trainer, authRepository.getCurrentUserId()!!)
            .onSuccess { trainerId ->
                userRepository.updateUserRole(authRepository.getCurrentUserId()!!, Role.TRAINER)

                val updatedUser = cachedUser.copy(role = Role.TRAINER)
                authRepository.saveCachedUser(updatedUser, authRepository.getCurrentUserId()!!)

                val successMsg = if (failedUris.isNotEmpty()) {
                    "Profil trenera utworzony pomyślnie, ale ${failedUris.size} zdjęć nie zostało przesłanych."
                } else {
                    "Profil trenera został utworzony pomyślnie!"
                }

                _state.value = _state.value.copy(
                    isLoading = false,
                    successMessage = successMsg,
                    errorMessage = null
                )
            }
            .onFailure { exception ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Błąd podczas tworzenia profilu: ${exception.message}"
                )
                Log.e("TrainerRegistration", "Błąd dodawania trenera dla userId=${authRepository.getCurrentUserId()}", exception)
            }
    }
}
