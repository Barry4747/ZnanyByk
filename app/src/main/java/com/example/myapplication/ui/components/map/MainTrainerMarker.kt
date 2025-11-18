package com.example.myapplication.ui.components.map

import androidx.compose.runtime.Composable
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberMarkerState

@Composable
fun MainTrainerMarker(
    position: LatLng,
    title: String? = null,
    snippet: String? = null,
    onClick: () -> Unit = {}
) {
    val markerState = rememberMarkerState(position = position)
    Marker(
        state = markerState,
        title = title,
        snippet = snippet,
        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET),
        onClick = { marker ->
            onClick()
            true
        }
    )
}
