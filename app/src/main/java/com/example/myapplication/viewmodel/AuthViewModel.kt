package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.User
import com.example.myapplication.data.repository.AuthRepository
import com.example.myapplication.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val user: User? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun register(firstName: String, lastName: String, email: String, password: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, errorMessage = null)

            authRepository.registerUser(email, password)
                .onSuccess { uid ->
                    val user = User(
                        firstName = firstName,
                        lastName = lastName,
                        email = email
                    )

                    userRepository.addUser(user, uid)
                        .onSuccess {
                            _authState.value = AuthState(
                                isLoading = false,
                                successMessage = "Registration successful!",
                                user = user
                            )
                        }
                        .onFailure { e ->
                            _authState.value = AuthState(
                                isLoading = false,
                                errorMessage = e.message ?: "Failed to save user data"
                            )
                        }
                }
                .onFailure { e ->
                    _authState.value = AuthState(
                        isLoading = false,
                        errorMessage = e.message ?: "Registration failed"
                    )
                }
        }
    }

    fun login(email: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, errorMessage = null)

            userRepository.getUserByEmail(email)
                .onSuccess { user ->
                    if (user != null) {
                        _authState.value = AuthState(
                            isLoading = false,
                            user = user
                        )
                    } else {
                        _authState.value = AuthState(
                            isLoading = false,
                            errorMessage = "User not found"
                        )
                    }
                }
                .onFailure { e ->
                    _authState.value = AuthState(
                        isLoading = false,
                        errorMessage = e.message ?: "Login failed"
                    )
                }
        }
    }

    fun logout() {
        authRepository.logoutUser()
        _authState.value = AuthState()
    }

    fun clearMessages() {
        _authState.value = _authState.value.copy(
            errorMessage = null,
            successMessage = null
        )
    }
}
