package com.artfinder.ui.art

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artfinder.data.model.Artwork
import com.artfinder.data.repository.ArtRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArtViewModel @Inject constructor(
    private val artRepository: ArtRepository
) : ViewModel() {

    private val _artState = MutableStateFlow<ArtState>(ArtState.Loading)
    val artState: StateFlow<ArtState> = _artState

    private var currentPage = 1
    private var allArtworks = mutableListOf<Artwork>()
    private var isEndReached = false
    private var isSearching = false
    private var lastQuery = ""

    init {
        loadNextPage()
    }

    fun loadNextPage() {
        if (_artState.value is ArtState.LoadingMore || isEndReached) return
        
        viewModelScope.launch {
            if (allArtworks.isEmpty()) {
                _artState.value = ArtState.Loading
            } else {
                _artState.value = ArtState.LoadingMore(allArtworks)
            }
            
            try {
                val response = if (isSearching) {
                    artRepository.searchArtworks(lastQuery, currentPage)
                } else {
                    artRepository.getArtworks(currentPage)
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
            // Check if we already have detailed artwork in current state
            val existing = allArtworks.find { it.id == id && it.description != null }
            if (existing != null) {
                _artState.value = ArtState.Success(allArtworks.toList())
                return@launch
            }

            try {
                // If we don't have it or need more details, show loading
                _artState.value = ArtState.Loading
                val detailed = artRepository.getArtworkDetails(id)
                
                val index = allArtworks.indexOfFirst { it.id == id }
                if (index != -1) {
                    allArtworks[index] = detailed
                } else {
                    allArtworks.add(detailed)
                }
                _artState.value = ArtState.Success(allArtworks.toList())
            } catch (e: Exception) {
                _artState.value = ArtState.Error(e.message ?: "Failed to load artwork details")
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
