package com.artfinder.data.repository

import com.artfinder.data.api.ArtApiService
import com.artfinder.data.model.ArtResponse
import com.artfinder.data.model.Artwork
import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArtRepository @Inject constructor(
    private val apiService: ArtApiService
) {
    private val TAG = "ArtFinder_ArtRepo"

    suspend fun getArtworks(page: Int, isOnView: Boolean? = null): ArtResponse {
        Log.d(TAG, "getArtworks: page=$page, isOnView=$isOnView")
        return apiService.getArtworks(page = page, isOnView = isOnView)
    }

    suspend fun searchArtworks(query: String? = null, page: Int, isOnView: Boolean? = null, galleryId: Long? = null): ArtResponse {
        var params: String? = null
        if (galleryId != null || isOnView != null) {
            val terms = mutableListOf<String>()
            if (galleryId != null) terms.add("\"term\":{\"gallery_id\":$galleryId}")
            if (isOnView != null) terms.add("\"term\":{\"is_on_view\":$isOnView}")
            
            params = if (terms.size == 1) {
                "{\"query\":{${terms[0]}}}"
            } else {
                "{\"query\":{\"bool\":{\"must\":[{${terms.joinToString("},{")}}]}}}"
            }
        }
        
        Log.d(TAG, "searchArtworks: query=$query, page=$page, galleryId=$galleryId, params=$params")
        return apiService.searchArtworks(
            query = query,
            params = params,
            page = page,
            isOnView = null, // Disable original individual params when using JSON params
            galleryId = null
        )
    }

    suspend fun getArtworkDetails(id: Int): Artwork {
        Log.d(TAG, "getArtworkDetails: id=$id")
        return apiService.getArtworkDetails(id).data
    }

    suspend fun getGalleries(page: Int) = apiService.getGalleries(page = page)

    suspend fun getActiveGalleryIds(): List<Long> {
        val options = mapOf(
            "aggregations[gallery_id][terms][field]" to "gallery_id",
            "aggregations[gallery_id][terms][size]" to "500"
        )
        val response = apiService.searchArtworks(
            isOnView = true,
            page = 1,
            limit = 0,
            options = options
        )
        Log.d(TAG, "getActiveGalleryIds: returning ${response.aggregations?.gallery_id?.buckets?.size ?: 0} galleries")
        return response.aggregations?.gallery_id?.buckets?.map { it.key } ?: emptyList()
    }

    suspend fun getGalleryDetails(id: Long) = apiService.getGalleryDetails(id).data
}
