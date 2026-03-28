package com.artfinder.ui.art

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.artfinder.data.model.Artwork

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtListScreen(
    onNavigateToDetails: (Int) -> Unit,
    galleryId: Long? = null,
    viewModel: ArtViewModel = hiltViewModel()
) {
    val artState by viewModel.combinedArtState.collectAsState()
    val galleryIdFilter by viewModel.galleryIdFilter.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(galleryId) {
        viewModel.setGalleryFilter(galleryId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (galleryIdFilter != null) "Gallery Artworks" else "Artworks") },
                navigationIcon = {
                    if (galleryIdFilter != null) {
                        IconButton(onClick = { viewModel.setGalleryFilter(null) }) {
                            Icon(androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            OutlinedTextField(
                value = searchQuery,
            onValueChange = { 
                searchQuery = it
                viewModel.search(it)
            },
            label = { Text("Search Artworks") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )

        val isOnViewOnly by viewModel.showOnlyOnView.collectAsState()
        val activeGallery by viewModel.activeGallery.collectAsState()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (galleryIdFilter != null) {
                FilterChip(
                    selected = true,
                    onClick = { viewModel.setGalleryFilter(null) },
                    label = { Text(activeGallery?.title ?: "Gallery $galleryIdFilter") },
                    trailingIcon = {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Clear Filter",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            } else {
                FilterChip(
                    selected = isOnViewOnly,
                    onClick = { viewModel.toggleShowOnlyOnView() },
                    label = { Text("On Display Only") },
                    leadingIcon = if (isOnViewOnly) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    } else null
                )
            }
        }

        when (val state = artState) {
            is ArtState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is ArtState.LoadingMore, is ArtState.Success -> {
                val artworks = if (state is ArtState.Success) state.artworks else (state as ArtState.LoadingMore).currentArtworks
                
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(artworks.size) { index ->
                        val artwork = artworks[index]
                        val isVisited = artwork.is_visited_local
                        
                        if (index >= artworks.size - 1) {
                            LaunchedEffect(Unit) {
                                viewModel.loadNextPage()
                            }
                        }
                        ArtItem(
                            artwork = artwork, 
                            isVisited = isVisited,
                            onToggleVisited = { viewModel.toggleVisited(artwork) },
                            onClick = { onNavigateToDetails(artwork.id) }
                        )
                    }
                    if (state is ArtState.LoadingMore) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
            }
            is ArtState.Error -> {
                Text(state.message, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
            }
        }
        }
    }
}

@Composable
fun ArtItem(artwork: Artwork, isVisited: Boolean, onToggleVisited: () -> Unit, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = artwork.imageUrl,
                contentDescription = artwork.title,
                modifier = Modifier.size(100.dp),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(artwork.title, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                Text(artwork.artist_display ?: "Unknown Artist", style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                Text(artwork.classification_title ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                Text(artwork.gallery_title ?: "", style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onToggleVisited) {
                Icon(
                    imageVector = if (isVisited) Icons.Default.Visibility else Icons.Outlined.Visibility,
                    contentDescription = if (isVisited) "Remove from visited list" else "Mark as visited",
                    tint = if (isVisited) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
