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
    val user: User? = null,
    val pendingGoogleUid: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            setLoading()

            authRepository.loginUser(email, password)
                .onSuccess { uid -> loadUserData(uid) }
                .onFailure { e -> setError(e.message ?: "Login failed") }
        }
    }

    fun signInWithGoogle(webClientId: String) {
        viewModelScope.launch {
            setLoading()

            authRepository.signInWithGoogle(webClientId)
                .onSuccess { result ->
                    handleGoogleSignInResult(result.uid, result.email)
                }
                .onFailure { e -> setError(e.message ?: "Google sign-in failed") }
        }
    }

    fun logout() {
        authRepository.logoutUser()
        _authState.value = AuthState()
    }

    private suspend fun handleGoogleSignInResult(uid: String, email: String?) {
        userRepository.getUser(uid)
            .onSuccess { user ->
                if (user != null) {
                    setSuccess(user)
                } else {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        pendingGoogleUid = uid
                    )
                }
            }
            .onFailure { e -> setError(e.message ?: "Failed to load user data") }
    }

    private suspend fun loadUserData(uid: String) {
        userRepository.getUser(uid)
            .onSuccess { user ->
                if (user != null) {
                    setSuccess(user)
                } else {
                    setError("User data not found")
                }
            }
            .onFailure { e -> setError(e.message ?: "Failed to load user data") }
    }

    private fun setLoading() {
        _authState.value = _authState.value.copy(
            isLoading = true,
            errorMessage = null
        )
    }

    private fun setError(message: String) {
        _authState.value = _authState.value.copy(
            isLoading = false,
            errorMessage = message
        )
    }

    private fun setSuccess(user: User) {
        _authState.value = _authState.value.copy(
            isLoading = false,
            user = user,
            errorMessage = null
        )
    }
}
