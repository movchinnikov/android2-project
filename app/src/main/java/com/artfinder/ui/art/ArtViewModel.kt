package com.artfinder.ui.art

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artfinder.data.model.Artwork
import com.artfinder.data.model.VisitedArtwork
import com.artfinder.data.repository.ArtRepository
import com.artfinder.data.repository.VisitedRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArtViewModel @Inject constructor(
    private val artRepository: ArtRepository,
    private val visitedRepository: VisitedRepository
) : ViewModel() {

    private val _artState = MutableStateFlow<ArtState>(ArtState.Loading)
    val artState: StateFlow<ArtState> = _artState

    private val _visitedArtworks = MutableStateFlow<List<VisitedArtwork>>(emptyList())
    val visitedArtworks: StateFlow<List<VisitedArtwork>> = _visitedArtworks

    private val _currentArtworkDetails = MutableStateFlow<Artwork?>(null)
    val currentArtworkDetails: StateFlow<Artwork?> = _currentArtworkDetails

    private val _isVisited = MutableStateFlow(false)
    val isVisited: StateFlow<Boolean> = _isVisited

    private val _showOnlyOnView = MutableStateFlow(true)
    val showOnlyOnView: StateFlow<Boolean> = _showOnlyOnView

    private val _galleries = MutableStateFlow<List<com.artfinder.data.api.GalleryData>>(emptyList())
    val galleries: StateFlow<List<com.artfinder.data.api.GalleryData>> = _galleries

    private val _galleryIdFilter = MutableStateFlow<Long?>(null)
    val galleryIdFilter: StateFlow<Long?> = _galleryIdFilter

    private val _activeGallery = MutableStateFlow<com.artfinder.data.api.GalleryData?>(null)
    val activeGallery: StateFlow<com.artfinder.data.api.GalleryData?> = _activeGallery

    private val _galleryThumbnails = MutableStateFlow<Map<Long, String?>>(emptyMap())
    val galleryThumbnails: StateFlow<Map<Long, String?>> = _galleryThumbnails

    private var currentPage = 1
    private var allArtworks = mutableListOf<Artwork>()
    private var isEndReached = false
    private var isSearching = false
    private var lastQuery = ""
    private var loadingJob: kotlinx.coroutines.Job? = null

    init {
        loadNextPage()
        loadVisitedArtworks()
    }

    fun loadVisitedArtworks() {
        viewModelScope.launch {
            try {
                _visitedArtworks.value = visitedRepository.getVisits()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun loadNextPage() {
        if (_artState.value is ArtState.LoadingMore || isEndReached) return
        
        loadingJob = viewModelScope.launch {
            if (allArtworks.isEmpty()) {
                _artState.value = ArtState.Loading
            } else {
                _artState.value = ArtState.LoadingMore(allArtworks)
            }
            
            try {
                val isOnView = if (_galleryIdFilter.value != null) null else (if (_showOnlyOnView.value) true else null)
                val galleryId = _galleryIdFilter.value
                val response = if (isSearching || isOnView == true || galleryId != null) {
                    artRepository.searchArtworks(
                        query = if (isSearching) lastQuery else null,
                        page = currentPage,
                        isOnView = isOnView,
                        galleryId = galleryId
                    )
                } else {
                    artRepository.getArtworks(currentPage, isOnView)
                }
                
                if (response.data.isEmpty()) {
                    isEndReached = true
                } else {
                    allArtworks.addAll(response.data)
                    currentPage++
                }
                _artState.value = ArtState.Success(allArtworks.toList())
            } catch (e: Exception) {
                _artState.value = ArtState.Error(e.message ?: "Failed to load artworks")
            }
        }
    }

    fun toggleShowOnlyOnView() {
        _showOnlyOnView.value = !_showOnlyOnView.value
        resetAndLoad()
    }

    fun setGalleryFilter(galleryId: Long?) {
        if (_galleryIdFilter.value == galleryId) return
        _galleryIdFilter.value = galleryId
        
        if (galleryId != null) {
            viewModelScope.launch {
                try {
                    _activeGallery.value = artRepository.getGalleryDetails(galleryId)
                } catch (e: Exception) {
                    _activeGallery.value = null
                }
            }
        } else {
            _activeGallery.value = null
        }
        
        resetAndLoad()
    }

    private fun resetAndLoad() {
        loadingJob?.cancel()
        currentPage = 1
        allArtworks.clear()
        isEndReached = false
        loadNextPage()
    }

    fun loadGalleries() {
        viewModelScope.launch {
            try {
                // 1. Fetch IDs of galleries that actually have artworks on display
                val activeIds = artRepository.getActiveGalleryIds().toSet()
                
                // 2. Load all galleries (AIC has ~180 galleries)
                val response1 = artRepository.getGalleries(1)
                val response2 = artRepository.getGalleries(2)
                val allFetched = response1.data + response2.data
                
                // 3. Filter: only show galleries that have artworks AND coordinates
                val filtered = allFetched.filter { gallery ->
                    gallery.id in activeIds && gallery.latitude != null && gallery.longitude != null
                }
                
                _galleries.value = filtered
            } catch (e: Exception) {
                // Keep current state on failure
            }
        }
    }

    fun loadThumbnailForGallery(galleryId: Long) {
        if (_galleryThumbnails.value.containsKey(galleryId)) return
        viewModelScope.launch {
            try {
                // Fetch 1 artwork for this gallery via search
                val response = artRepository.searchArtworks(
                    query = null,
                    page = 1,
                    isOnView = null, // In galleries we don't care about is_on_view for preview
                    galleryId = galleryId
                )
                val imageId = response.data.firstOrNull()?.image_id
                _galleryThumbnails.update { it + (galleryId to imageId) }
            } catch (e: Exception) {
                _galleryThumbnails.update { it + (galleryId to null) }
            }
        }
    }

    fun search(query: String) {
        if (query == lastQuery) return
        lastQuery = query
        isSearching = query.isNotBlank()
        currentPage = 1
        allArtworks.clear()
        isEndReached = false
        loadNextPage()
    }

    fun getArtDetail(id: Int) {
        viewModelScope.launch {
            _isVisited.value = visitedRepository.isVisited(id)
            
            val existing = allArtworks.find { it.id == id && it.description != null }
            if (existing != null) {
                _currentArtworkDetails.value = existing
                _artState.value = ArtState.Success(allArtworks.toList())
                return@launch
            }

            try {
                _artState.value = ArtState.Loading
                var detailed = artRepository.getArtworkDetails(id)
                
                // Fetch coordinates if missing but gallery exists
                if (detailed.gallery_id != null) {
                    try {
                        val gallery = artRepository.getGalleryDetails(detailed.gallery_id!!.toLong())
                        detailed = detailed.copy(
                            latitude = gallery.latitude,
                            longitude = gallery.longitude,
                            gallery_title = if (detailed.gallery_title.isNullOrBlank()) gallery.title else detailed.gallery_title
                        )
                    } catch (e: Exception) { /* Fallback to null coords */ }
                }

                val index = allArtworks.indexOfFirst { it.id == id }
                if (index != -1) {
                    allArtworks[index] = detailed
                } else {
                    allArtworks.add(detailed)
                }
                _currentArtworkDetails.value = detailed
                _artState.value = ArtState.Success(allArtworks.toList())
            } catch (e: Exception) {
                _artState.value = ArtState.Error(e.message ?: "Failed to load artwork details")
            }
        }
    }

    fun toggleVisited(artwork: Artwork) {
        viewModelScope.launch {
            try {
                if (_isVisited.value) {
                    visitedRepository.removeVisit(artwork.id)
                    _isVisited.value = false
                } else {
                    val visited = com.artfinder.data.model.VisitedArtwork(
                        id = artwork.id,
                        title = artwork.title,
                        artist = artwork.artist_display,
                        imageId = artwork.image_id,
                        visitedAt = com.google.firebase.Timestamp.now(),
                        latitude = artwork.latitude,
                        longitude = artwork.longitude,
                        galleryTitle = artwork.gallery_title
                    )
                    visitedRepository.addVisit(visited)
                    _isVisited.value = true
                }
                loadVisitedArtworks()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun removeVisited(id: Int) {
        viewModelScope.launch {
            try {
                visitedRepository.removeVisit(id)
                loadVisitedArtworks()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}

sealed class ArtState {
    object Loading : ArtState()
    data class LoadingMore(val currentArtworks: List<Artwork>) : ArtState()
    data class Success(val artworks: List<Artwork>) : ArtState()
    data class Error(val message: String) : ArtState()
}
