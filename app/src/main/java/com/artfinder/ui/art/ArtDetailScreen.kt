package com.artfinder.ui.art

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtDetailScreen(
    artId: Int,
    onBack: () -> Unit,
    viewModel: ArtViewModel = hiltViewModel()
) {
    val artState by viewModel.artState.collectAsState()
    
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
                    val artworks = if (state is ArtState.Success) state.artworks else (state as ArtState.LoadingMore).currentArtworks
                    val artwork = artworks.find { it.id == artId }
                    if (artwork != null) {
                        ArtDetailContent(artwork)
                    } else {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
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
fun ArtDetailContent(artwork: com.artfinder.data.model.Artwork) {
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
            DetailItem("Gallery", artwork.gallery_title ?: "N/A")
            DetailItem("Credit", artwork.credit_line ?: "N/A")
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text("Description", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                artwork.description?.replace(Regex("<[^>]*>"), "") ?: artwork.short_description ?: "No description available.",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(32.dp))
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
