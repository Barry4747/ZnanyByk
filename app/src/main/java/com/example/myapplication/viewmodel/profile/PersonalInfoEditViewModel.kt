package com.example.myapplication.viewmodel.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.users.UserLocation
import com.example.myapplication.data.repository.AuthRepository
import com.example.myapplication.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

data class PersonalInfoState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val firstName: String = "",
    val lastName: String = "",
    val phoneNumber: String = "",
    val birthDate: Date? = null,
    val location: UserLocation? = null,
    val email: String = "",
    val role: String = ""
)

@HiltViewModel
class PInfoEditViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PersonalInfoState())
    val state: StateFlow<PersonalInfoState> = _state.asStateFlow()

    init {
        loadUserData()
    }

    private fun loadUserData() {
        val cachedUser = authRepository.getCachedUser()?.second
        if (cachedUser != null) {
            _state.value = _state.value.copy(
                firstName = cachedUser.firstName,
                lastName = cachedUser.lastName,
                phoneNumber = cachedUser.phoneNumber ?: "",
                birthDate = cachedUser.birthDate,
                location = cachedUser.location,
                email = cachedUser.email,
                role = cachedUser.role.name
            )
        }
    }

    fun saveChanges(
        firstName: String,
        lastName: String,
        phoneNumber: String,
        birthDate: Date?,
        location: UserLocation?,
        email: String
    ) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)

            val uid = authRepository.getCurrentUserId()
            if (uid == null) {
                _state.value = _state.value.copy(isLoading = false, errorMessage = "Brak zalogowanego użytkownika")
                return@launch
            }

            val cachedUser = authRepository.getCachedUser()?.second
            if (cachedUser == null) {
                _state.value = _state.value.copy(isLoading = false, errorMessage = "Nie znaleziono danych użytkownika")
                return@launch
            }

            val updatedUser = cachedUser.copy(
                firstName = firstName,
                lastName = lastName,
                phoneNumber = phoneNumber.takeIf { it.isNotBlank() },
                birthDate = birthDate,
                location = location,
                email = email
            )

            userRepository.updateUser(uid, updatedUser)
                .onSuccess {
                    authRepository.saveCachedUser(updatedUser, uid)
                    _state.value = _state.value.copy(isLoading = false, successMessage = "Dane zostały zaktualizowane")
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(isLoading = false, errorMessage = e.message ?: "Błąd podczas aktualizacji")
                }
        }
    }
}