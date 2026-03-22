package com.artfinder.data.model

data class ArtResponse(
    val data: List<Artwork>,
    val pagination: Pagination
)

data class ArtDetailResponse(
    val data: Artwork
)

data class Artwork(
    val id: Int,
    val title: String,
    val artist_display: String? = null,
    val image_id: String? = null,
    val place_of_origin: String? = null,
    val gallery_title: String? = null,
    val description: String? = null,
    val short_description: String? = null,
    val classification_title: String? = null,
    val medium_display: String? = null,
    val date_display: String? = null,
    val dimensions: String? = null,
    val credit_line: String? = null
) {
    val imageUrl: String
        get() = "https://www.artic.edu/iiif/2/$image_id/full/843,/0/default.jpg"
}

data class Pagination(
    val total: Int,
    val limit: Int,
    val offset: Int,
    val total_pages: Int,
    val current_page: Int
)
