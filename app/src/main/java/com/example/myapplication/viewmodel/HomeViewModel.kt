package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.users.Trainer
import com.example.myapplication.data.model.users.User
import com.example.myapplication.data.repository.AuthRepository
import com.example.myapplication.data.repository.TrainerRepository
import com.example.myapplication.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeState(
    val user: User? = null,
    val trainers: List<Trainer> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val trainerRepository: TrainerRepository
) : ViewModel() {

    private val _homeState = MutableStateFlow(HomeState(isLoading = true))
    val homeState: StateFlow<HomeState> = _homeState.asStateFlow()

    init {
        loadCurrentUser()
        loadTrainers()
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

    private fun loadTrainers() {
        viewModelScope.launch {
            _homeState.value = _homeState.value.copy(isLoading = true)

            trainerRepository.getAllTrainers()
                .onSuccess { trainersList ->
                    if (trainersList.isNotEmpty()) {
                        _homeState.value = _homeState.value.copy(
                            isLoading = false,
                            trainers = trainersList
                        )
                    } else {
                        _homeState.value = _homeState.value.copy(
                            isLoading = false,
                            trainers = emptyList(),
                        )
                    }
                }
                .onFailure { exception ->
                    _homeState.value = _homeState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Nie udało się załadować trenerów"
                    )
                }


        }
    }

    fun logout() {
        authRepository.logoutUser()
        _homeState.value = HomeState()
    }
}
