package com.artfinder.data.api

import com.artfinder.data.model.ArtResponse
import com.artfinder.data.model.ArtDetailResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap

interface ArtApiService {
    /**
     * Get a list of artworks.
     * Recommended to use only public domain images.
     * Limit: 60 requests per minute.
     */
    @GET("artworks")
    suspend fun getArtworks(
        @Query("page") page: Int,
        @Query("limit") limit: Int = 20,
        @Query("fields") fields: String = "id,title,artist_display,image_id,classification_title,medium_display,date_display,dimensions,credit_line,gallery_id,gallery_title,is_on_view",
        @Query("query[term][is_on_view]") isOnView: Boolean? = null
    ): ArtResponse

    /**
     * Search for artworks by name.
     */
    @GET("artworks/search")
    suspend fun searchArtworks(
        @Query("q") query: String? = null,
        @Query("params") params: String? = null,
        @Query("page") page: Int,
        @Query("limit") limit: Int = 20,
        @Query("fields") fields: String = "id,title,artist_display,image_id,classification_title,medium_display,date_display,dimensions,credit_line,gallery_id,gallery_title,is_on_view",
        @Query("query[term][is_on_view]") isOnView: Boolean? = null,
        @Query("query[term][gallery_id]") galleryId: Long? = null,
        @Query("facets") facets: String? = null,
        @QueryMap options: Map<String, String> = emptyMap()
    ): ArtResponse

    /**
     * Get details for a specific artwork.
     */
    @GET("artworks/{id}")
    suspend fun getArtworkDetails(
        @Path("id") id: Int,
        @Query("fields") fields: String = "id,title,artist_display,image_id,classification_title,medium_display,date_display,dimensions,credit_line,gallery_id,gallery_title,description,is_on_view"
    ): ArtDetailResponse

    @GET("galleries")
    suspend fun getGalleries(
        @Query("page") page: Int,
        @Query("limit") limit: Int = 100,
        @Query("fields") fields: String = "id,title,latitude,longitude"
    ): GalleryListResponse

    @GET("galleries/{id}")
    suspend fun getGalleryDetails(
        @Path("id") id: Long,
        @Query("fields") fields: String = "id,title,latitude,longitude"
    ): GalleryResponse
}

data class GalleryListResponse(
    val data: List<GalleryData>,
    val pagination: com.artfinder.data.model.Pagination
)

data class GalleryResponse(
    val data: GalleryData
)

data class GalleryData(
    val id: Long,
    val title: String,
    val latitude: Double? = null,
    val longitude: Double? = null
)
