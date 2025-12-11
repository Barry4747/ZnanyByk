package com.example.myapplication.viewmodel.registration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.users.User
import com.example.myapplication.data.repository.AuthRepository
import com.example.myapplication.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

data class AuthState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val user: User? = null,
    val pendingGoogleUid: String? = null,
    val isCheckingAuth: Boolean = false,
    val passwordResetMessage: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    @param:Named("webClientId") private val webClientId: String
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.currentUser.collect { firebaseUser ->
                if (firebaseUser == null) {
                    authRepository.clearCachedUser()
                    _authState.value = AuthState()
                }
            }
        }
    }

    fun checkAuthState() {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isCheckingAuth = true)

            val firebaseUser = authRepository.currentUser.value

            if (firebaseUser != null) {
                val userId = firebaseUser.uid
                val cachedData = authRepository.getCachedUser()

                if (cachedData != null && cachedData.first == userId) {
                    setSuccess(cachedData.second)
                } else {
                    loadUserData(userId)
                }
            } else {
                authRepository.clearCachedUser()
                _authState.value = _authState.value.copy(isCheckingAuth = false)
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            setLoading()

            authRepository.loginUser(email, password)
                .onSuccess { uid -> loadUserData(uid) }
                .onFailure { e -> setError(e.message ?: "Login failed") }
        }
    }

    fun signInWithGoogle() {
        viewModelScope.launch {
            setLoading()

            authRepository.signInWithGoogle(webClientId)
                .onSuccess { result ->
                    handleGoogleSignInResult(result.uid)
                }
                .onFailure { e -> setError(e.message ?: "Google sign-in failed") }
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, errorMessage = null, passwordResetMessage = null)
            authRepository.resetPassword(email)
                .onSuccess {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        passwordResetMessage = "Link do resetowania hasła został wysłany na podany adres e-mail."
                    )
                }
                .onFailure { e ->
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        errorMessage = "Nie udało się wysłać linku: ${e.message}",
                        passwordResetMessage = null
                    )
                }
        }
    }

    private suspend fun handleGoogleSignInResult(uid: String) {
        userRepository.getUser(uid)
            .onSuccess { user ->
                if (user != null) {
                    authRepository.saveCachedUser(user, uid)
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
                    authRepository.saveCachedUser(user, uid)
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
            errorMessage = null,
            isCheckingAuth = false
        )
    }

    private fun setError(message: String) {
        _authState.value = _authState.value.copy(
            isLoading = false,
            errorMessage = message,
            isCheckingAuth = false
        )
    }

    private fun setSuccess(user: User) {
        _authState.value = _authState.value.copy(
            isLoading = false,
            user = user,
            errorMessage = null,
            isCheckingAuth = false
        )
    }
}
