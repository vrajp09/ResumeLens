package com.cs407.resumelens.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cs407.resumelens.R
import com.cs407.resumelens.ui.theme.ResumeLensTheme
import android.content.Context
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cs407.resumelens.analysis.ResumeAnalysisViewModel
import com.cs407.resumelens.network.SuggestionDto

// Citation- https://developer.android.com/reference/kotlin/androidx/compose/foundation/layout/Arrangement
@Composable
fun ResumeAnalysisScreen(
    viewModel: ResumeAnalysisViewModel,
    analysisId: String? = null,
    onBack: () -> Unit = {},
    onImproveScore: () -> Unit = {}
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        // Check if loading existing analysis
        if (analysisId != null) {
            viewModel.loadAnalysisById(analysisId)
            return@LaunchedEffect
        }
        
        // Handle new analysis from camera/PDF
        val imgUri = viewModel.consumePendingImageUri()
        if (imgUri != null) {
            val bytes = context.contentResolver.openInputStream(imgUri)?.readBytes()
            if (bytes != null) viewModel.analyzeImageBytes(bytes)
            return@LaunchedEffect
        }

        val pdfUri = viewModel.consumePendingPdfUri()
        if (pdfUri != null) {
            val bytes = context.contentResolver.openInputStream(pdfUri)?.readBytes()
            if (bytes != null) viewModel.analyzePdfBytes(bytes)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Image(
                    painter = painterResource(id = R.drawable.back_button),
                    contentDescription = "Back",
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Resume Analysis",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        when {
            uiState.loading -> {
                Spacer(modifier = Modifier.height(40.dp))
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Analyzing your resume...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }

            uiState.error != null -> {
                Spacer(modifier = Modifier.height(40.dp))
                Text(
                    text = uiState.error ?: "",
                    color = Color.Red,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            uiState.score != null -> {
                // Icon
                Image(
                    painter = painterResource(id = R.drawable.resume_icon),
                    contentDescription = "Resume Icon",
                    modifier = Modifier.size(100.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = uiState.summary ?: "",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // For now we only have one numeric score from backend.
                    ScoreCircle(label = "Overall", score = uiState.score ?: 0)
                    ScoreCircle(label = "ATS", score = uiState.score ?: 0)       // placeholder
                    ScoreCircle(label = "Relevance", score = uiState.score ?: 0) // placeholder
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (uiState.suggestions.isNotEmpty()) {
                    Text(
                        text = "Suggestions",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                    ) {
                        items(uiState.suggestions) { suggestion ->
                            SuggestionItem(suggestion)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            else -> {
                // No image or result yet
                Spacer(modifier = Modifier.height(40.dp))
                Text(
                    text = "Upload or capture a resume to get started.",
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onImproveScore,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(30.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF91D0AC))
        ) {
            Text("Improve Score", color = Color.White, fontSize = 16.sp)
        }
    }
}


@Composable
private fun ScoreCircle(label: String, score: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Color.Transparent)
                .border(width = 2.dp, color = Color(0xFF3CB371), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$score",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF3CB371)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "$label Score",
            color = Color.Black,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun SuggestionItem(suggestion: SuggestionDto) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFF5F5F5))
            .padding(12.dp)
    ) {
        Text(
            text = suggestion.category,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = suggestion.issue,
            color = Color.Gray,
            fontSize = 13.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = suggestion.recommendation,
            fontSize = 13.sp
        )
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewResumeAnalysisScreen() {
    ResumeLensTheme {
        val fakeVm = com.cs407.resumelens.analysis.ResumeAnalysisViewModel()
        ResumeAnalysisScreen(viewModel = fakeVm)
    }
}
