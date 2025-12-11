package com.example.myapplication.viewmodel.trainer

import android.app.Application
import android.location.Location
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import coil.imageLoader
import coil.request.ImageRequest
import com.example.myapplication.R
import com.example.myapplication.data.model.gyms.Gym
import com.example.myapplication.data.model.gyms.GymLocation
import com.example.myapplication.data.model.users.Trainer
import com.example.myapplication.data.model.users.UserLocation
import com.example.myapplication.data.repository.AuthRepository
import com.example.myapplication.data.repository.GymRepository
import com.example.myapplication.data.repository.TrainerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.round

enum class SortOption(val displayName: String) {
    PRICE_ASC("Cena: Rosnąco"),
    PRICE_DESC("Cena: Malejąco"),
    RATING_DESC("Ocena: Malejąco"),
    DISTANCE_ASC("Odległość: Najbliżej")
}

enum class SuggestionType {
    TRAINER,
    GYM
}

data class SearchSuggestion(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val type: SuggestionType
)

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
    val priceMax: Int = 1000,
    val maxPriceFromTrainers: Int = 1000,
    val selectedCategories: Set<String> = emptySet(),
    val minRating: Float = 0.0f,
    val sortBy: SortOption = SortOption.RATING_DESC,
    val searchQuery: String = "",
    val currentUserRating: Int = 0,
    var userLocation: UserLocation? = null,
    var distanceToTrainer: Double? = null,
    val suggestions: List<SearchSuggestion> = emptyList(),
    var firstLoad: Boolean = true,
    val gymsById: Map<String, Gym> = emptyMap()
)

