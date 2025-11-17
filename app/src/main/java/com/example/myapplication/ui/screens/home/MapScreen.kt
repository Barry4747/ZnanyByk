package com.example.myapplication.ui.screens.home


import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.myapplication.viewmodel.MapViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import com.example.myapplication.ui.components.MapContent
val DEFAULT_LOCATION = LatLng(51.09898, 17.03665)
const val DEFAULT_ZOOM = 17f

@Composable
fun MapScreen(
    viewModel : MapViewModel,
    onNavigateBack: () -> Unit
)  {
    val state by viewModel.state.collectAsState()
    val currentUser = state.currentUser

    val initialLocation = currentUser?.location?.let { location ->
        LatLng(location.latitude, location.longitude)
    } ?: DEFAULT_LOCATION

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialLocation, DEFAULT_ZOOM)
    }

    val properties = MapProperties()

    val uiSettings = MapUiSettings(
        zoomControlsEnabled = true,
        mapToolbarEnabled = false
    )

    MapContent(
        cameraPositionState = cameraPositionState,
        properties = properties,
        uiSettings = uiSettings
    )
}