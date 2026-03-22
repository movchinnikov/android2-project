package com.artfinder.data.repository

import com.artfinder.data.model.VisitedArtwork
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VisitedRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    private val userId get() = auth.currentUser?.uid ?: ""

    suspend fun markAsVisited(visited: VisitedArtwork) {
        if (userId.isEmpty()) return
        firestore.collection("users")
            .document(userId)
            .collection("visited_artworks")
            .document(visited.artworkId.toString())
            .set(visited)
            .await()
    }

    suspend fun getVisitedArtworks(): List<VisitedArtwork> {
        if (userId.isEmpty()) return emptyList()
        return firestore.collection("users")
            .document(userId)
            .collection("visited_artworks")
            .get()
            .await()
            .toObjects(VisitedArtwork::class.java)
    }

    suspend fun deleteVisitedArtwork(artworkId: Int) {
        if (userId.isEmpty()) return
        firestore.collection("users")
            .document(userId)
            .collection("visited_artworks")
            .document(artworkId.toString())
            .delete()
            .await()
    }
}
