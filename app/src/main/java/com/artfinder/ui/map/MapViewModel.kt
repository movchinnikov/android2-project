package com.artfinder.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artfinder.data.model.Artwork
import com.artfinder.data.model.VisitedArtwork
import com.artfinder.data.repository.ArtRepository
import com.artfinder.data.repository.VisitedRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val artRepository: ArtRepository,
    private val visitedRepository: VisitedRepository
) : ViewModel() {

    private val _mapState = MutableStateFlow<MapState>(MapState.Loading)
    val mapState: StateFlow<MapState> = _mapState

    init {
        loadMapData()
    }

    private fun loadMapData() {
        viewModelScope.launch {
            try {
                val visited = visitedRepository.getVisitedArtworks()
                // In a real app, we'd fetch artworks with coordinates from the API
                // For this demo, we'll use the visited list
                _mapState.value = MapState.Success(visited)
            } catch (e: Exception) {
                _mapState.value = MapState.Error(e.message ?: "Failed to load map data")
            }
        }
    }

    fun markAsVisited(artwork: Artwork, lat: Double, lng: Double) {
        viewModelScope.launch {
            val visited = VisitedArtwork(
                artworkId = artwork.id,
                title = artwork.title,
                latitude = lat,
                longitude = lng
            )
            visitedRepository.markAsVisited(visited)
            loadMapData()
        }
    }
}

sealed class MapState {
    object Loading : MapState()
    data class Success(val visitedArtworks: List<VisitedArtwork>) : MapState()
    data class Error(val message: String) : MapState()
}
