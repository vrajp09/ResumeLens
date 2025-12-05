package com.cs407.resumelens.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cs407.resumelens.auth.AuthRepository
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class HistoryItem(
    val analysisId: String,
    val versionLabel: String,
    val correctionsCount: Int,
    val suggestionsCount: Int,
    val score: Int
)

data class GraphBarData(
    val score: Int,
    val analysisId: String,
    val createdAt: Timestamp
)

data class DashboardUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val totalEdits: Int = 0,
    val totalCorrections: Int = 0,
    val aiCheckerPercent: Int = 0,
    val graphBars: List<Int> = emptyList(),
    val graphBarData: List<GraphBarData> = emptyList(),
    val historyItems: List<HistoryItem> = emptyList()
)

data class AnalysisData(
    val analysisId: String,
    val score: Int,
    val suggestionCount: Int,
    val createdAt: Timestamp
)

class DashboardViewModel(
    private val firestoreRepo: FirestoreRepository = FirestoreRepository(),
    private val authRepo: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardUiState())
    val state: StateFlow<DashboardUiState> = _state

    fun loadDashboardData() {
        val userId = authRepo.currentUser?.uid ?: return

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)

            firestoreRepo.getResumeAnalyses(userId)
                .onSuccess { rawAnalyses ->
                    val analyses = parseAnalyses(rawAnalyses)
                    val uiState = computeDashboardState(analyses)
                    _state.value = uiState.copy(isLoading = false)
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Failed to load dashboard data"
                    )
                }
        }
    }

    private fun parseAnalyses(rawData: List<Map<String, Any>>): List<AnalysisData> {
        return rawData.mapNotNull { data ->
            try {
                val analysisId = data["analysisId"] as? String ?: return@mapNotNull null
                val score = (data["score"] as? Long)?.toInt() ?: return@mapNotNull null
                val createdAt = data["createdAt"] as? Timestamp ?: return@mapNotNull null
                
                // Use null-safe fallback for suggestionCount
                val suggestionCount = (data["suggestionCount"] as? Long?)?.toInt()
                    ?: (data["suggestions"] as? List<*>)?.size
                    ?: 0

                AnalysisData(
                    analysisId = analysisId,
                    score = score,
                    suggestionCount = suggestionCount,
                    createdAt = createdAt
                )
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun computeDashboardState(analyses: List<AnalysisData>): DashboardUiState {
        val totalEdits = analyses.size

        val totalCorrections = analyses.sumOf { it.suggestionCount }

        val aiCheckerPercent = if (analyses.isNotEmpty()) {
            analyses.map { it.score }.average().toInt()
        } else {
            0
        }

        val sortedAnalysesForGraph = analyses.sortedBy { it.createdAt }.takeLast(7)
        val graphBars = sortedAnalysesForGraph.map { it.score }
        val graphBarData = sortedAnalysesForGraph.map { analysis ->
            GraphBarData(
                score = analysis.score,
                analysisId = analysis.analysisId,
                createdAt = analysis.createdAt
            )
        }

        val sortedAnalyses = analyses.sortedByDescending { it.createdAt }
        val historyItems = sortedAnalyses.mapIndexed { index, analysis ->
            HistoryItem(
                analysisId = analysis.analysisId,
                versionLabel = "Resume_Version_${sortedAnalyses.size - index}",
                correctionsCount = analysis.suggestionCount,
                suggestionsCount = analysis.suggestionCount,
                score = analysis.score
            )
        }

        return DashboardUiState(
            totalEdits = totalEdits,
            totalCorrections = totalCorrections,
            aiCheckerPercent = aiCheckerPercent,
            graphBars = graphBars,
            graphBarData = graphBarData,
            historyItems = historyItems
        )
    }

    fun refresh() {
        loadDashboardData()
    }
}

