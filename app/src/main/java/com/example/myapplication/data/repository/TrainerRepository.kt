package com.example.myapplication.data.repository

import com.example.myapplication.data.model.Trainer
import com.example.myapplication.data.model.User
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class TrainerRepository {
    private val db = Firebase.firestore
    private val trainerCollection = db.collection("trainers")

    suspend fun getAllTrainers(): Result<List<Trainer>> {
        return try {
            val snapshot = trainerCollection.get().await()
            val users = snapshot.toObjects(Trainer::class.java)
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addTrainer(trainer: Trainer): Result<String> {
        return try {
            val docRef = trainerCollection.add(trainer).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun findTrainer(name: String): Result<List<Trainer>> {
        return try {
            val snapshot = trainerCollection.whereEqualTo("name", name).get().await()
            if (snapshot.isEmpty) {
                Result.failure(Exception("Trainer not found"))
            } else {
                val trainers = snapshot.toObjects(Trainer::class.java)
                Result.success(trainers)}
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}