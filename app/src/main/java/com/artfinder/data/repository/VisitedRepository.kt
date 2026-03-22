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
        visitedCollection.document(artwork.id.toString()).set(artwork.copy(userId = userId)).await()
    }

    suspend fun setVisitedStatus(artworkId: Int, isVisited: Boolean) {
        val doc = visitedCollection.document(artworkId.toString()).get().await()
        if (doc.exists()) {
            visitedCollection.document(artworkId.toString()).update("isVisited", isVisited).await()
        }
    }

    suspend fun removeVisitIfNoPhotos(artworkId: Int) {
        val visit = getVisitById(artworkId)
        if (visit != null && visit.imageUrls.isEmpty() && !visit.isVisited) {
            visitedCollection.document(artworkId.toString()).delete().await()
        }
    }

    suspend fun getPublicVisitsForArtwork(artworkId: Int): List<VisitedArtwork> {
        return firestore.collectionGroup("visited_artworks")
            .whereEqualTo("id", artworkId)
            .get()
            .await()
            .toObjects(VisitedArtwork::class.java)
    }

    suspend fun getVisits(): List<VisitedArtwork> {
        return visitedCollection.get().await().toObjects(VisitedArtwork::class.java)
    }

    suspend fun isVisited(artworkId: Int): Boolean {
        return try {
            val doc = visitedCollection.document(artworkId.toString()).get().await()
            doc.exists() && doc.getBoolean("isVisited") == true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getVisitById(artworkId: Int): VisitedArtwork? {
        return try {
            visitedCollection.document(artworkId.toString()).get().await().toObject(VisitedArtwork::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateVisitPhotos(artworkId: Int, urls: List<String>) {
        visitedCollection.document(artworkId.toString()).update("imageUrls", urls).await()
    }
}
