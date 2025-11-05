package com.cs407.resumelens.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { /* open drawer later */ }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                },
                actions = {
                    IconButton(onClick = { /* new upload later */ }) {
                            Icon(Icons.Default.Add, contentDescription = "Add")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {

            Text("Total Resume Edits", fontSize = 16.sp, color = Color.Gray)
            Text("432", fontSize = 40.sp, fontWeight = FontWeight.Bold)

            Spacer(Modifier.height(12.dp))


            //Citation- https://github.com/developerchunk/BarGraph-JetpackCompose
            // Citation- https://stackoverflow.com/questions/66955541/create-list-of-lists-in-ktlin
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .padding(vertical = 8.dp)
            ) {
                val heights = listOf(40, 80, 60, 100, 90, 70, 50)
                heights.forEach {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(it.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xFF9E9E9E))
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                StatCard(
                    title = "Resume Corrections",
                    value = "30",
                    icon = R.drawable.resume_icon,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "AI Checker",
                    value = "80%",
                    icon = R.drawable.resume_icon,
                    modifier = Modifier.weight(1f)
                )
            }


            Spacer(Modifier.height(20.dp))

            Text("Resume History", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))

            LazyColumn {
                items(3) { index ->
                    ResumeHistoryItem(
                        title = "Resume_Version_${3 - index}",
                        corrections = listOf(5, 2, 7)[index],
                        suggestions = listOf(6, 7, 10)[index]
                    )
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: Int,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        modifier = modifier.height(80.dp)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(title, fontSize = 14.sp, color = Color.Gray)
            Text(value, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }
    }
}


@Composable
private fun ResumeHistoryItem(title: String, corrections: Int, suggestions: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.resume_icon),
            contentDescription = null,
            modifier = Modifier.size(32.dp)
        )
        Spacer(Modifier.width(12.dp))
        Column {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(
                "$corrections Corrections, $suggestions Suggestions",
                color = Color.Gray,
                fontSize = 14.sp
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewDashboardScreen() {
    com.cs407.resumelens.ui.theme.ResumeLensTheme {
        DashboardScreen()
    }
}
