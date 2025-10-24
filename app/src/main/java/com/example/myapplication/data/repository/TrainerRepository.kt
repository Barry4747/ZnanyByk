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
import kotlin.jvm.optionals.getOrNull

class TrainerRepository @Inject constructor(
    private val db: FirebaseFirestore
                                          ) {
    private val trainerCollection = db.collection("trainers")


    suspend fun getAllTrainers(): Result<List<Trainer>> {
        return try {
            val snapshot = trainerCollection.get().await()
            val trainers = snapshot.toObjects(Trainer::class.java)
            Log.d("TRAINER_REPO", "📊 Trenerzy: ${trainers.map { it.name }}")

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
        return if (trainer.ratings.isNotEmpty()) {
            "%.2f".format(trainer.ratings.average()) // 👈 Dwa miejsca po przecinku
        } else {
            "0.00"
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

    suspend fun addTestTrainer(): Result<String> {
        return try {
            val testTrainer = Trainer(
                name = "Jan Kowalski",
                description = "Doświadczony trener personalny",
                specialities = listOf("Siłownia", "Fitness", "Redukcja"),
                location = "Warszawa",
                ratings = listOf(5, 4, 5),
                pricePerHour =  100,
                experience = 4,
                id = "1"
            )

            val docRef = trainerCollection.add(testTrainer).await()
            println("=== DODANO TESTOWEGO TRENERA ===")
            println("ID: ${docRef.id}")
            println("Nazwa: ${testTrainer.name}")
            Result.success(docRef.id)
        } catch (e: Exception) {
            println("Błąd dodawania: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun debugAllTrainerNames() {
        try {
            Log.d("COLLECTION_DEBUG", "=== DEBUG WSZYSTKICH TRENERÓW ===")

            val snapshot = trainerCollection.get().await()

            Log.d("COLLECTION_DEBUG", "📊 Liczba dokumentów: ${snapshot.documents.size}")

            if (snapshot.isEmpty) {
                Log.d("COLLECTION_DEBUG", "Kolekcja jest PUSTA!")
                return
            }

            snapshot.documents.forEachIndexed { index, document ->
                Log.d("COLLECTION_DEBUG", "--- Dokument $index ---")
                Log.d("COLLECTION_DEBUG", "ID: ${document.id}")
                Log.d("COLLECTION_DEBUG", "Wszystkie pola: ${document.data?.keys}")

                // Sprawdź konkretnie pole 'name'
                val name = document.getString("name")
                Log.d("COLLECTION_DEBUG", "Pole 'name': $name")
                Log.d("COLLECTION_DEBUG", "Typ pola 'name': ${name?.let { it::class.java.simpleName }}")

                // Sprawdź jak wygląda name w lowercase
                Log.d("COLLECTION_DEBUG", "name.lowercase(): ${name?.lowercase()}")
            }

        } catch (e: Exception) {
            Log.e("COLLECTION_DEBUG", "Błąd: ${e.message}")
        }
    }

    suspend fun findTrainer(name: String): Result<List<Trainer>> {
        return try {
            Log.d("SEARCH_DEBUG", "🔍 Szukam: '$name'")


            val allSnapshot = trainerCollection.get().await()
            val allTrainers = allSnapshot.toObjects(Trainer::class.java)

            Log.d("SEARCH_DEBUG", "Wszystkich trenerów: ${allTrainers.size}")


            val filteredTrainers = allTrainers.filter { trainer ->
                containsIgnoreCase(trainer.name, name)
            }

            Log.d("SEARCH_DEBUG", "Znaleziono ${filteredTrainers.size} trenerów")
            filteredTrainers.forEach {
                Log.d("SEARCH_DEBUG", "   - ${it.name}")
            }

            Result.success(filteredTrainers)

        } catch (e: Exception) {
            Log.e("SEARCH_DEBUG", "BŁĄD WYSZUKIWANIA: ${e.message}", e)
            Result.failure(e)
        }
    }

    fun containsIgnoreCase(text: String, searchText: String): Boolean {
        return text.contains(searchText, ignoreCase = true)
    }
}