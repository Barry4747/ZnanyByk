package com.example.myapplication.data.repository

import android.content.Context
import android.util.Log
import androidx.compose.animation.core.copy
import com.example.myapplication.data.model.Trainer
import com.example.myapplication.data.model.User
import com.google.firebase.firestore.firestore
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.jvm.optionals.getOrNull

@Singleton
class TrainerRepository @Inject constructor() {
    private val trainerCollection = FirebaseFirestore.getInstance().collection("trainers")


    suspend fun addTrainer(trainer: Trainer, uid: String): Result<String> {
        return try {
            trainerCollection.document(uid).set(trainer).await()
            Result.success(uid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllTrainers(): Result<List<Trainer>> {
        return try {
            val snapshot = trainerCollection.get().await()
            val trainers = snapshot.toObjects(Trainer::class.java)
            Log.d(
                "TRAINER_REPO",
                "📊 Trenerzy: ${trainers.map { "${it.firstName} ${it.lastName}" }}"
            )

            for (trainer in trainers) {
                val avgRating = getTrainerAvgRating(trainer)
                trainer.avgRating = avgRating
            }

            Result.success(trainers)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTrainerAvgRating(trainer: Trainer): String {
        return if (!trainer.ratings.isNullOrEmpty()) {
            "%.2f".format(trainer.ratings.average())
        } else {
            "0.00"
        }
    }
}