package com.example.myapplication.data.repository

import com.example.myapplication.data.model.users.User
import com.example.myapplication.data.model.users.UserLocation
import com.example.myapplication.data.model.users.Role
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
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
}
