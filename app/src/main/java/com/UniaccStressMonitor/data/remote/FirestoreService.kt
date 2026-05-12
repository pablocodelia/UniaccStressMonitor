package com.UniaccStressMonitor.data.remote

import android.util.Log
import com.UniaccStressMonitor.domain.model.StressSession
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import java.util.concurrent.TimeUnit

class FirestoreService {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val TAG = "FirestoreService"

    suspend fun uploadSessions(sessions: List<StressSession>): Boolean {
        Log.d(TAG, "Starting upload of ${sessions.size} sessions")
        val user = auth.currentUser ?: run {
            Log.e(TAG, "No authenticated user found for upload")
            return false
        }
        val studentId = user.uid
        
        val batch = firestore.batch()
        val sessionsRef = firestore.collection("students_stress")
            .document(studentId)
            .collection("sessions")

        for (session in sessions) {
            val docRef = sessionsRef.document(session.id)
            batch.set(docRef, mapOf(
                "timestamp" to session.timestamp,
                "stressLevel" to session.stressLevel.value,
                "durationSeconds" to session.durationSeconds,
                "userId" to session.userId
            ))
        }

        return try {
            // Set a 10 second timeout to prevent infinite loading
            withTimeout(10000L) {
                Log.d(TAG, "Committing Firestore batch...")
                batch.commit().await()
                Log.d(TAG, "Batch commit successful")
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Firestore upload error or timeout: ${e.message}", e)
            false
        }
    }
}
