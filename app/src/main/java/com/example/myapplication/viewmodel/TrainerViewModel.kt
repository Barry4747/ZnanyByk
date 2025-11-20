package com.example.myapplication.viewmodel

import android.app.Application

import android.util.Log

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import coil.imageLoader
import coil.request.ImageRequest
import com.example.myapplication.R
import com.example.myapplication.data.model.users.Trainer
import com.example.myapplication.data.repository.AuthRepository // <-- KROK 1: DODAJ IMPORT
import com.example.myapplication.data.repository.TrainerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SortOption(val displayName: String) {
    PRICE_ASC("Cena: Rosnąco"),
    PRICE_DESC("Cena: Malejąco"),
    RATING_DESC("Ocena: Malejąco")
}


enum class TrainerCategory(val stringResId: Int) {
    CROSSFIT(R.string.category_crossfit),
    POWERLIFTING(R.string.category_powerlifting),
    YOGA(R.string.category_yoga),
    CARDIO(R.string.category_cardio),
    PILATES(R.string.category_pilates),
    BODYBUILDING(R.string.category_bodybuilding),
    ENDURANCE(R.string.category_endurance),
    SPINNING(R.string.category_spinning),
    MARTIAL_ARTS(R.string.category_martial_arts),
    AEROBICS(R.string.category_aerobics),
    CALISTHENICS(R.string.category_calistenics),
    REHABILITATION(R.string.category_rehabilitation),
    MEDITATION(R.string.category_meditation),
    ZUMBA(R.string.category_zumba),
    AQUA_FITNESS(R.string.category_aqua_fitness),
    STRETCHING(R.string.category_stretching),
    MOBILITY(R.string.category_mobility),
    GYMNASTICS(R.string.category_gymnastics),
    TRACK_AND_FIELD(R.string.category_track_and_field),
    RUNNING(R.string.category_running),
    SWIMMING(R.string.category_swimming),
    TRIATHLON(R.string.category_triathlon),
    SPORT_SPECIFIC(R.string.category_sport_specific),
    OUTDOOR_FITNESS(R.string.category_outdoor_fitness);
}

data class TrainersState(
    val trainers: List<Trainer> = emptyList(),
    val selectedTrainer: Trainer? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val priceMin: Int = 0,
    val priceMax: Int = 8000,
    val selectedCategories: Set<String> = emptySet(),
    val minRating: Float = 0.0f,
    val sortBy: SortOption = SortOption.RATING_DESC,
    val searchQuery: String = "",
    val currentUserRating: Int = 0)

@HiltViewModel
class TrainersViewModel @Inject constructor(
    private val app: Application,
    private val trainerRepository: TrainerRepository,
    private val authRepository: AuthRepository
) : AndroidViewModel(app) {

    private val _trainersState = MutableStateFlow(TrainersState())
    val trainersState: StateFlow<TrainersState> = _trainersState.asStateFlow()


    fun loadInitialTrainers() {
        if (trainersState.value.trainers.isNotEmpty() || trainersState.value.isLoading) {
            return
        }
        applyFiltersAndLoad()
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
                preloadTrainerImages(trainers)
            }.onFailure { error ->
                _trainersState.update {
                    it.copy(isLoading = false, errorMessage = error.message)
                }
            }

            Log.d("TrainersViewModel", trainersState.value.trainers.toString())
        }
    }

    fun onSearchQueryChanged(query: String) {
        _trainersState.update { it.copy(searchQuery = query) }
    }

    fun onPriceRangeChanged(min: Int, max: Int) {
        _trainersState.update { it.copy(priceMin = min, priceMax = max) }
    }

    private fun preloadTrainerImages(trainers: List<Trainer>) {
        val imageLoader = app.imageLoader
        trainers.forEach { trainer ->
            val firstImage = trainer.images?.firstOrNull()
            if (firstImage != null) {
                val request = ImageRequest.Builder(app)
                    .data(firstImage)
                    .build()
                imageLoader.enqueue(request)
            }
        }
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

    fun onSortOptionSelected(sortOption: SortOption) {
        _trainersState.update { it.copy(sortBy = sortOption) }

        _trainersState.update { currentState ->
            val sortedList = when (sortOption) {
                SortOption.PRICE_ASC -> currentState.trainers.sortedBy { it.pricePerHour }
                SortOption.PRICE_DESC -> currentState.trainers.sortedByDescending { it.pricePerHour }
                SortOption.RATING_DESC -> currentState.trainers.sortedByDescending { it.avgRating }
            }
            currentState.copy(trainers = sortedList)
        }
    }

    fun searchTrainers(query: String) {
        _trainersState.update { it.copy(searchQuery = query) }

        viewModelScope.launch {
            try {
                val trainersList = trainerRepository.getFilteredTrainers(query).getOrThrow()
                _trainersState.update { it.copy(trainers = trainersList) }
            } catch (e: Exception) {
            } finally {
                _trainersState.update { it.copy(isLoading = false) }
            }
        }
    }


    fun selectTrainer(trainer: Trainer) {
        _trainersState.update { it.copy(selectedTrainer = trainer) }
        loadCurrentUserRating(trainer)
    }


    private fun loadCurrentUserRating(trainer: Trainer) {
        val currentUserId = authRepository.getCurrentUserId()
        val userRating = trainer.ratings?.get(currentUserId)
        _trainersState.update {
            it.copy(currentUserRating = userRating ?: 0) // Zapisz ocenę w stanie
        }
    }

    fun updateUserRating(rating: Int) {
        val trainerId = _trainersState.value.selectedTrainer?.id
        if (trainerId.isNullOrEmpty()) {
            Log.e("TrainersVM", "Błąd: Próba oceny, gdy ID trenera jest nieznane ${_trainersState.value.selectedTrainer}.")
            return
        }

        val currentUserId = authRepository.getCurrentUserId()
        if (currentUserId.isNullOrEmpty()) {
            Log.e("TrainersVM", "Błąd: Użytkownik niezalogowany próbuje ocenić.")
            return
        }

        viewModelScope.launch {
            val result = trainerRepository.updateRating(
                trainerId = trainerId, // <-- Pewny String
                userId = currentUserId,
                rating = rating
            )

            result.onSuccess {
                Log.d("TrainersVM", "Ocena pomyślnie zaktualizowana w bazie.")
                // Po sukcesie, odśwież dane trenera, aby pobrać nową średnią
                refreshSelectedTrainerData()
            }.onFailure { error ->
                _trainersState.update { it.copy(errorMessage = "Nie udało się zapisać oceny.") }
                Log.e("TrainersVM", "Błąd podczas aktualizacji oceny: ${error.message}")
            }
        }
    }


    private fun refreshSelectedTrainerData() {
        val trainerIdToRefresh = _trainersState.value.selectedTrainer?.id ?: return

        viewModelScope.launch {
            val result = trainerRepository.getTrainerById(trainerIdToRefresh)
            result.onSuccess { updatedTrainer ->
                _trainersState.update { it.copy(selectedTrainer = updatedTrainer) }
                loadCurrentUserRating(updatedTrainer)
                _trainersState.update { currentState ->
                    val updatedList = currentState.trainers.map { trainer ->
                        if (trainer.id == updatedTrainer.id) updatedTrainer else trainer
                    }
                    currentState.copy(trainers = updatedList)
                }
            }.onFailure { error ->
                _trainersState.update { it.copy(errorMessage = "Nie udało się odświeżyć danych.") }
            }
        }
    }
}
