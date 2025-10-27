package com.example.myapplication.viewmodel.profile

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.users.UserLocation
import com.example.myapplication.data.repository.AuthRepository
import com.example.myapplication.data.repository.UserRepository
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import android.util.Log

data class LocationUiState(
    val query: String = "",
    val predictions: List<AutocompletePrediction> = emptyList(),
    val selectedPlace: Place? = null,
    val isLoading: Boolean = false,
    val isSaveComplete: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class LocationOnboardingViewModel @Inject constructor(
    application: Application,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LocationUiState())
    val uiState = _uiState.asStateFlow()

    private val placesClient: PlacesClient = Places.createClient(application)
    private var token: AutocompleteSessionToken = AutocompleteSessionToken.newInstance()

    private var searchJob: Job? = null

    fun onQueryChanged(newQuery: String) {
        _uiState.update { it.copy(query = newQuery, selectedPlace = null) }

        searchJob?.cancel()

        searchJob = viewModelScope.launch {
            delay(300L)
            if (newQuery.length > 2) {
                fetchPredictions(newQuery)
            } else {
                _uiState.update { it.copy(predictions = emptyList()) }
            }
        }
    }

    private suspend fun fetchPredictions(query: String) {
        try {
            val request = FindAutocompletePredictionsRequest.builder()
                .setSessionToken(token)
                .setQuery(query)
                .build()

            val response = placesClient.findAutocompletePredictions(request).await()
            _uiState.update { it.copy(predictions = response.autocompletePredictions) }

        } catch (e: Exception) {
            Log.e("PlacesError", "Failed to fetch predictions", e)
            _uiState.update { it.copy(error = "Could not load suggestions") }
        }
    }

    fun onPredictionSelected(prediction: AutocompletePrediction) {
        _uiState.update {
            it.copy(
                query = prediction.getPrimaryText(null).toString(),
                predictions = emptyList(),
                isLoading = true
            )
        }

        val placeId = prediction.placeId
        val placeFields = listOf(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.LAT_LNG,
            Place.Field.ADDRESS,
            Place.Field.ADDRESS_COMPONENTS
        )

        val request = FetchPlaceRequest.builder(placeId, placeFields).build()

        viewModelScope.launch {
            try {
                val response = placesClient.fetchPlace(request).await()
                _uiState.update { it.copy(selectedPlace = response.place, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Could not fetch place details", isLoading = false) }
            }
        }
    }

    fun onSaveLocation() {
        val place = _uiState.value.selectedPlace ?: return
        val userId = authRepository.getCachedUser()?.first

        if (userId == null) {
            _uiState.update { it.copy(error = "User not logged in.") }
            return
        }

        _uiState.update { it.copy(isLoading = true) }

        val userLocation = parsePlaceToUserLocation(place)

        viewModelScope.launch {
            val result = userRepository.updateUserLocation(userId, userLocation)

            if (result.isSuccess) {
                _uiState.update { it.copy(isLoading = false, isSaveComplete = true) }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message ?: "Failed to save location"
                    )
                }
            }
        }
    }

    private fun parsePlaceToUserLocation(place: Place): UserLocation {
        var city: String? = null
        var postalCode: String? = null
        var country: String? = null

        place.addressComponents?.asList()?.forEach { component ->
            when {
                component.types.contains("locality") -> city = component.name
                component.types.contains("postal_code") -> postalCode = component.name
                component.types.contains("country") -> country = component.shortName
            }
        }

        return UserLocation(
            latitude = place.latLng?.latitude ?: 0.0,
            longitude = place.latLng?.longitude ?: 0.0,
            fullAddress = place.address ?: "",
            city = city,
            postalCode = postalCode,
            country = country
        )
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
