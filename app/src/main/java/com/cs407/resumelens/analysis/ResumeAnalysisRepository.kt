package com.cs407.resumelens.analysis

import com.cs407.resumelens.auth.AuthRepository
import com.cs407.resumelens.data.FirestoreRepository
import com.cs407.resumelens.network.AnalysisRequestDto
import com.cs407.resumelens.network.AnalysisResponseDto
import com.cs407.resumelens.network.ApiClient
import com.cs407.resumelens.network.ResumeLensApi
import com.cs407.resumelens.network.SuggestionDto
import com.google.firebase.Timestamp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class ResumeAnalysisRepository(
    private val api: ResumeLensApi = ApiClient.api,
    private val firestoreRepo: FirestoreRepository = FirestoreRepository(),
    private val authRepo: AuthRepository = AuthRepository()
) {

    /**
     * 1) Uploads image bytes to /extract
     * 2) Sends extracted text to /analyze
     * 3) Saves result in Firestore under users/{uid}/resume_analyses/{analysisId}
     */
    suspend fun analyzeImageBytes(imageBytes: ByteArray): Result<AnalysisResponseDto> =
        withContext(Dispatchers.IO) {
            try {
                val requestFile = imageBytes.toRequestBody("image/jpeg".toMediaType())
                val filePart = MultipartBody.Part.createFormData(
                    name = "file",
                    filename = "resume.jpg",
                    body = requestFile
                )

                val ocr = api.extractText(filePart)

                val analysis = api.analyzeResume(
                    AnalysisRequestDto(
                        resume_text = ocr.extracted_text
                    )
                )

                // ---- Save in Firestore ----
                val userId = authRepo.currentUser?.uid
                if (userId != null) {
                    val analysisId = System.currentTimeMillis().toString()
                    val data = mapOf(
                        "analysisId" to analysisId,
                        "source" to "image",                // FIXED
                        "score" to analysis.score,
                        "summary" to analysis.summary,
                        "resumeText" to ocr.extracted_text, // REQUIRED FOR HISTORY
                        "suggestions" to analysis.suggestions.map { s ->
                            mapOf(
                                "category" to s.category,
                                "issue" to s.issue,
                                "recommendation" to s.recommendation
                            )
                        },
                        "suggestionCount" to analysis.suggestions.size,  // REQUIRED FOR DASHBOARD
                        "createdAt" to Timestamp.now()
                    )
                    firestoreRepo.saveResumeAnalysis(userId, analysisId, data)
                }

                Result.success(analysis)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }


    suspend fun analyzePdfBytes(pdfBytes: ByteArray): Result<AnalysisResponseDto> =
        withContext(Dispatchers.IO) {
            try {
                val requestFile = pdfBytes.toRequestBody("application/pdf".toMediaType())
                val filePart = MultipartBody.Part.createFormData(
                    name = "file",
                    filename = "resume.pdf",
                    body = requestFile
                )

                val ocr = api.extractPdf(filePart)

                val analysis = api.analyzeResume(
                    AnalysisRequestDto(
                        resume_text = ocr.extracted_text
                    )
                )

                // ---- Save in Firestore ----
                val userId = authRepo.currentUser?.uid
                if (userId != null) {
                    val analysisId = System.currentTimeMillis().toString()
                    val data = mapOf(
                        "analysisId" to analysisId,
                        "source" to "pdf",
                        "score" to analysis.score,
                        "summary" to analysis.summary,
                        "resumeText" to ocr.extracted_text,             // FIXED
                        "suggestions" to analysis.suggestions.map { s ->
                            mapOf(
                                "category" to s.category,
                                "issue" to s.issue,
                                "recommendation" to s.recommendation
                            )
                        },
                        "suggestionCount" to analysis.suggestions.size,  // FIXED
                        "createdAt" to Timestamp.now()
                    )
                    firestoreRepo.saveResumeAnalysis(userId, analysisId, data)
                }

                Result.success(analysis)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }


    suspend fun getAnalysisById(analysisId: String): Result<AnalysisResponseDto> =
        withContext(Dispatchers.IO) {
            try {
                val userId = authRepo.currentUser?.uid 
                    ?: return@withContext Result.failure(Exception("User not authenticated"))
                
                val data = firestoreRepo.getResumeAnalysisById(userId, analysisId)
                    .getOrThrow()
                
                // Parse Firestore data back into AnalysisResponseDto
                @Suppress("UNCHECKED_CAST")
                val suggestionsData = data["suggestions"] as? List<Map<String, Any>> ?: emptyList()
                val suggestions = suggestionsData.map { s: Map<String, Any> ->
                    SuggestionDto(
                        category = s["category"] as String,
                        issue = s["issue"] as String,
                        recommendation = s["recommendation"] as String
                    )
                }
                
                val response = AnalysisResponseDto(
                    score = (data["score"] as Long).toInt(),
                    summary = data["summary"] as String,
                    suggestions = suggestions
                )
                
                Result.success(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

}
