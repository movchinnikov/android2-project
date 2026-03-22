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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Collections
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File
import android.net.Uri
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

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
    
    val visitedArtworks by viewModel.visitedArtworks.collectAsState()
    val publicVisits by viewModel.publicVisits.collectAsState()
    val visit = remember(visitedArtworks, artId) { visitedArtworks.find { it.id == artId } }
    val myPhotos = remember(visit) { visit?.imageUrls ?: emptyList() }
    val currentUserId = remember { viewModel.getCurrentUserId() }
    
    val context = LocalContext.current
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    // Photo URI for Camera
    val photoUri = remember {
        val file = File(context.cacheDir, "temp_photo_${artId}.jpg")
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                viewModel.uploadVisitPhoto(artId, photoUri)
                showBottomSheet = false
            }
        }
    )

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                viewModel.uploadVisitPhoto(artId, it)
                showBottomSheet = false
            }
        }
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                cameraLauncher.launch(photoUri)
            }
        }
    )

    fun checkAndLaunchCamera() {
        val permissionCheckResult = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
        if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
            cameraLauncher.launch(photoUri)
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

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
        },
        floatingActionButton = {
            if (isVisited) {
                ExtendedFloatingActionButton(
                    onClick = { showBottomSheet = true },
                    icon = { Icon(Icons.Default.AddAPhoto, contentDescription = null) },
                    text = { Text("Add Visit Photo") }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            when (val state = artState) {
                is ArtState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is ArtState.LoadingMore, is ArtState.Success -> {
                    artworkDetails?.let { artwork ->
                        ArtDetailContent(
                            artwork = artwork, 
                            isVisited = isVisited,
                            myPhotos = myPhotos,
                            publicVisits = publicVisits,
                            currentUserId = currentUserId,
                            onDeletePhoto = { url -> viewModel.deleteVisitPhoto(artId, url) },
                            showMap = !showBottomSheet
                        )
                    } ?: CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is ArtState.Error -> {
                    Text(state.message, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
                }
            }

            if (showBottomSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showBottomSheet = false },
                    sheetState = sheetState
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 32.dp, start = 16.dp, end = 16.dp)
                    ) {
                        Text(
                            "Add Photo of your Visit",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        ListItem(
                            headlineContent = { Text("Take Photo") },
                            leadingContent = { Icon(Icons.Default.AddAPhoto, contentDescription = null) },
                            modifier = Modifier.clickable { checkAndLaunchCamera() }
                        )
                        ListItem(
                            headlineContent = { Text("Choose from Gallery") },
                            leadingContent = { Icon(Icons.Default.Collections, contentDescription = null) },
                            modifier = Modifier.clickable { galleryLauncher.launch("image/*") }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ArtDetailContent(
    artwork: com.artfinder.data.model.Artwork, 
    isVisited: Boolean,
    myPhotos: List<String> = emptyList(),
    publicVisits: List<com.artfinder.data.model.VisitedArtwork> = emptyList(),
    currentUserId: String? = null,
    onDeletePhoto: (String) -> Unit = {},
    showMap: Boolean = true
) {
    val cleanedDescription = remember(artwork.description) {
        artwork.description?.replace(Regex("<[^>]*>"), "") ?: "No description available."
    }

    androidx.compose.foundation.lazy.LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            AsyncImage(
                model = artwork.imageUrl,
                contentDescription = artwork.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp),
                contentScale = ContentScale.Fit
            )
        }

        item {
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
                Text(cleanedDescription, style = MaterialTheme.typography.bodyMedium)
                
                if (myPhotos.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Your Visit Photos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(myPhotos, key = { it }) { url ->
                            Box(modifier = Modifier.size(150.dp)) {
                                AsyncImage(
                                    model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                                        .data(url)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Visit photo",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(MaterialTheme.shapes.medium),
                                    contentScale = ContentScale.Crop
                                )
                                IconButton(
                                    onClick = { onDeletePhoto(url) },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f), MaterialTheme.shapes.small)
                                        .size(32.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }

                // Community Gallery
                val otherVisits = publicVisits.filter { it.userId != currentUserId && it.imageUrls.isNotEmpty() }
                if (otherVisits.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Community Photos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        otherVisits.forEach { visitDoc ->
                            items(visitDoc.imageUrls, key = { "${visitDoc.userId}_$it" }) { url ->
                                Box(modifier = Modifier.size(150.dp)) {
                                    AsyncImage(
                                        model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                                            .data(url)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "Community photo",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(MaterialTheme.shapes.medium),
                                        contentScale = ContentScale.Crop
                                    )
                                    Surface(
                                        modifier = Modifier.align(Alignment.BottomStart).padding(4.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                                        shape = MaterialTheme.shapes.extraSmall
                                    ) {
                                        Text(
                                            visitDoc.userName.ifEmpty { "Visitor" },
                                            style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                if (artwork.is_on_view) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Location on Map", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (showMap) {
                        ArtLocationMap(artwork, isVisited)
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Map suspended to save power", style = MaterialTheme.typography.bodySmall)
                        }
                    }
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
            }
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
    val lat = remember(artwork.latitude) { artwork.latitude ?: 41.8796 }
    val lng = remember(artwork.longitude) { artwork.longitude ?: -87.6237 }
    val location = remember(lat, lng) { LatLng(lat, lng) }
    
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
