package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.User
import com.example.myapplication.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RegistrationState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class RegistrationViewModel : ViewModel() {
    private val repository = UserRepository()

    private val _registrationState = MutableStateFlow(RegistrationState())
    val registrationState: StateFlow<RegistrationState> = _registrationState.asStateFlow()

    fun register(firstName: String, lastName: String, email: String) {
        viewModelScope.launch {
            _registrationState.value = _registrationState.value.copy(isLoading = true, errorMessage = null)

            val user = User(
                first_name = firstName,
                last_name = lastName,
                email = email
            )

            repository.addUser(user)
                .onSuccess {
                    _registrationState.value = RegistrationState(
                        isLoading = false,
                        successMessage = "Registration successful!"
                    )
                }
                .onFailure { e ->
                    _registrationState.value = RegistrationState(
                        isLoading = false,
                        errorMessage = e.message ?: "Registration failed"
                    )
                }
        }
    }

    fun clearMessages() {
        _registrationState.value = _registrationState.value.copy(
            errorMessage = null,
            successMessage = null
        )
    }
}