@HiltViewModel
class TrainersViewModel @Inject constructor(
    private val app: Application,
    private val trainerRepository: TrainerRepository,
    private val authRepository: AuthRepository,
    private val gymRepository: GymRepository
) : AndroidViewModel(app) {

    private val _trainersState = MutableStateFlow(TrainersState())
    val trainersState: StateFlow<TrainersState> = _trainersState.asStateFlow()

    init {
        loadCurrentUserLocation()
    }

    fun loadInitialTrainers() {
        if (!trainersState.value.firstLoad || trainersState.value.isLoading) return

        val state = _trainersState.value
        if (state.searchQuery.isNotEmpty() || state.selectedCategories.isNotEmpty() || state.priceMax < state.maxPriceFromTrainers || state.minRating > 0 || state.priceMin > 0) {
            applyFiltersAndLoad()
            return
        }

        viewModelScope.launch {
            _trainersState.update { it.copy(isLoading = true) }

            val allTrainersResult = trainerRepository.getAllTrainers()
            allTrainersResult.onSuccess { allTrainers ->
                val maxPrice = allTrainers.maxOfOrNull { it.pricePerHour ?: 0 } ?: 1000
                val gymsMap = fetchGymsForTrainers(allTrainers)

                _trainersState.update {
                    it.copy(
                        isLoading = false,
                        firstLoad = false,
                        trainers = allTrainers,
                        gymsById = gymsMap,
                        maxPriceFromTrainers = maxPrice,
                        priceMax = maxPrice
                    )
                }
                preloadTrainerImages(allTrainers)
            }.onFailure { error ->
                _trainersState.update { it.copy(isLoading = false, firstLoad = false, errorMessage = error.message) }
            }
        }
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
                query = currentState.searchQuery
            )

            result.onSuccess { filteredTrainers ->
                val gymsMap = fetchGymsForTrainers(filteredTrainers)
                _trainersState.update {
                    it.copy(
                        isLoading = false,
                        firstLoad = false,
                        trainers = filteredTrainers,
                        gymsById = gymsMap
                    )
                }
                preloadTrainerImages(filteredTrainers)
            }.onFailure { error ->
                _trainersState.update {
                    it.copy(isLoading = false, firstLoad = false, errorMessage = error.message)
                }
            }
        }
    }

    private suspend fun fetchGymsForTrainers(trainers: List<Trainer>): Map<String, Gym> {
        val gymIds = trainers.mapNotNull { it.gymId }.distinct()
        return if (gymIds.isNotEmpty()) {
            val gymsResult = gymRepository.getGymsByIds(gymIds)
            gymsResult.getOrNull() ?: emptyMap()
        } else {
            emptyMap()
        }
    }

    fun onSearchQueryChanged(query: String) {
        _trainersState.update { it.copy(searchQuery = query) }
        updateSearchSuggestions(query)
    }
    
    fun updateSearchSuggestions(query: String) {
        if (query.length < 2) {
             _trainersState.update { it.copy(suggestions = emptyList()) }
             return
        }
        viewModelScope.launch {
             val trainersDeferred = async { trainerRepository.getFilteredTrainers(query) }
             val gymsDeferred = async { gymRepository.searchGyms(query) } 
             
             val trainersResult = trainersDeferred.await()
             val gymsResult = gymsDeferred.await()
             
             val trainers = trainersResult.getOrNull() ?: emptyList()
             val gyms = gymsResult.getOrNull() ?: emptyList()
             
             val suggestions = mutableListOf<SearchSuggestion>()
             suggestions.addAll(trainers.map { SearchSuggestion(it.id ?: "", "${it.firstName} ${it.lastName}", "Trener", SuggestionType.TRAINER) })
             suggestions.addAll(gyms.map { SearchSuggestion(it.id, it.gymName, it.gymLocation.shortFormattedAddress ?: "Siłownia", SuggestionType.GYM) })
             
             _trainersState.update { it.copy(suggestions = suggestions) }
        }
    }

    fun clearSuggestions() {
        _trainersState.update { it.copy(suggestions = emptyList()) }
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
        _trainersState.update { state ->
            state.copy(
                priceMin = 0,
                priceMax = state.maxPriceFromTrainers,
                selectedCategories = emptySet(),
                minRating = 0.0f,
                searchQuery = ""
            )
        }
        applyFiltersAndLoad()
    }

    fun getGymOfTrainer(trainer: Trainer): Gym? {
        Log.d("GYMS", "Gym IDs: ${_trainersState.value.gymsById}")
        return trainer.gymId?.let { _trainersState.value.gymsById[it] }
    }

    fun onSortOptionSelected(sortOption: SortOption) {
        _trainersState.update { it.copy(sortBy = sortOption) }

        viewModelScope.launch {
            if (sortOption == SortOption.DISTANCE_ASC) {
                loadCurrentUserLocation()
                val currentState = _trainersState.value
                val userLoc = currentState.userLocation

                if (userLoc != null && userLoc.latitude != 0.0) {
                    _trainersState.update { it.copy(isLoading = true) }

                    val trainersWithDistances = currentState.trainers.map { trainer ->
                        val gym = getGymOfTrainer(trainer)
                        val distance = gym?.gymLocation?.let {
                            calculateDistanceInKm(
                                userLoc.latitude, userLoc.longitude,
                                it.latitude, it.longitude
                            )
                        } ?: Double.MAX_VALUE
                        trainer to distance
                    }

                    val sortedList = trainersWithDistances.sortedBy { it.second }.map { it.first }

                    _trainersState.update { it.copy(trainers = sortedList, isLoading = false) }
                } else {
                    Log.d("SORT", "Brak lokalizacji użytkownika, nie można posortować po dystansie.")
                }
            } else {
                _trainersState.update { currentState ->
                    val sortedList = when (sortOption) {
                        SortOption.PRICE_ASC -> currentState.trainers.sortedBy { it.pricePerHour }
                        SortOption.PRICE_DESC -> currentState.trainers.sortedByDescending { it.pricePerHour }
                        SortOption.RATING_DESC -> currentState.trainers.sortedByDescending { it.avgRating }
                        else -> currentState.trainers
                    }
                    currentState.copy(trainers = sortedList)
                }
            }
        }
    }

    fun searchTrainers(query: String) {
        _trainersState.update { it.copy(searchQuery = query) }

        applyFiltersAndLoad()
    }

    fun selectTrainer(trainer: Trainer) {
        _trainersState.update { it.copy(selectedTrainer = trainer) }
        loadCurrentUserRating(trainer)
        viewModelScope.launch {
            calculateDistanceToSelectedTrainer()
        }
    }

    private fun loadCurrentUserRating(trainer: Trainer) {
        val currentUserId = authRepository.getCurrentUserId()
        val userRating = trainer.ratings?.get(currentUserId)
        _trainersState.update {
            it.copy(currentUserRating = userRating ?: 0)
        }
    }

    private fun loadCurrentUserLocation() {
        val currentLocation = authRepository.getCurrentLocation()
        _trainersState.update {
            it.copy(userLocation = currentLocation)
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

    suspend fun getSelectedTrainerGymLocation(): GymLocation? {
        val selectedTrainer = _trainersState.value.selectedTrainer ?: return null
        val gymId = selectedTrainer.gymId ?: return null
        return gymRepository.getGymById(gymId).getOrNull()?.gymLocation
    }

    fun calculateDistanceInKm(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double? {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) {
            return null
        }
        if (lat1 == 0.0 && lon1 == 0.0) return null
        if (lat2 == 0.0 && lon2 == 0.0) return null

        loadCurrentUserLocation()
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        return results[0] / 1000.0
    }

    suspend fun calculateDistanceToSelectedTrainer() {
        loadCurrentUserLocation()
        val userLoc = _trainersState.value.userLocation
        Log.d("DIST", "User location: $userLoc")
        val gymLoc = getSelectedTrainerGymLocation()

        if (userLoc != null && gymLoc != null && userLoc.latitude != 0.0 && userLoc.longitude != 0.0) {
            var dist =  calculateDistanceInKm(
                gymLoc.latitude,
                gymLoc.longitude,
                userLoc.latitude,
                userLoc.longitude
            )
            dist = round(dist?.times(10.0) ?: 0.0) / 10.0
            Log.d("DIST", "Obliczony dystans: $dist km")
            
            _trainersState.update { it.copy(distanceToTrainer = dist) }
        } else {
            Log.d("DIST", "Nie można obliczyć dystansu. Brak lokalizacji użytkownika lub siłowni.")
            _trainersState.update { it.copy(distanceToTrainer = null) }
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
                trainerId = trainerId,
                userId = currentUserId,
                rating = rating
            )

            result.onSuccess {
                Log.d("TrainersVM", "Ocena pomyślnie zaktualizowana w bazie.")
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

    fun isVideoUrl(url: String): Boolean {
        return url.contains(".mp4", ignoreCase = true)
    }
    
    fun onSuggestionClicked(suggestion: SearchSuggestion) {
        if (suggestion.type == SuggestionType.TRAINER) {
             viewModelScope.launch {
                 searchTrainers(suggestion.title)
                 clearSuggestions()
                 _trainersState.update { it.copy(searchQuery = suggestion.title) }
             }
        } else if (suggestion.type == SuggestionType.GYM) {
             viewModelScope.launch {
                 val allTrainers = trainerRepository.getAllTrainers().getOrNull() ?: emptyList()
                 val filtered = allTrainers.filter { it.gymId == suggestion.id }
                 _trainersState.update { it.copy(trainers = filtered, searchQuery = suggestion.title) }
                 clearSuggestions()
             }
        }
    }
}
