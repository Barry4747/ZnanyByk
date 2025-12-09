package com.example.myapplication.data.repository

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import com.example.myapplication.data.model.users.User
import com.example.myapplication.data.model.users.UserLocation

data class GoogleSignInResult(
    val uid: String,
    val email: String?
)

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    @param:ApplicationContext private val context: Context
    ,
    private val firestore: FirebaseFirestore
) {
    private val _currentUser = MutableStateFlow(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    @Volatile
    private var cachedUserData: Pair<String, User>? = null


    suspend fun updateCachedUserFromFirebaseAuth(): Boolean {
        val firebaseUser = auth.currentUser
        if (firebaseUser == null) {
            cachedUserData = null
            return false
        }

        val uid = firebaseUser.uid
        return try {
            val doc = firestore.collection("users").document(uid).get().await()
            if (doc.exists()) {
                val user = doc.toObject(User::class.java)
                if (user != null) {
                    cachedUserData = Pair(uid, user)
                    true
                } else {
                    cachedUserData = null
                    false
                }
            } else {
                cachedUserData = null
                false
            }
        } catch (e: Exception) {
            cachedUserData = null
            false
        }
    }

    suspend fun registerUser(email: String, password: String): Result<String> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: throw Exception("User creation failed")
            _currentUser.value = firebaseUser
            Result.success(firebaseUser.uid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginUser(email: String, password: String): Result<String> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: throw Exception("Login failed")
            _currentUser.value = firebaseUser
            Result.success(firebaseUser.uid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInWithGoogle(webClientId: String): Result<GoogleSignInResult> {
        return try {
            val credentialManager = CredentialManager.create(context)

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(
                request = request,
                context = context
            )

            val credential = result.credential
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val googleIdToken = googleIdTokenCredential.idToken

            val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
            val authResult = auth.signInWithCredential(firebaseCredential).await()
            val firebaseUser = authResult.user ?: throw Exception("Google sign-in failed")

            _currentUser.value = firebaseUser
            Result.success(GoogleSignInResult(firebaseUser.uid, firebaseUser.email))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun saveCachedUser(user: User, uid: String) {
        cachedUserData = Pair(uid, user)
    }

    fun getCachedUser(): Pair<String, User>? {
        return cachedUserData
    }

    fun clearCachedUser() {
        cachedUserData = null
    }

    fun getCurrentUserEmail(): String? {
        return _currentUser.value?.email
    }

    fun getCurrentUserId(): String? {
        return _currentUser.value?.uid
    }

    fun getCurrentLocation(): UserLocation? {
        return cachedUserData?.second?.location
    }

    fun logoutUser() {
        auth.signOut()
        _currentUser.value = null
        clearCachedUser()
    }

    suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
