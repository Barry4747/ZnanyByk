package com.example.myapplication.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.gestures.forEach
import androidx.compose.foundation.layout.size
import com.example.myapplication.data.model.users.Trainer
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileOutputStream
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

            // Compress the image
            val bitmap = BitmapFactory.decodeStream(context.contentResolver.openInputStream(uri))
            if (bitmap == null) {
                throw IllegalArgumentException("Failed to decode image")
            }

            val tempFile = File.createTempFile("compressed", ".$extension", context.cacheDir)
            val outputStream = FileOutputStream(tempFile)
            val compressFormat = when (mimeType) {
                "image/jpeg" -> Bitmap.CompressFormat.JPEG
                "image/png" -> Bitmap.CompressFormat.PNG
                "image/webp" -> Bitmap.CompressFormat.WEBP
                else -> Bitmap.CompressFormat.JPEG
            }
            val quality = if (mimeType == "image/png") 100 else 80
            bitmap.compress(compressFormat, quality, outputStream)
            outputStream.close()
            bitmap.recycle()

            val fileRef = storageRef.child("users/$userId/$filename")
            fileRef.putFile(Uri.fromFile(tempFile)).await()
            val url = fileRef.downloadUrl.await().toString()

            // Clean up temp file
            tempFile.delete()

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
