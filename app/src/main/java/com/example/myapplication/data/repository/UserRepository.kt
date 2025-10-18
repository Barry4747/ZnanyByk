package com.example.myapplication.data.repository

import com.example.myapplication.data.model.User
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class UserRepository {
    private val db = Firebase.firestore
    private val usersCollection = db.collection("users")

    suspend fun addUser(user: User): Result<String> {
        return try {
            val email = user.email ?: throw IllegalArgumentException("Email is required")

            // Sprawdź czy użytkownik już istnieje
            val exists = usersCollection.document(email).get().await().exists()
            if (exists) {
                return Result.failure(Exception("User with this email already exists"))
            }

            usersCollection.document(email).set(user).await()
            Result.success(email)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUser(email: String): Result<User?> {
        return try {
            val doc = usersCollection.document(email).get().await()
            val user = doc.toObject(User::class.java)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUser(email: String, user: User): Result<Unit> {
        return try {
            usersCollection.document(email).set(user).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteUser(email: String): Result<Unit> {
        return try {
            usersCollection.document(email).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllUsers(): Result<List<User>> {
        return try {
            val snapshot = usersCollection.get().await()
            val users = snapshot.toObjects(User::class.java)
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
