package com.artfinder.ui.map

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.artfinder.ui.art.ArtState
import com.artfinder.ui.art.ArtViewModel
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onNavigateToDetail: (Int) -> Unit,
    onNavigateToGallery: (Int) -> Unit,
    viewModel: ArtViewModel = hiltViewModel()
) {
    val artState by viewModel.combinedArtState.collectAsState()
    val context = LocalContext.current
    
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasLocationPermission = granted }
    )

    val galleries by viewModel.galleries.collectAsState()
    
    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        viewModel.loadGalleries()
    }

    val aicLocation = LatLng(41.8796, -87.6237)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(aicLocation, 14f)
    }

    val galleryThumbnails by viewModel.galleryThumbnails.collectAsState()
    var selectedCluster by remember { mutableStateOf<List<com.artfinder.data.api.GalleryData>?>(null) }
    val sheetState = rememberModalBottomSheetState()

    // Group galleries by approximate coordinates
    val groupedGalleries = galleries.filter { it.latitude != null && it.longitude != null }
        .groupBy { 
            val lat = ((it.latitude ?: 0.0) * 3000).toInt() / 3000.0
            val lng = ((it.longitude ?: 0.0) * 3000).toInt() / 3000.0
            LatLng(lat, lng) 
        }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = hasLocationPermission),
            uiSettings = MapUiSettings(myLocationButtonEnabled = hasLocationPermission)
        ) {
            // High zIndex for artwork markers
            if (artState is ArtState.Success) {
                (artState as ArtState.Success).artworks.forEach { artwork ->
                    val lat = artwork.latitude ?: 41.8796
                    val lng = artwork.longitude ?: -87.6237
                    val isVisited = artwork.is_visited_local
                    
                    Marker(
                        state = MarkerState(position = LatLng(lat, lng)),
                        title = artwork.title,
                        snippet = artwork.gallery_title ?: "Art Institute of Chicago",
                        zIndex = 1f,
                        icon = if (isVisited) {
                            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                        } else {
                            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                        },
                        onClick = {
                            onNavigateToDetail(artwork.id)
                            true
                        }
                    )
                }
            }

            // Grouped Gallery Markers
            groupedGalleries.forEach { (position, cluster) ->
                Marker(
                    state = MarkerState(position = position),
                    title = if (cluster.size > 1) "${cluster.size} Galleries" else cluster[0].title,
                    snippet = if (cluster.size > 1) "Click to see all" else "View artworks",
                    zIndex = 2f,
                    icon = BitmapDescriptorFactory.defaultMarker(
                        if (cluster.size > 1) BitmapDescriptorFactory.HUE_VIOLET else BitmapDescriptorFactory.HUE_AZURE
                    ),
                    onClick = {
                        selectedCluster = cluster
                        cluster.forEach { viewModel.loadThumbnailForGallery(it.id) }
                        true
                    }
                )
            }
        }

        val currentCluster = selectedCluster
        if (currentCluster != null) {
            ModalBottomSheet(
                onDismissRequest = { selectedCluster = null },
                sheetState = sheetState
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(
                        text = if (currentCluster.size > 1) "Galleries at this location" else currentCluster[0].title,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    LazyColumn(
                        modifier = Modifier.fillMaxHeight(0.6f)
                    ) {
                        items(currentCluster) { gallery ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                onClick = { 
                                    onNavigateToGallery(gallery.id.toInt())
                                    selectedCluster = null
                                }
                            ) {
                                Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    val imageId = galleryThumbnails[gallery.id]
                                    if (imageId != null) {
                                        AsyncImage(
                                            model = "https://www.artic.edu/iiif/2/$imageId/full/200,/0/default.jpg",
                                            contentDescription = null,
                                            modifier = Modifier.size(60.dp),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Box(modifier = Modifier.size(60.dp).background(MaterialTheme.colorScheme.surfaceVariant))
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text(gallery.title, style = MaterialTheme.typography.titleMedium)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (!hasLocationPermission) {
            Button(
                onClick = { permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) },
                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
            ) {
                Text("Enable My Location")
            }
        }
    }
}
