package com.artfinder.data.model

import com.google.firebase.firestore.DocumentId

data class UserProfile(
    @DocumentId val id: String = "",
    val email: String = "",
    val name: String = "",
    val points: Int = 0,
    val badge: String = "Explorer",
    val visitedArtworks: List<String> = emptyList()
)
