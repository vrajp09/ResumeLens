package com.cs407.resumelens.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cs407.resumelens.R
import com.cs407.resumelens.ui.theme.ResumeLensTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResumeTipsScreen(onBack: () -> Unit = {}) {
    Scaffold(
            topBar = {
                TopAppBar(
                        title = { Text("Resume Tips", fontWeight = FontWeight.Bold) },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(
                                        painterResource(id = R.drawable.back_button),
                                        contentDescription = "Back"
                                )
                            }
                        }
                )
            }
    ) { padding ->
        LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp)
        ) {
            item {
                Text(
                        "ATS Optimization Tips",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00B67A)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                        "Follow these best practices to create an ATS-friendly resume that stands out.",
                        fontSize = 14.sp,
                        color = Color.Gray
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                TipSection(
                        title = "Keywords & Content",
                        tips =
                                listOf(
                                        TipItem(
                                                "Use Job-Specific Keywords",
                                                "Include technologies, frameworks, and skills mentioned in the job description. For software engineering: algorithms, data structures, system design, testing, CI/CD."
                                        ),
                                        TipItem(
                                                "Quantify Your Achievements",
                                                "Use numbers and metrics. Instead of 'improved performance,' say 'reduced load time by 40% for 10K+ users.'"
                                        ),
                                        TipItem(
                                                "Strong Action Verbs",
                                                "Start bullets with impactful verbs: Built, Engineered, Optimized, Architected, Implemented, Reduced, Increased."
                                        )
                                )
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            item {
                TipSection(
                        title = "Formatting",
                        tips =
                                listOf(
                                        TipItem(
                                                "Simple, Clean Layout",
                                                "Avoid tables, text boxes, headers/footers, or graphics. ATS systems struggle with complex formatting."
                                        ),
                                        TipItem(
                                                "Standard Section Headers",
                                                "Use clear labels: Education, Experience, Skills, Projects. ATS looks for these keywords."
                                        ),
                                        TipItem(
                                                "Consistent Formatting",
                                                "Use the same date format, bullet style, and spacing throughout your resume."
                                        )
                                )
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            item {
                TipSection(
                        title = "Structure",
                        tips =
                                listOf(
                                        TipItem(
                                                "Organized Skills Section",
                                                "Group skills into categories: Programming Languages, Frameworks, Databases, Tools & Platforms."
                                        ),
                                        TipItem(
                                                "Recent Graduates",
                                                "Lead with Education and Projects. Include GPA if above 3.5. List relevant coursework."
                                        ),
                                        TipItem(
                                                "Experienced Professionals",
                                                "Lead with Experience. Focus on impact and results. Projects section is optional."
                                        )
                                )
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Common Mistakes
            item {
                TipSection(
                        title = "Common Mistakes to Avoid",
                        tips =
                                listOf(
                                        TipItem(
                                                "Generic Descriptions",
                                                "Avoid vague phrases like 'responsible for' or 'worked on.' Be specific about what YOU accomplished."
                                        ),
                                        TipItem(
                                                "Typos and Errors",
                                                "Proofread carefully. Spelling errors can get your resume rejected immediately."
                                        ),
                                        TipItem(
                                                "Too Long or Too Short",
                                                "Entry-level (0-3 years): 1 page. Mid-level (3-7 years): 1-2 pages. Senior (7+ years): 2 pages max."
                                        )
                                )
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            item {
                ProTipCard(
                        title = "Pro Tip",
                        tip =
                                "Tailor your resume for each job application. Adjust keywords and emphasize relevant experience to match the specific role you're applying for."
                )
            }
        }
    }
}

@Composable
private fun TipSection(title: String, tips: List<TipItem>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 12.dp)
        )

        tips.forEach { tip ->
            TipCard(tip = tip)
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun TipCard(tip: TipItem) {
    Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                    text = tip.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.Black
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                    text = tip.description,
                    fontSize = 14.sp,
                    color = Color.DarkGray,
                    lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun ProTipCard(title: String, tip: String) {
    Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF00B67A)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = tip, fontSize = 14.sp, color = Color.DarkGray, lineHeight = 20.sp)
        }
    }
}

private data class TipItem(val title: String, val description: String)

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewResumeTipsScreen() {
    ResumeLensTheme { ResumeTipsScreen() }
}
