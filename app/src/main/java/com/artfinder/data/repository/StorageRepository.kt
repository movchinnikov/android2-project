package com.artfinder.data.repository

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageRepository @Inject constructor(
    private val storage: FirebaseStorage,
    private val auth: FirebaseAuth
) {
    private val userId: String
        get() = auth.currentUser?.uid ?: throw Exception("User not logged in")

    suspend fun uploadVisitPhoto(artworkId: Int, uri: Uri): String = withContext(Dispatchers.IO) {
        val fileName = "visit_${System.currentTimeMillis()}.jpg"
        val ref = storage.reference.child("visits/$userId/$artworkId/$fileName")
        ref.putFile(uri).await()
        ref.downloadUrl.await().toString()
    }

    suspend fun deletePhoto(url: String) = withContext(Dispatchers.IO) {
        try {
            val ref = storage.getReferenceFromUrl(url)
            ref.delete().await()
        } catch (e: Exception) {
            // Log or ignore if already gone
        }
    }
}
