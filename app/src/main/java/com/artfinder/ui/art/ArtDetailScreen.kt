package com.artfinder.ui.art

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtDetailScreen(
    artId: Int,
    onBack: () -> Unit,
    viewModel: ArtViewModel = hiltViewModel()
) {
    val artState by viewModel.artState.collectAsState()
    val isVisited by viewModel.isVisited.collectAsState()
    val artworkDetails by viewModel.currentArtworkDetails.collectAsState()
    
    LaunchedEffect(artId) {
        viewModel.getArtDetail(artId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Artwork Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    artworkDetails?.let { artwork ->
                        IconButton(onClick = { viewModel.toggleVisited(artwork) }) {
                            Icon(
                                Icons.Default.Visibility,
                                contentDescription = "Toggle Visited",
                                tint = if (isVisited) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            when (val state = artState) {
                is ArtState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is ArtState.LoadingMore, is ArtState.Success -> {
                    artworkDetails?.let { artwork ->
                        ArtDetailContent(artwork, isVisited)
                    } ?: CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is ArtState.Error -> {
                    Text(state.message, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
                }
            }
        }
    }
}

@Composable
fun ArtDetailContent(artwork: com.artfinder.data.model.Artwork, isVisited: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        AsyncImage(
            model = artwork.imageUrl,
            contentDescription = artwork.title,
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp),
            contentScale = ContentScale.Fit
        )
        
        Column(modifier = Modifier.padding(16.dp)) {
            Text(artwork.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(artwork.artist_display ?: "Unknown Artist", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.secondary)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            DetailItem("Type", artwork.classification_title ?: "N/A")
            DetailItem("Date", artwork.date_display ?: "N/A")
            DetailItem("Origin", artwork.place_of_origin ?: "N/A")
            DetailItem("Medium", artwork.medium_display ?: "N/A")
            DetailItem("Dimensions", artwork.dimensions ?: "N/A")
            DetailItem("Gallery", if (artwork.is_on_view) (artwork.gallery_title ?: "N/A") else "Not on Display")
            DetailItem("Credit", artwork.credit_line ?: "N/A")
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text("Description", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                artwork.description?.replace(Regex("<[^>]*>"), "") ?: "No description available.",
                style = MaterialTheme.typography.bodyMedium
            )
            
            if (artwork.is_on_view) {
                Spacer(modifier = Modifier.height(24.dp))
                
                Text("Location on Map", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                
                ArtLocationMap(artwork, isVisited)
            } else {
                Spacer(modifier = Modifier.height(24.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "This artwork is currently not on display in the gallery.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun ArtLocationMap(artwork: com.artfinder.data.model.Artwork, isVisited: Boolean) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var hasLocationPermission by remember {
        mutableStateOf(
            androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasLocationPermission = granted }
    )

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // Default to Art Institute of Chicago if no specific coords
    val lat = artwork.latitude ?: 41.8796
    val lng = artwork.longitude ?: -87.6237
    val location = LatLng(lat, lng)
    
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(location, 15f)
    }

    // Update camera if location changes
    LaunchedEffect(location) {
        cameraPositionState.position = CameraPosition.fromLatLngZoom(location, 15f)
    }

    Box(modifier = Modifier.fillMaxWidth().height(250.dp)) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = hasLocationPermission),
            uiSettings = MapUiSettings(myLocationButtonEnabled = hasLocationPermission)
        ) {
            Marker(
                state = MarkerState(position = location),
                title = artwork.title,
                snippet = artwork.gallery_title ?: "Art Institute of Chicago",
                icon = if (isVisited) {
                    BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                } else {
                    BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                }
            )
        }
    }
}

@Composable
fun DetailItem(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        Text("$label: ", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}
