package com.artfinder.data.model

import com.google.firebase.firestore.DocumentId
import java.util.Date

data class VisitedArtwork(
    @DocumentId val id: String = "",
    val artworkId: Int = 0,
    val title: String = "",
    val visitDate: Date = Date(),
    val photos: List<String> = emptyList(),
    val pointsEarned: Int = 0,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)
