package com.artfinder.data.repository

import com.artfinder.data.model.VisitedArtwork
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VisitedRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val TAG = "ArtFinder_VisitedRepo"

    private val userId: String
        get() = auth.currentUser?.uid ?: throw Exception("User not logged in")

    private val visitedCollection
        get() = firestore.collection("users").document(userId).collection("visited_artworks")

    suspend fun addVisit(artwork: VisitedArtwork) {
        Log.d(TAG, "addVisit: artworkId=${artwork.id}")
        visitedCollection.document(artwork.id.toString()).set(artwork.copy(userId = userId)).await()
    }

    suspend fun setVisitedStatus(artworkId: Int, isVisited: Boolean) {
        Log.d(TAG, "setVisitedStatus: id=$artworkId, isVisited=$isVisited")
        val doc = visitedCollection.document(artworkId.toString()).get().await()
        if (doc.exists()) {
            visitedCollection.document(artworkId.toString()).update("isVisited", isVisited).await()
        }
    }

    suspend fun removeVisitIfNoPhotos(artworkId: Int) {
        val visit = getVisitById(artworkId)
        if (visit != null && visit.imageUrls.isEmpty() && !visit.isVisited) {
            Log.d(TAG, "removeVisitIfNoPhotos: deleting doc for id=$artworkId")
            visitedCollection.document(artworkId.toString()).delete().await()
        }
    }

    suspend fun deleteVisit(artworkId: Int) {
        Log.d(TAG, "deleteVisit: id=$artworkId")
        visitedCollection.document(artworkId.toString()).delete().await()
    }

    suspend fun getPublicVisitsForArtwork(artworkId: Int): List<VisitedArtwork> {
        Log.d(TAG, "getPublicVisitsForArtwork: id=$artworkId")
        return firestore.collectionGroup("visited_artworks")
            .whereEqualTo("id", artworkId)
            .get()
            .await()
            .toObjects(VisitedArtwork::class.java)
    }

    suspend fun getVisits(): List<VisitedArtwork> {
        return visitedCollection.get().await().toObjects(VisitedArtwork::class.java)
    }

    fun getVisitsFlow(): Flow<List<VisitedArtwork>> = callbackFlow {
        val listener = visitedCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val items = snapshot.toObjects(VisitedArtwork::class.java)
                trySend(items)
            }
        }
        awaitClose { listener.remove() }
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
