package com.artfinder.data.repository

import com.artfinder.data.api.ArtApiService
import com.artfinder.data.model.ArtResponse
import com.artfinder.data.model.Artwork
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArtRepository @Inject constructor(
    private val apiService: ArtApiService
) {
    suspend fun getArtworks(page: Int): ArtResponse {
        return apiService.getArtworks(page = page)
    }

    suspend fun searchArtworks(query: String, page: Int): ArtResponse {
        return apiService.searchArtworks(query = query, page = page)
    }

    suspend fun getArtworkDetails(id: Int): Artwork {
        return apiService.getArtworkDetails(id).data
    }
}
