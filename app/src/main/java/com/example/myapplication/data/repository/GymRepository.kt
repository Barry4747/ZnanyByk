package com.example.myapplication.data.repository

import com.example.myapplication.data.model.gyms.Gym
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GymRepository @Inject constructor() {
    private val db = FirebaseFirestore.getInstance()
    private val gymsCollection = db.collection("gyms")


    suspend fun getAllGyms(): Result<List<Gym>> {
        return try {
            val snapshot = gymsCollection.get().await()
            val gyms = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Gym::class.java)?.copy(id = doc.id)
            }
            Result.success(gyms)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getGymById(gymId: String): Result<Gym?> {
        return try {
            val doc = gymsCollection.document(gymId).get().await()
            if (doc.exists()) {
                val gym = doc.toObject(Gym::class.java)?.copy(id = doc.id)
                Result.success(gym)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getGymsByIds(gymIds: List<String>): Result<Map<String, Gym>> {
        if (gymIds.isEmpty()) return Result.success(emptyMap())

        return try {
            val snapshot = gymsCollection.whereIn("__name__", gymIds).get().await()
            val gyms = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Gym::class.java)?.copy(id = doc.id)
            }.associateBy { it.id }
            Result.success(gyms)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchGyms(query: String): Result<List<Gym>> {
        return try {
            val snapshot = gymsCollection.get().await()
            val lowerQuery = query.lowercase()
            val gyms = snapshot.documents.mapNotNull { doc ->
                val gym = doc.toObject(Gym::class.java)?.copy(id = doc.id)
                gym
            }.filter { gym ->
                gym.gymName.lowercase().contains(lowerQuery) ||
                        gym.gymLocation.formattedAddress.lowercase().contains(lowerQuery)
            }
            Result.success(gyms)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
