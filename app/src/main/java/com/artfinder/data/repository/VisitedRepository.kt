package com.artfinder.data.repository

import com.artfinder.data.model.VisitedArtwork
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VisitedRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val userId: String
        get() = auth.currentUser?.uid ?: throw Exception("User not logged in")

    private val visitedCollection
        get() = firestore.collection("users").document(userId).collection("visited_artworks")

    suspend fun addVisit(artwork: VisitedArtwork) {
        visitedCollection.document(artwork.id.toString()).set(artwork).await()
    }

    suspend fun removeVisit(artworkId: Int) {
        visitedCollection.document(artworkId.toString()).delete().await()
    }

    suspend fun getVisits(): List<VisitedArtwork> {
        return visitedCollection.get().await().toObjects(VisitedArtwork::class.java)
    }

    suspend fun isVisited(artworkId: Int): Boolean {
        return try {
            visitedCollection.document(artworkId.toString()).get().await().exists()
        } catch (e: Exception) {
            false
        }
    }
}
