package com.example.myapplication.ui.screens.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.myapplication.data.model.gyms.GymLocation
import com.example.myapplication.data.model.users.Trainer
import com.example.myapplication.ui.components.BlackMarker
import com.example.myapplication.viewmodel.MapViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState

val DEFAULT_LOCATION = LatLng(51.09898, 17.03665)
const val DEFAULT_ZOOM = 17f

const val MAP_STYLE_JSON = """
    [
      {
        "featureType": "poi",
        "elementType": "labels",
        "stylers": [
          { "visibility": "off" }
        ]
      }
    ]
"""

@Composable
fun MapScreen(
    viewModel: MapViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val currentUser = state.currentUser

    var trainersWithGyms by remember { mutableStateOf<List<Pair<Trainer, GymLocation>>>(emptyList()) }

    LaunchedEffect(Unit) {
        trainersWithGyms = viewModel.getTrainersWithGymLocations()
    }

    val initialLocation = currentUser?.location?.let {
        LatLng(it.latitude, it.longitude)
    } ?: DEFAULT_LOCATION

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialLocation, DEFAULT_ZOOM)
    }

    val properties = MapProperties(
        isMyLocationEnabled = false,
        mapStyleOptions = MapStyleOptions(MAP_STYLE_JSON)
    )

    val uiSettings = MapUiSettings(
        zoomControlsEnabled = true,
        mapToolbarEnabled = false
    )

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = properties,
        uiSettings = uiSettings
    ) {
        trainersWithGyms.forEach { (trainer, gymLocation) ->
            BlackMarker(
                position = LatLng(gymLocation.latitude, gymLocation.longitude),
                title = "${trainer.firstName} ${trainer.lastName}",
                snippet = trainer.description ?: ""
            )
        }
    }
}
