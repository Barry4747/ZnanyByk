package com.example.myapplication.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.R
import com.example.myapplication.data.model.User
import com.example.myapplication.data.repository.AuthRepository
import com.example.myapplication.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

private const val MINIMUM_PASSWORD_LENGTH = 6

data class RegistrationCredentials(
    val email: String = "",
    val password: String = "",
    val repeatPassword: String = ""
)

data class RegistrationState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val user: User? = null,
    val registrationCredentials: RegistrationCredentials = RegistrationCredentials(),
    val pendingGoogleUid: String? = null,
    val passwordValidationError: String? = null
)

@HiltViewModel
class RegistrationViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    @param:Named("webClientId") private val webClientId: String,
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    private val _registrationState = MutableStateFlow(RegistrationState())
    val registrationState: StateFlow<RegistrationState> = _registrationState.asStateFlow()

    fun updateEmail(email: String) {
        _registrationState.value = _registrationState.value.copy(
            registrationCredentials = _registrationState.value.registrationCredentials.copy(email = email)
        )
    }

    fun updatePassword(password: String) {
        val credentials = _registrationState.value.registrationCredentials.copy(password = password)
        _registrationState.value = _registrationState.value.copy(
            registrationCredentials = credentials,
            passwordValidationError = validatePasswords(password, credentials.repeatPassword)
        )
    }

    fun updateRepeatPassword(repeatPassword: String) {
        val credentials = _registrationState.value.registrationCredentials.copy(repeatPassword = repeatPassword)
        _registrationState.value = _registrationState.value.copy(
            registrationCredentials = credentials,
            passwordValidationError = validatePasswords(credentials.password, repeatPassword)
        )
    }

    private fun validatePasswords(password: String, repeatPassword: String): String? {
        return when {
            password.isBlank() && repeatPassword.isBlank() -> null
            password.length < MINIMUM_PASSWORD_LENGTH -> appContext.getString(R.string.minum_characters_password)
            repeatPassword.isNotBlank() && password != repeatPassword -> appContext.getString(R.string.passwords_mismatch)
            else -> null
        }
    }

    fun isFormValid(): Boolean {
        val credentials = _registrationState.value.registrationCredentials
        return credentials.email.isNotBlank() &&
               credentials.password.isNotBlank() &&
               credentials.repeatPassword.isNotBlank() &&
               credentials.password == credentials.repeatPassword &&
               credentials.password.length >= MINIMUM_PASSWORD_LENGTH &&
               _registrationState.value.passwordValidationError == null
    }

    fun saveRegistrationCredentials(email: String, password: String) {
        _registrationState.value = _registrationState.value.copy(
            registrationCredentials = RegistrationCredentials(email, password, password)
        )
    }

    fun register(
        firstName: String,
        lastName: String,
        phoneNumber: String? = null,
        birthDate: java.util.Date? = null
    ) {
        viewModelScope.launch {
            setLoading()

            val pendingUid = _registrationState.value.pendingGoogleUid

            if (pendingUid != null) {
                handleGoogleRegistration(firstName, lastName, phoneNumber, birthDate, pendingUid)
            } else {
                handleEmailPasswordRegistration(firstName, lastName, phoneNumber, birthDate)
            }
        }
    }

    fun signUpWithGoogle() {
        viewModelScope.launch {
            setLoading()

            authRepository.signInWithGoogle(webClientId)
                .onSuccess { result ->
                    handleGoogleSignUpResult(result.uid, result.email)
                }
                .onFailure { e -> setError(e.message ?: "Google sign-up failed") }
        }
    }

    fun clearPendingGoogleRegistration() {
        _registrationState.value = _registrationState.value.copy(
            pendingGoogleUid = null,
            registrationCredentials = RegistrationCredentials()
        )
    }

    private suspend fun handleGoogleRegistration(
        firstName: String,
        lastName: String,
        phoneNumber: String?,
        birthDate: java.util.Date?,
        uid: String
    ) {
        val email = getEmailForGoogleRegistration()
        if (email == null) {
            setError("Missing email. Please try signing in again.")
            return
        }

        val user = createUser(firstName, lastName, email, phoneNumber, birthDate)
        saveUserToFirestore(user, uid)
    }

    private suspend fun handleEmailPasswordRegistration(
        firstName: String,
        lastName: String,
        phoneNumber: String?,
        birthDate: java.util.Date?
    ) {
        val credentials = _registrationState.value.registrationCredentials

        if (credentials.email.isBlank()) {
            setError("Missing email. Please start registration again.")
            return
        }

        if (credentials.password.isBlank()) {
            setError("Missing password. Please start registration again.")
            return
        }

        authRepository.registerUser(credentials.email, credentials.password)
            .onSuccess { uid ->
                val user = createUser(firstName, lastName, credentials.email, phoneNumber, birthDate)
                saveUserToFirestore(user, uid)
            }
            .onFailure { e -> setError(e.message ?: "Registration failed") }
    }

    private suspend fun handleGoogleSignUpResult(uid: String, email: String?) {
        userRepository.getUser(uid)
            .onSuccess { user ->
                if (user != null) {
                    userRepository.saveCachedUser(user, uid)
                    setSuccess(user, "Account already exists. Logging you in...")
                } else {
                    startGoogleRegistrationFlow(uid, email)
                }
            }
            .onFailure { e -> setError(e.message ?: "Failed to check user data") }
    }

    private fun startGoogleRegistrationFlow(uid: String, email: String?) {
        val finalEmail = email ?: authRepository.getCurrentUserEmail()

        if (finalEmail.isNullOrBlank()) {
            setError("Google account did not provide an email.")
        } else {
            _registrationState.value = _registrationState.value.copy(
                isLoading = false,
                registrationCredentials = RegistrationCredentials(email = finalEmail, password = ""),
                pendingGoogleUid = uid
            )
        }
    }

    private suspend fun saveUserToFirestore(user: User, uid: String) {
        userRepository.addUser(user, uid)
            .onSuccess {
                userRepository.saveCachedUser(user, uid)
                setSuccess(user, "Registration successful!")
            }
            .onFailure { e -> setError(e.message ?: "Failed to save user data") }
    }

    private fun getEmailForGoogleRegistration(): String? {
        val email = _registrationState.value.registrationCredentials.email.ifBlank {
            authRepository.getCurrentUserEmail()
        }
        return email?.takeIf { it.isNotBlank() }
    }

    private fun createUser(
        firstName: String,
        lastName: String,
        email: String,
        phoneNumber: String?,
        birthDate: java.util.Date?
    ): User {
        return User(
            firstName = firstName,
            lastName = lastName,
            email = email,
            phoneNumber = phoneNumber,
            birthDate = birthDate
        )
    }

    private fun setLoading() {
        _registrationState.value = _registrationState.value.copy(
            isLoading = true,
            errorMessage = null
        )
    }

    private fun setError(message: String) {
        _registrationState.value = _registrationState.value.copy(
            isLoading = false,
            errorMessage = message
        )
    }

    private fun setSuccess(user: User, successMessage: String? = null) {
        _registrationState.value = _registrationState.value.copy(
            isLoading = false,
            successMessage = successMessage,
            user = user,
            errorMessage = null
        )
    }
}
