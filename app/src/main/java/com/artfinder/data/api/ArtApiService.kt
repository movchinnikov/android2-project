package com.artfinder.data.api

import com.artfinder.data.model.ArtResponse
import com.artfinder.data.model.ArtDetailResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ArtApiService {
    /**
     * Get a list of artworks.
     * Recommended to use only public domain images.
     * Limit: 60 requests per minute.
     */
    @GET("artworks")
    suspend fun getArtworks(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 12,
        @Query("fields") fields: String = "id,title,artist_display,image_id,place_of_origin,gallery_title,classification_title"
    ): ArtResponse

    /**
     * Search for artworks by name.
     */
    @GET("artworks/search")
    suspend fun searchArtworks(
        @Query("q") query: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 12,
        @Query("fields") fields: String = "id,title,artist_display,image_id,classification_title"
    ): ArtResponse

    /**
     * Get details for a specific artwork.
     */
    @GET("artworks/{id}")
    suspend fun getArtworkDetails(
        @Path("id") id: Int,
        @Query("fields") fields: String = "id,title,artist_display,image_id,place_of_origin,gallery_title,description,short_description,classification_title,medium_display,date_display,dimensions,credit_line"
    ): ArtDetailResponse
}
