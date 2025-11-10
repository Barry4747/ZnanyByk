package com.example.myapplication.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.users.Trainer
import com.example.myapplication.data.repository.TrainerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// Stan dla ekranu szczegółów
data class TrainerDetailState(
    val trainer: Trainer? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class TrainerDetailViewModel @Inject constructor(
    private val trainerRepository: TrainerRepository,

    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _trainerDetailState = MutableStateFlow(TrainerDetailState())
    val trainerDetailState: StateFlow<TrainerDetailState> = _trainerDetailState.asStateFlow()
    private val trainerId: String = checkNotNull(savedStateHandle["trainerId"])






    init {
        Log.d("SavedStateHandle_Debug", "--- Sprawdzanie zawartości SavedStateHandle ---")

        // Sprawdź, czy w ogóle są jakieś klucze
        if (savedStateHandle.keys().isEmpty()) {
            Log.d("SavedStateHandle_Debug", "SavedStateHandle jest PUSTY! Brak argumentów nawigacji.")
        } else {
            // Jeśli są klucze, wypisz je wszystkie
            savedStateHandle.keys().forEach { key ->
                val value = savedStateHandle.get<Any>(key) // Pobierz wartość jako 'Any'
                Log.d("SavedStateHandle_Debug", "Klucz: '$key', Wartość: '$value'")
            }
        }
        Log.d("SavedStateHandle_Debug", "------------------------------------------")
        // --- KONIEC SEKCJI DEBUGOWANIA ---

        // Twoja obecna logika - teraz wiesz, dlaczego może się wysypać
        Log.d("TrainerDetailViewModel", "Próba odczytu ID trenera: $trainerId")
        loadTrainerDetails()
    }

    private fun loadTrainerDetails() {
        viewModelScope.launch {
            _trainerDetailState.update { it.copy(isLoading = true) }


            val result = trainerRepository.getTrainerById(trainerId)

            result.onSuccess { trainer ->
                _trainerDetailState.update {
                    it.copy(isLoading = false, trainer = trainer)
                }
            }.onFailure { error ->
                _trainerDetailState.update {
                    it.copy(isLoading = false, errorMessage = error.message)
                }
            }
        }
    }
}
