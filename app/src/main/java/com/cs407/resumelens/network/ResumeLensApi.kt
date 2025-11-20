package com.cs407.resumelens.network

import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part


data class OcrResponse(
    val extracted_text: String
)

data class SuggestionDto(
    val category: String,
    val issue: String,
    val recommendation: String
)

data class AnalysisRequestDto(
    val resume_text: String,
    val target_role: String? = null
)

data class AnalysisResponseDto(
    val score: Int,
    val summary: String,
    val suggestions: List<SuggestionDto>
)


interface ResumeLensApi {

    @Multipart
    @POST("extract_pdf")
    suspend fun extractPdf(
        @Part file: MultipartBody.Part
    ): OcrResponse

    // POST /extract  (multipart resume image)
    @Multipart
    @POST("extract")
    suspend fun extractText(
        @Part file: MultipartBody.Part
    ): OcrResponse

    // POST /analyze  (resume text + optional role)
    @POST("analyze")
    suspend fun analyzeResume(
        @Body request: AnalysisRequestDto
    ): AnalysisResponseDto

}
