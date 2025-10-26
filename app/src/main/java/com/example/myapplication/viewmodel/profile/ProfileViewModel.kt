package com.example.myapplication.viewmodel.profile

import androidx.lifecycle.ViewModel
import com.example.myapplication.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class ProfileState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val userName: String = ""
)


@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    init {
        loadUserData()
    }

    private fun loadUserData() {
        val cachedUser = authRepository.getCachedUser()?.second
        val userName = when {
            cachedUser != null -> "${cachedUser.firstName} ${cachedUser.lastName}"
            else -> authRepository.getCurrentUserEmail() ?: ""
        }
        _state.value = _state.value.copy(userName = userName)
    }
}