package com.artfinder.data.repository

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhotoStorageRepository @Inject constructor(
    private val storage: FirebaseStorage
) {
    suspend fun uploadPhoto(uri: Uri): String {
        val fileName = UUID.randomUUID().toString()
        val ref = storage.reference.child("artwork_photos/$fileName")
        ref.putFile(uri).await()
        return ref.downloadUrl.await().toString()
    }
}
