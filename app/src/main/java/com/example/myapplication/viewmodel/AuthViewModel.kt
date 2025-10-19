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

data class RegistrationCredentials(
    val email: String = "",
    val password: String = ""
)

data class AuthState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val user: User? = null,
    val registrationCredentials: RegistrationCredentials = RegistrationCredentials(),
    val pendingGoogleUid: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun saveRegistrationCredentials(email: String, password: String) {
        _authState.value = _authState.value.copy(
            registrationCredentials = RegistrationCredentials(email, password)
        )
    }

    fun register(firstName: String, lastName: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, errorMessage = null)

            val credentials = _authState.value.registrationCredentials
            val pendingUid = _authState.value.pendingGoogleUid

            if (pendingUid != null) {
                val email = credentials.email.ifBlank {
                    authRepository.getCurrentUserEmail() ?: ""
                }

                if (email.isBlank()) {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        errorMessage = "Missing email from Google account. Please try signing in again."
                    )
                    return@launch
                }

                val user = User(
                    firstName = firstName,
                    lastName = lastName,
                    email = email
                )

                userRepository.addUser(user, pendingUid)
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

                return@launch
            }

            // Classic email/password flow - check credentials
            if (credentials.email.isBlank()) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    errorMessage = "Missing email. Please start registration again."
                )
                return@launch
            }

            if (credentials.password.isBlank()) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    errorMessage = "Missing password. Please start registration again."
                )
                return@launch
            }

            authRepository.registerUser(credentials.email, credentials.password)
                .onSuccess { uid ->
                    val user = User(
                        firstName = firstName,
                        lastName = lastName,
                        email = credentials.email
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

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, errorMessage = null)

            authRepository.loginUser(email, password)
                .onSuccess { uid ->
                    userRepository.getUser(uid)
                        .onSuccess { user ->
                            if (user != null) {
                                _authState.value = AuthState(
                                    isLoading = false,
                                    user = user
                                )
                            } else {
                                _authState.value = AuthState(
                                    isLoading = false,
                                    errorMessage = "User data not found"
                                )
                            }
                        }
                        .onFailure { e ->
                            _authState.value = AuthState(
                                isLoading = false,
                                errorMessage = e.message ?: "Failed to load user data"
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

    fun signInWithGoogle(webClientId: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, errorMessage = null)

            authRepository.signInWithGoogle(webClientId)
                .onSuccess { result ->
                    val uid = result.uid
                    android.util.Log.d("AuthViewModel", "Google sign-in result: uid=$uid, email=${result.email}")
                    android.util.Log.d("AuthViewModel", "getCurrentUserEmail: ${authRepository.getCurrentUserEmail()}")

                    userRepository.getUser(uid)
                        .onSuccess { user ->
                            if (user != null) {
                                android.util.Log.d("AuthViewModel", "User exists in Firestore, logging in")
                                _authState.value = AuthState(
                                    isLoading = false,
                                    user = user
                                )
                            } else {
                                // User authenticated with Google but no Firestore profile -> start registration flow
                                android.util.Log.d("AuthViewModel", "User NOT in Firestore, starting registration flow")
                                val email = result.email ?: authRepository.getCurrentUserEmail()
                                android.util.Log.d("AuthViewModel", "Email for registration: $email")

                                if (email.isNullOrBlank()) {
                                    android.util.Log.e("AuthViewModel", "Email is null or blank!")
                                    _authState.value = AuthState(
                                        isLoading = false,
                                        errorMessage = "Google account did not provide an email."
                                    )
                                } else {
                                    android.util.Log.d("AuthViewModel", "Setting pendingGoogleUid and registrationCredentials with email: $email")
                                    _authState.value = _authState.value.copy(
                                        isLoading = false,
                                        registrationCredentials = RegistrationCredentials(email = email, password = ""),
                                        pendingGoogleUid = uid
                                    )
                                }
                            }
                        }
                        .onFailure { e ->
                            android.util.Log.e("AuthViewModel", "Failed to get user from Firestore: ${e.message}", e)
                            _authState.value = AuthState(
                                isLoading = false,
                                errorMessage = e.message ?: "Failed to load user data"
                            )
                        }
                }
                .onFailure { e ->
                    android.util.Log.e("AuthViewModel", "Google sign-in failed: ${e.message}", e)
                    _authState.value = AuthState(
                        isLoading = false,
                        errorMessage = e.message ?: "Google sign-in failed"
                    )
                }
        }
    }

    fun clearPendingGoogleRegistration() {
        _authState.value = _authState.value.copy(pendingGoogleUid = null)
    }

    fun logout() {
        authRepository.logoutUser()
        _authState.value = AuthState()
    }
}
