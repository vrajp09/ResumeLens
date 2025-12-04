package com.cs407.resumelens.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cs407.resumelens.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpCenterScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Help Center", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(id = R.drawable.back_button), contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                Text("Frequently Asked Questions", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                FAQItem(
                    question = "How do I upload a resume?",
                    answer = "From the dashboard, tap the '+' button. You can then choose to upload a resume file (PDF, JPG, PNG) from your device or take a photo of your resume with your camera."
                )
            }
            item {
                FAQItem(
                    question = "How is my resume score calculated?",
                    answer = "Our AI analyzes your resume based on several factors including keyword relevance, formatting, action verb usage, and clarity. The score is a general indicator of how well your resume might perform with ATS (Applicant Tracking Systems)."
                )
            }
            item {
                Text(
                    "Contact Us",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 24.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("For further assistance, please email us at support@resumelens.com.")
            }
        }
    }
}

@Composable
private fun FAQItem(question: String, answer: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(question, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(answer, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Divider(modifier = Modifier.padding(top = 12.dp))
    }
}
