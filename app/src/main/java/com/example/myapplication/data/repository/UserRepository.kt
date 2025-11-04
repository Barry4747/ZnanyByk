package com.example.myapplication.data.repository

import android.content.Context
import android.net.Uri
import com.example.myapplication.data.model.users.Role
import com.example.myapplication.data.model.users.User
import com.example.myapplication.data.model.users.UserLocation
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor() {
    private val usersCollection = FirebaseFirestore.getInstance().collection("users")

    suspend fun addUser(user: User, uid: String): Result<String> {
        return try {
            usersCollection.document(uid).set(user).await()
            Result.success(uid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUser(uid: String): Result<User?> {
        return try {
            val doc = usersCollection.document(uid).get().await()
            val user = doc.toObject(User::class.java)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserByEmail(email: String): Result<User?> {
        return try {
            val snapshot = usersCollection
                .whereEqualTo("email", email)
                .get()
                .await()
            val user = snapshot.documents.firstOrNull()?.toObject(User::class.java)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUser(uid: String, user: User): Result<Unit> {
        return try {
            usersCollection.document(uid).set(user).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUserLocation(uid: String, location: UserLocation): Result<Unit> {
        return try {
            val locationMap = mapOf(
                "latitude" to location.latitude,
                "longitude" to location.longitude,
                "fullAddress" to location.fullAddress,
                "city" to location.city,
                "postalCode" to location.postalCode,
                "country" to location.country
            )
            usersCollection
                .document(uid)
                .set(mapOf("location" to locationMap), SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteUser(uid: String): Result<Unit> {
        return try {
            usersCollection.document(uid).delete().await()
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

    fun getUserSync(uid: String): User? {
        return try {
            kotlinx.coroutines.runBlocking {
                val doc = usersCollection.document(uid).get().await()
                doc.toObject(User::class.java)
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateUserRole(uid: String, role: Role): Result<Unit> {
        return try {
            usersCollection.document(uid).set(mapOf("role" to role), SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserLocation(uid: String): Result<UserLocation?> {
        return getUser(uid).map { it?.location }
    }

    suspend fun uploadAvatar(context: Context, userId: String, uri: Uri): Result<String> {
        return try {
            val storageRef = FirebaseStorage.getInstance().reference
            val user = getUser(userId).getOrNull()
            if (user?.avatarUrl != null) {
                // Delete previous avatar
                val previousRef = FirebaseStorage.getInstance().getReferenceFromUrl(user.avatarUrl!!)
                previousRef.delete().await()
            }
            // Upload new avatar
            val filename = "avatar.jpg"
            val avatarRef = storageRef.child("users/$userId/avatar/$filename")
            val inputStream = context.contentResolver.openInputStream(uri)
            inputStream?.use { stream ->
                avatarRef.putStream(stream).await()
            }
            val downloadUrl = avatarRef.downloadUrl.await().toString()
            // Update user with new avatarUrl
            val updatedUser = user?.copy(avatarUrl = downloadUrl) ?: User(avatarUrl = downloadUrl)
            updateUser(userId, updatedUser)
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAvatarUrl(userId: String): Result<String?> {
        return getUser(userId).map { it?.avatarUrl }
    }
}
