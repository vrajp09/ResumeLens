package com.cs407.resumelens.analysis

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cs407.resumelens.network.SuggestionDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ResumeAnalysisUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val score: Int? = null,
    val summary: String? = null,
    val suggestions: List<SuggestionDto> = emptyList()
)

class ResumeAnalysisViewModel(
    private val repository: ResumeAnalysisRepository = ResumeAnalysisRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(ResumeAnalysisUiState())
    val state: StateFlow<ResumeAnalysisUiState> = _state

    private var pendingImageUri: Uri? = null

    private var pendingPdfUri: Uri? = null

    fun setPendingPdfUri(uri: Uri?) {
        pendingPdfUri = uri
        _state.value = ResumeAnalysisUiState()
    }

    fun consumePendingPdfUri(): Uri? {
        val current = pendingPdfUri
        pendingPdfUri = null
        return current
    }


    fun setPendingImageUri(uri: Uri?) {
        pendingImageUri = uri
        _state.value = ResumeAnalysisUiState()
    }


    fun consumePendingImageUri(): Uri? {
        val current = pendingImageUri
        pendingImageUri = null
        return current
    }

    fun analyzeImageBytes(imageBytes: ByteArray) {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)

            repository.analyzeImageBytes(imageBytes)
                .onSuccess { resp ->
                    _state.value = _state.value.copy(
                        loading = false,
                        score = resp.score,
                        summary = resp.summary,
                        suggestions = resp.suggestions
                    )
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(
                        loading = false,
                        error = e.message ?: "Analysis failed. Please try again."
                    )
                }
        }
    }

    fun analyzePdfBytes(pdfBytes: ByteArray) {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)

            repository.analyzePdfBytes(pdfBytes)
                .onSuccess { resp ->
                    _state.value = _state.value.copy(
                        loading = false,
                        score = resp.score,
                        summary = resp.summary,
                        suggestions = resp.suggestions
                    )
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(
                        loading = false,
                        error = e.message ?: "Failed to analyze PDF."
                    )
                }
        }
    }




    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}
