package com.artfinder.ui.art

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.artfinder.data.model.Artwork

@Composable
fun ArtListScreen(
    onNavigateToDetails: (Int) -> Unit,
    viewModel: ArtViewModel = hiltViewModel()
) {
    val artState by viewModel.artState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
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
                        if (index >= artworks.size - 1) {
                            LaunchedEffect(Unit) {
                                viewModel.loadNextPage()
                            }
                        }
                        ArtItem(artwork = artwork, onClick = { onNavigateToDetails(artwork.id) })
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

@Composable
fun ArtItem(artwork: Artwork, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(modifier = Modifier.padding(8.dp)) {
            AsyncImage(
                model = artwork.imageUrl,
                contentDescription = artwork.title,
                modifier = Modifier.size(100.dp),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(artwork.title, style = MaterialTheme.typography.titleMedium)
                Text(artwork.artist_display ?: "Unknown Artist", style = MaterialTheme.typography.bodyMedium)
                Text(artwork.classification_title ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                Text(artwork.gallery_title ?: "", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
