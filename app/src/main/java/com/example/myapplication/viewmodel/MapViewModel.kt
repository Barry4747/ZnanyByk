package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.gyms.GymLocation
import com.example.myapplication.data.model.users.Trainer
import com.example.myapplication.data.model.users.User
import com.example.myapplication.data.repository.AuthRepository
import com.example.myapplication.data.repository.GymRepository
import com.example.myapplication.data.repository.TrainerRepository
import com.example.myapplication.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MapState(
    val currentUser: User? = null,
    val isLoading: Boolean = false,
    val trainersWithGyms: List<Pair<Trainer, GymLocation>> = emptyList()
)

@HiltViewModel
class MapViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val trainerRepository: TrainerRepository,
    private val userRepository: UserRepository,
    private val gymRepository: GymRepository
) : ViewModel() {
    private val _state = MutableStateFlow(MapState())
    val state: StateFlow<MapState> = _state.asStateFlow()

    init {
        getCurrentUser()
    }

    private fun getCurrentUser() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val user = authRepository.getCachedUser()?.second
            _state.value = _state.value.copy(currentUser = user, isLoading = false)
        }
    }

    suspend fun getTrainersWithGymLocations(): List<Pair<Trainer, GymLocation>> {
        val trainersResult = trainerRepository.getAllTrainers()
        if (trainersResult.isFailure) return emptyList()
        val trainers = trainersResult.getOrNull() ?: emptyList()
        val result = mutableListOf<Pair<Trainer, GymLocation>>()
        for (trainer in trainers) {
            val gymLocation = trainer.gymId?.let { gymId ->
                gymRepository.getGymById(gymId).getOrNull()?.gymLocation
            }
            if (gymLocation != null) {
                result.add(trainer to gymLocation)
            }
        }
        return result
    }
}