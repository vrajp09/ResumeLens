package com.cs407.resumelens.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.tasks.await

class FirestoreRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    // Save user profile data
    suspend fun saveUserProfile(userId: String, data: Map<String, Any>): Result<Unit> {
        return try {
            db.collection("users")
                .document(userId)
                .set(data)
                .await()
            Result.success(Unit)
        } catch (e: FirebaseFirestoreException) {
            Result.failure(e)
        }
    }

    // Get user profile
    suspend fun getUserProfile(userId: String): Result<Map<String, Any>?> {
        return try {
            val document = db.collection("users")
                .document(userId)
                .get()
                .await()
            Result.success(document.data)
        } catch (e: FirebaseFirestoreException) {
            Result.failure(e)
        }
    }

    // Save resume analysis results
    suspend fun saveResumeAnalysis(
        userId: String,
        analysisId: String,
        data: Map<String, Any>
    ): Result<Unit> {
        return try {
            db.collection("users")
                .document(userId)
                .collection("resume_analyses")
                .document(analysisId)
                .set(data)
                .await()
            Result.success(Unit)
        } catch (e: FirebaseFirestoreException) {
            Result.failure(e)
        }
    }

    // Get all resume analyses for a user
    suspend fun getResumeAnalyses(userId: String): Result<List<Map<String, Any>>> {
        return try {
            val snapshot = db.collection("users")
                .document(userId)
                .collection("resume_analyses")
                .get()
                .await()
            val analyses = snapshot.documents.mapNotNull { it.data }
            Result.success(analyses)
        } catch (e: FirebaseFirestoreException) {
            Result.failure(e)
        }
    }

    // Get a single resume analysis by ID
    suspend fun getResumeAnalysisById(
        userId: String,
        analysisId: String
    ): Result<Map<String, Any>> {
        return try {
            val document = db.collection("users")
                .document(userId)
                .collection("resume_analyses")
                .document(analysisId)
                .get()
                .await()
            
            if (document.exists()) {
                Result.success(document.data!!)
            } else {
                Result.failure(Exception("Analysis not found"))
            }
        } catch (e: FirebaseFirestoreException) {
            Result.failure(e)
        }
    }

    // Real-time listener for resume analyses
    fun observeResumeAnalyses(
        userId: String,
        onUpdate: (List<Map<String, Any>>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("users")
            .document(userId)
            .collection("resume_analyses")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }
                val analyses = snapshot?.documents?.mapNotNull { it.data } ?: emptyList()
                onUpdate(analyses)
            }
    }
}