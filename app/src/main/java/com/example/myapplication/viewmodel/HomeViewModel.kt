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

data class HomeState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _homeState = MutableStateFlow(HomeState())
    val homeState: StateFlow<HomeState> = _homeState.asStateFlow()

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            _homeState.value = _homeState.value.copy(isLoading = true)

            val currentUserId = authRepository.getCurrentUserId()
            if (currentUserId != null) {
                userRepository.getUser(currentUserId)
                    .onSuccess { user ->
                        _homeState.value = HomeState(
                            user = user,
                            isLoading = false
                        )
                    }
                    .onFailure { e ->
                        _homeState.value = HomeState(
                            isLoading = false,
                            errorMessage = e.message ?: "Failed to load user data"
                        )
                    }
            } else {
                _homeState.value = HomeState(
                    isLoading = false,
                    errorMessage = "No user logged in"
                )
            }
        }
    }

    fun logout() {
        authRepository.logoutUser()
        _homeState.value = HomeState()
    }
}
