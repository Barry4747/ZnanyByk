package com.example.myapplication.viewmodel

import androidx.activity.result.launch
import androidx.compose.animation.core.copy
import androidx.lifecycle.ViewModel
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.users.Trainer
import com.example.myapplication.data.repository.TrainerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.concurrent.atomics.update

data class TrainersState(
    val trainers: List<Trainer> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val priceMin: Int = 0,
    val priceMax: Int = 500,
    val selectedCategories: Set<String> = emptySet(),
    val minRating: Float = 0.0f
)

@HiltViewModel
class TrainersViewModel @Inject constructor(
    private val trainerRepository: TrainerRepository
) : ViewModel() {

    private val _trainersState = MutableStateFlow(TrainersState())
    val trainersState: StateFlow<TrainersState> = _trainersState.asStateFlow()

    fun loadInitialTrainers() {
        // Sprawdzamy, czy lista nie została już załadowana, aby uniknąć zbędnych zapytań
        if (trainersState.value.trainers.isNotEmpty() || trainersState.value.isLoading) {
            return
        }
        applyFiltersAndLoad() // Wywołujemy główną funkcję z domyślnymi filtrami
    }

    fun applyFiltersAndLoad() {
        viewModelScope.launch {
            val currentState = _trainersState.value
            _trainersState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = trainerRepository.getFilteredTrainers(
                minPrice = currentState.priceMin,
                maxPrice = currentState.priceMax,
                categories = currentState.selectedCategories,
                minRating = currentState.minRating,
                query = ""
            )

            result.onSuccess { trainers ->
                _trainersState.update {
                    it.copy(isLoading = false, trainers = trainers)
                }
            }.onFailure { error ->
                _trainersState.update {
                    it.copy(isLoading = false, errorMessage = error.message)
                }
            }
        }
    }
    fun onPriceRangeChanged(min: Int, max: Int) {
        _trainersState.update { it.copy(priceMin = min, priceMax = max) }
    }

    fun onCategorySelected(category: String) {
        _trainersState.update { currentState ->
            val newCategories = currentState.selectedCategories.toMutableSet()
            if (category in newCategories) newCategories.remove(category) else newCategories.add(category)
            currentState.copy(selectedCategories = newCategories)
        }
    }

    fun onMinRatingChanged(rating: Float) {
        _trainersState.update { it.copy(minRating = rating) }
    }

    /**
     * Funkcja do zresetowania filtrów. Można ją wywołać z FilterScreen.
     */
    fun clearFilters() {
        _trainersState.update {
            it.copy(
                priceMin = 0,
                priceMax = 500,
                selectedCategories = emptySet(),
                minRating = 0.0f
            )
        }
    }
}
