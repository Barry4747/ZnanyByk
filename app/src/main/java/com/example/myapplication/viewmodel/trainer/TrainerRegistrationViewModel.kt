package com.example.myapplication.viewmodel.trainer

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.gyms.Gym
import com.example.myapplication.data.model.users.Role
import com.example.myapplication.data.model.users.Trainer
import com.example.myapplication.data.model.users.User
import com.example.myapplication.data.repository.AuthRepository
import com.example.myapplication.data.repository.GymRepository
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
    val userName: String = "",
    val selectedImages: List<Uri> = emptyList(),
    val uploadedImages: List<String> = emptyList(),
    val isUploadingImages: Boolean = false,
    val gyms: List<Gym> = emptyList()
)

@HiltViewModel
class TrainerRegistrationViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val trainerRepository: TrainerRepository,
    private val userRepository: UserRepository,
    private val gymRepository: GymRepository
) : ViewModel() {

    private val _state = MutableStateFlow(TrainerRegistrationState())
    val state: StateFlow<TrainerRegistrationState> = _state.asStateFlow()

    init {
        loadUserName()
        loadGyms()
    }

    private fun loadGyms() {
        viewModelScope.launch {
            gymRepository.getAllGyms()
                .onSuccess { gyms ->
                    _state.value = _state.value.copy(gyms = gyms)
                }
                .onFailure { exception ->
                    Log.e("TrainerRegistrationVM", "Failed to load gyms", exception)
                }
        }
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
        experienceYears: String,
        selectedCategories: List<String>,
        images: List<String>
    ) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)

            val cachedUser = getCachedUser() ?: return@launch
            val (hourlyRateInt, experienceInt) = validateAndParseInput(hourlyRate, experienceYears) ?: return@launch

            addTrainer(cachedUser, description, hourlyRateInt, experienceInt, gymId, selectedCategories, images, emptyList())
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

    private fun getCachedUser(): User? {
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

    private suspend fun addTrainer(
        cachedUser: User,
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
            gymId = gymId,
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

    fun uploadMedias(context: Context, uris: List<Uri>) {
        val currentUserId = getCurrentUserId() ?: return
        _state.value = _state.value.copy(selectedImages = _state.value.selectedImages + uris, isUploadingImages = true)
        viewModelScope.launch {
            val result = trainerRepository.uploadMedias(context, currentUserId, uris)
            result.onSuccess { (successfulUrls, _) ->
                _state.value = _state.value.copy(
                    selectedImages = _state.value.selectedImages - uris,
                    uploadedImages = _state.value.uploadedImages + successfulUrls,
                    isUploadingImages = false
                )
            }.onFailure {
                _state.value = _state.value.copy(
                    selectedImages = _state.value.selectedImages - uris,
                    isUploadingImages = false,
                    errorMessage = "Błąd podczas wysyłania zdjęć"
                )
            }
        }
    }

    fun removeSelectedMedia(uri: Uri) {
        _state.value = _state.value.copy(selectedImages = _state.value.selectedImages - uri)
    }

    fun removeUploadedMedia(url: String) {
        _state.value = _state.value.copy(uploadedImages = _state.value.uploadedImages.filter { it != url })
        viewModelScope.launch {
            trainerRepository.deleteImageByUrl(url)
        }
    }

    fun isVideoUri(context: Context, uri: Uri): Boolean {
        return context.contentResolver.getType(uri)?.startsWith("video/") == true
    }

    fun isVideoUrl(url: String): Boolean {
        return url.contains(".mp4", ignoreCase = true)
    }
}
