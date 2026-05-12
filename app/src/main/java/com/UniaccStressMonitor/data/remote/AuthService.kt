package com.UniaccStressMonitor.data.remote

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

class AuthService {
    private val auth = FirebaseAuth.getInstance()
    private val TAG = "AuthService"

    fun isUserLoggedIn(): Boolean = auth.currentUser != null

    fun getCurrentUserEmail(): String? = auth.currentUser?.email

    fun getCurrentUserId(): String? = auth.currentUser?.uid

    suspend fun login(email: String, password: String): Pair<Boolean, String?> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            Log.d(TAG, "Login successful for: $email")
            Pair(true, null)
        } catch (e: Exception) {
            Log.e(TAG, "Login error: ${e.message}", e)
            Pair(false, e.localizedMessage)
        }
    }

    suspend fun register(email: String, password: String): Pair<Boolean, String?> {
        return try {
            auth.createUserWithEmailAndPassword(email, password).await()
            Log.d(TAG, "Registration successful for: $email")
            Pair(true, null)
        } catch (e: Exception) {
            Log.e(TAG, "Registration error: ${e.message}", e)
            Pair(false, e.localizedMessage)
        }
    }

    fun logout() {
        auth.signOut()
    }
}
