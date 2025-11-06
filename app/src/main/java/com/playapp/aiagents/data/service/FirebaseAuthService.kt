package com.playapp.aiagents.data.service

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.AuthResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.tasks.await

class FirebaseAuthService {
    private val TAG = "FirebaseAuthService"
    private val auth = FirebaseAuth.getInstance()

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    val isUserLoggedIn: Boolean
        get() = currentUser != null

    fun getAuthStateFlow(): Flow<FirebaseUser?> = callbackFlow {
        // Emit current user
        trySend(auth.currentUser)

        // Listen for auth state changes
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser)
        }
        auth.addAuthStateListener(listener)

        awaitClose { auth.removeAuthStateListener(listener) }
    }

    suspend fun signInWithEmailAndPassword(email: String, password: String): Result<AuthResult> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            Result.success(result)
        } catch (e: Exception) {
            Log.e(TAG, "Error signing in with email and password", e)
            Result.failure(e)
        }
    }

    suspend fun createUserWithEmailAndPassword(email: String, password: String): Result<AuthResult> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            Result.success(result)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating user with email and password", e)
            Result.failure(e)
        }
    }

    suspend fun signInWithGoogle(idToken: String): Result<AuthResult> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            Result.success(result)
        } catch (e: Exception) {
            Log.e(TAG, "Error signing in with Google", e)
            Result.failure(e)
        }
    }

    suspend fun signInAnonymously(): Result<AuthResult> {
        return try {
            val result = auth.signInAnonymously().await()
            Result.success(result)
        } catch (e: Exception) {
            Log.e(TAG, "Error signing in anonymously", e)
            Result.failure(e)
        }
    }

    fun signOut() {
        auth.signOut()
    }

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error sending password reset email", e)
            Result.failure(e)
        }
    }

    suspend fun updatePassword(newPassword: String): Result<Unit> {
        return try {
            currentUser?.updatePassword(newPassword)?.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating password", e)
            Result.failure(e)
        }
    }

    suspend fun deleteAccount(): Result<Unit> {
        return try {
            currentUser?.delete()?.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting account", e)
            Result.failure(e)
        }
    }
}