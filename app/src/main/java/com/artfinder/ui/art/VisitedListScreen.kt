package com.artfinder.ui.art

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.artfinder.data.model.VisitedArtwork
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisitedListScreen(
    onNavigateToDetail: (Int) -> Unit,
    viewModel: ArtViewModel = hiltViewModel()
) {
    val visitedArtworks by viewModel.visitedArtworks.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadVisitedArtworks()
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Visited Artworks") }) }
    ) { innerPadding ->
        if (visitedArtworks.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.List, 
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No visited artworks yet. Explore and mark them!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                items(visitedArtworks) { artwork ->
                    VisitedArtItem(
                        artwork = artwork,
                        onDelete = { viewModel.removeVisited(artwork.id) },
                        onClick = { onNavigateToDetail(artwork.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun VisitedArtItem(
    artwork: VisitedArtwork,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    val dateStr = artwork.visitedAt?.let { dateFormat.format(it.toDate()) } ?: "Unknown"

    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp).clickable { onClick() }
    ) {
        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = "https://www.artic.edu/iiif/2/${artwork.imageId}/full/200,/0/default.jpg",
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(artwork.title, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                Text(artwork.artist ?: "Unknown Artist", style = MaterialTheme.typography.bodySmall)
                Text("Visited: $dateStr", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
