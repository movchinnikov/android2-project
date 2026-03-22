package com.artfinder.data.model


data class ArtResponse(
    val data: List<Artwork>,
    val pagination: Pagination,
    val aggregations: Aggregations? = null
)

data class Aggregations(
    val gallery_id: GalleryAggregation? = null
)

data class GalleryAggregation(
    val buckets: List<GalleryBucket>? = null
)

data class GalleryBucket(
    val key: Long,
    val doc_count: Int
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
    val credit_line: String? = null,
    val gallery_id: Int? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val is_on_view: Boolean = false
) {
    val imageUrl: String
        get() = "https://www.artic.edu/iiif/2/$image_id/full/843,/0/default.jpg"
}

data class VisitedArtwork(
    val id: Int = 0,
    val title: String = "",
    val artist: String? = null,
    val imageId: String? = null,
    val visitedAt: com.google.firebase.Timestamp? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val galleryTitle: String? = null,
    val imageUrls: List<String> = emptyList(),
    val isVisited: Boolean = true,
    val userId: String = "",
    val userName: String = ""
)

data class Pagination(
    val total: Int,
    val limit: Int,
    val offset: Int,
    val total_pages: Int,
    val current_page: Int
)
