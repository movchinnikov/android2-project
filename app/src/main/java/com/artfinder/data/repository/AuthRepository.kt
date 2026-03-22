package com.artfinder.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth
) {
    suspend fun login(email: String, psw: String): FirebaseUser? {
        return auth.signInWithEmailAndPassword(email, psw).await().user
    }

    suspend fun register(email: String, psw: String): FirebaseUser? {
        return auth.createUserWithEmailAndPassword(email, psw).await().user
    }

    fun logout() {
        auth.signOut()
    }

    suspend fun updatePassword(newPsw: String) {
        auth.currentUser?.updatePassword(newPsw)?.await()
    }

    suspend fun reauthenticate(password: String) {
        val user = auth.currentUser ?: throw Exception("No user logged in")
        val email = user.email ?: throw Exception("User email not found")
        val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(email, password)
        user.reauthenticate(credential).await()
    }

    suspend fun resetPassword(email: String) {
        auth.sendPasswordResetEmail(email).await()
    }
}
