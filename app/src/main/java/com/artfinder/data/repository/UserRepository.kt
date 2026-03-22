package com.artfinder.data.repository

import com.artfinder.data.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    val currentUser get() = auth.currentUser

    suspend fun getUserProfile(): UserProfile? {
        val uid = auth.currentUser?.uid ?: return null
        return try {
            firestore.collection("users")
                .document(uid)
                .get()
                .await()
                .toObject(UserProfile::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun saveUserProfile(uid: String, profile: UserProfile) {
        firestore.collection("users")
            .document(uid)
            .set(profile)
            .await()
    }

    suspend fun updatePoints(newPoints: Int) {
        val uid = auth.currentUser?.uid ?: return
        val badge = when {
            newPoints > 250 -> "Archivist"
            newPoints > 100 -> "Curator"
            else -> "Explorer"
        }
        
        firestore.collection("users")
            .document(uid)
            .update(mapOf(
                "points" to newPoints,
                "badge" to badge
            ))
            .await()
    }

    suspend fun updateProfileName(name: String) {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("users")
            .document(uid)
            .update("name", name)
            .await()
    }

    suspend fun getAllUsers(): List<UserProfile> {
        return try {
            firestore.collection("users")
                .get()
                .await()
                .toObjects(UserProfile::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
