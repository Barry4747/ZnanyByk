package com.example.myapplication.viewmodel.profile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repository.AuthRepository
import com.example.myapplication.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val userName: String = "",
    val userAddress: String = "",
    val userRole: String = "",
    val avatarUrl: String? = null
)


@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    init {
        loadUserData()
        loadUserAddress()
        loadAvatar()
    }

    private fun loadUserData() {
        val cachedUser = authRepository.getCachedUser()?.second
        val userName = when {
            cachedUser != null -> "${cachedUser.firstName} ${cachedUser.lastName}"
            else -> authRepository.getCurrentUserEmail() ?: ""
        }
        val userRole = cachedUser?.role?.name ?: ""
        _state.value = _state.value.copy(userName = userName, userRole = userRole)
    }

    private fun loadUserAddress() {
        viewModelScope.launch {
            val uid = authRepository.getCurrentUserId()
            if (uid != null) {
                userRepository.getUserLocation(uid).onSuccess { location ->
                    val locationString = location?.fullAddress ?: ""
                    _state.value = _state.value.copy(userAddress = locationString)
                }
            }
        }
    }

    private fun loadAvatar() {
        viewModelScope.launch {
            val uid = authRepository.getCurrentUserId()
            if (uid != null) {
                userRepository.getAvatarUrl(uid).onSuccess { url ->
                    _state.value = _state.value.copy(avatarUrl = url)
                }
            }
        }
    }

    fun addAvatar(context: Context, uri: Uri) {
        viewModelScope.launch {
            val uid = authRepository.getCurrentUserId()
            if (uid != null) {
                userRepository.uploadAvatar(context, uid, uri).onSuccess {
                    _state.value = _state.value.copy(avatarUrl = it)
                }
            }
        }
    }

    fun logout() {
        authRepository.logoutUser()
    }
}