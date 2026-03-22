package com.artfinder.ui.map

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.artfinder.data.model.VisitedArtwork
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@Composable
fun MapScreen(
    viewModel: MapViewModel = hiltViewModel()
) {
    val mapState by viewModel.mapState.collectAsState()
    val chicago = LatLng(41.8796, -87.6237)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(chicago, 12f)
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ) {
        if (mapState is MapState.Success) {
            val visited = (mapState as MapState.Success).visitedArtworks
            visited.forEach { art ->
                Marker(
                    state = MarkerState(position = LatLng(art.latitude, art.longitude)),
                    title = art.title,
                    snippet = "Visited on: ${art.visitDate}",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                )
            }
        }
    }
}
