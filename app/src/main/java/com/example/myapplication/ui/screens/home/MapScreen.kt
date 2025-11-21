package com.example.myapplication.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.model.gyms.GymLocation
import com.example.myapplication.data.model.users.Trainer
import com.example.myapplication.ui.components.map.MainTrainerMarker
import com.example.myapplication.ui.components.map.TrainerInfoDialog
import com.example.myapplication.viewmodel.MapViewModel
import com.google.android.gms.maps.CameraUpdateFactory
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
            "featureType": "all",
            "stylers": [
                {
                    "saturation": 0
                },
                {
                    "hue": "#e7ecf0"
                }
            ]
        },
        {
            "featureType": "road",
            "stylers": [
                {
                    "saturation": -70
                }
            ]
        },
        {
            "featureType": "transit",
            "stylers": [
                {
                    "visibility": "off"
                }
            ]
        },
        {
            "featureType": "poi",
            "stylers": [
                {
                    "visibility": "off"
                }
            ]
        },
        {
            "featureType": "water",
            "stylers": [
                {
                    "visibility": "simplified"
                },
                {
                    "saturation": -60
                }
            ]
        }
    ]
"""

@Composable
fun MapScreen(
    viewModel: MapViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToTrainer: (Trainer) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val currentUser = state.currentUser

    var trainersWithGyms by remember { mutableStateOf<List<Pair<Trainer, GymLocation>>>(emptyList()) }
    var selectedTrainer by remember { mutableStateOf<Trainer?>(null) }

    LaunchedEffect(Unit) {
        trainersWithGyms = viewModel.getTrainersWithGymLocations()
    }

    val initialLocation = currentUser?.location?.let {
        LatLng(it.latitude, it.longitude)
    } ?: DEFAULT_LOCATION

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialLocation, DEFAULT_ZOOM)
    }

    LaunchedEffect(selectedTrainer) {
        selectedTrainer?.let { trainer ->
            val gym = trainersWithGyms.find { it.first == trainer }?.second
            gym?.let {
                cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), 15f))
            }
        }
    }

    val properties = MapProperties(
        isMyLocationEnabled = false,
        mapStyleOptions = MapStyleOptions(MAP_STYLE_JSON)
    )

    val uiSettings = MapUiSettings(
        zoomControlsEnabled = true,
        mapToolbarEnabled = false
    )

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = properties,
            uiSettings = uiSettings
        ) {
            trainersWithGyms.forEach { (trainer, gymLocation) ->
                MainTrainerMarker(
                    position = LatLng(gymLocation.latitude, gymLocation.longitude),
                    title = "${trainer.firstName} ${trainer.lastName}",
                    snippet = trainer.description ?: "",
                    onClick = { selectedTrainer = trainer }
                )
            }
        }

        FloatingActionButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp),
            containerColor = Color.White,
            contentColor = Color.Black
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back"
            )
        }
    }

    TrainerInfoDialog(
        trainer = selectedTrainer,
        onSeeMore = {
            selectedTrainer?.let { trainer ->
                onNavigateToTrainer(trainer)
            }},
        onDismiss = { selectedTrainer = null }
    )
}
