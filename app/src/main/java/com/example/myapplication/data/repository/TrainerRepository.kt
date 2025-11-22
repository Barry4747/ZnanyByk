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


    private suspend fun uploadSingleMedia(
        context: Context,
        userId: String,
        uri: Uri
    ): Result<String> {
        return try {
            val storageRef = FirebaseStorage.getInstance().reference
            val supportedMimeTypes = setOf("image/jpeg", "image/png", "image/webp", "video/mp4")

            val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"

            if (mimeType !in supportedMimeTypes) {
                throw IllegalArgumentException("Unsupported media type: $mimeType")
            }

            val fileExtension = when (mimeType) {
                "image/jpeg" -> "jpg"
                "image/png" -> "png"
                "image/webp" -> "webp"
                "video/mp4" -> "mp4"
                else -> "unknown"
            }

            val fileName = "${UUID.randomUUID()}.$fileExtension"
            val fileRef = storageRef.child("users/$userId/$fileName")

            fileRef.putFile(uri).await()
            val downloadUrl = fileRef.downloadUrl.await().toString()

            Result.success(downloadUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadMedias(
        context: Context,
        userId: String,
        files: List<Uri>
    ): Result<Pair<List<String>, List<Uri>>> {
        return try {
            val uploadResults = coroutineScope {
                files.map { uri -> async { uploadSingleMedia(context, userId, uri) } }.awaitAll()
            }

            val successfulUrls = uploadResults.mapNotNull { it.getOrNull() }
            val failedUris = files.filterIndexed { i, _ -> uploadResults[i].isFailure }

            Result.success(successfulUrls to failedUris)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteImageByUrl(imageUrl: String): Result<Unit> {
        return try {
            val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl)
            storageRef.delete().await()
            Result.success(Unit)
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

    suspend fun getFilteredTrainers(
        query: String = "",
        minRating: Float = 0.0f,
        minPrice: Int = 0,
        maxPrice: Int = 8000,
        categories: Set<String> = emptySet()
    ): Result<List<Trainer>> {
        return try {
            val snapshot = trainerCollection.get().await()
            var trainers = snapshot.toObjects(Trainer::class.java)

            // Filtrowanie po query (nazwa, opis, kategorie, etc.)
            if (query.isNotBlank()) {
                val lowerQuery = query.lowercase()
                trainers = trainers.filter { trainer ->
                    val firstName = trainer.firstName?.lowercase() ?: ""
                    val lastName = trainer.lastName?.lowercase() ?: ""
                    val fullName = "$firstName $lastName"

                    // Sprawdź, czy query pasuje do imienia, nazwiska LUB pełnej nazwy
                    firstName.contains(lowerQuery) ||
                            lastName.contains(lowerQuery) ||
                            fullName.contains(lowerQuery)
                }
            }
            // Filtrowanie po kategoriach
            if (categories.isNotEmpty()) {
                trainers = trainers.filter { trainer ->
                    trainer.categories?.any { it in categories } == true
                }
            }

            // Filtrowanie po cenie
            trainers = trainers.filter {
                val price = it.pricePerHour ?: 0
                price in minPrice..maxPrice
            }


            Log.d("TRAINER_REPO", "📊 Znaleziono ${trainers.size} przefiltrowanych trenerów. " +
                    "Query: '$query', MinRating: $minRating, Price: $minPrice-$maxPrice, " +
                    "Categories: ${categories.size}")

            Result.success(trainers)
        } catch (e: Exception) {
            Log.e("TRAINER_REPO", "Błąd podczas pobierania lub filtrowania trenerów", e)
            Result.failure(e)
        }
    }
    }

    suspend fun getTrainerAvgRating(trainer: Trainer): String {
        return if (!trainer.ratings.isNullOrEmpty()) {
            "%.2f".format(trainer.ratings.average())
        } else {
            "0.00"
        }
    }
