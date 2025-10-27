package com.example.myapplication.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.myapplication.data.model.users.Trainer
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class TrainerRepository @Inject constructor() {
    private val trainerCollection = FirebaseFirestore.getInstance().collection("trainers")


    private suspend fun uploadSingleImage(
        context: Context,
        userId: String,
        uri: Uri
    ): Result<String> {
        return try {
            val storageRef = FirebaseStorage.getInstance().reference
            val supportedMimeTypes = setOf("image/jpeg", "image/png", "image/webp")

            val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"

            if (mimeType !in supportedMimeTypes) {
                throw IllegalArgumentException("Unsupported image type: $mimeType")
            }

            val extension = mimeType.substringAfterLast('/', "jpg")
            val rawName = uri.lastPathSegment?.substringAfterLast('/')?.takeIf { it.isNotBlank() }
            val filename = rawName ?: "${UUID.randomUUID()}.$extension"

            val fileRef = storageRef.child("users/$userId/$filename")
            fileRef.putFile(uri).await()
            val url = fileRef.downloadUrl.await().toString()

            Result.success(url)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadImages(
        context: Context,
        userId: String,
        files: List<Uri>
    ): Result<Pair<List<String>, List<Uri>>> {
        return try {
            val uploadResults = coroutineScope {
                files.map { uri -> async { uploadSingleImage(context, userId, uri) } }.awaitAll()
            }

            val successfulUrls = uploadResults.mapNotNull { it.getOrNull() }
            val failedUris = files.filterIndexed { i, _ -> uploadResults[i].isFailure }

            Result.success(successfulUrls to failedUris)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addTrainer(trainer: Trainer, uid: String): Result<String> {
        return try {
            trainerCollection.document(uid).set(trainer).await()
            Result.success(uid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTrainerById(trainerId: String): Result<Trainer> {
        return try {
            val doc = trainerCollection.document(trainerId).get().await()
            val trainer = doc.toObject(Trainer::class.java)
            if (trainer != null) {
                val avgRating = getTrainerAvgRating(trainer)
                trainer.avgRating = avgRating
                Result.success(trainer)
            } else {
                Result.failure(Exception("Trainer not found"))
            }
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
                "Trenerzy: ${trainers.map { "${it.firstName} ${it.lastName}" }}"
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