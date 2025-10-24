package com.example.myapplication.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.myapplication.data.model.User
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserRepository @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val usersCollection = FirebaseFirestore.getInstance().collection("users")
    private val dataStore = context.dataStore

    companion object {
        private val USER_ID_KEY = stringPreferencesKey("user_id")
    }

    suspend fun saveCachedUser(user: User, uid: String) {
        dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = uid
        }
    }

    suspend fun getCachedUser(): Pair<String, User>? {
        val preferences = dataStore.data.first()
        val uid = preferences[USER_ID_KEY] ?: return null
        val doc = usersCollection.document(uid).get().await()
        val user = doc.toObject(User::class.java) ?: return null
        return Pair(uid, user)
    }

    suspend fun clearCachedUser() {
        dataStore.edit { preferences ->
            preferences.remove(USER_ID_KEY)
        }
    }

    suspend fun isUserCached(): Boolean {
        val preferences = dataStore.data.first()
        return preferences[USER_ID_KEY] != null
    }

    suspend fun getCachedUserId(): String? {
        val preferences = dataStore.data.first()
        return preferences[USER_ID_KEY]
    }

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
}
