package com.example.myapplication.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth: FirebaseAuth = Firebase.auth

    suspend fun registerUser(email: String, password: String): Result<String> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: throw Exception("User creation failed")
            Result.success(firebaseUser.uid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getCurrentUserEmail(): String? {
        return auth.currentUser?.email
    }

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    fun logoutUser() {
        auth.signOut()
    }
}

