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

@Composable
fun ResumeAnalysisScreen(
    onBack: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Back Button
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

        Spacer(modifier = Modifier.height(40.dp))

        // Center illustration
        Image(
            painter = painterResource(id = R.drawable.resume_icon),
            contentDescription = "Resume Icon",
            modifier = Modifier.size(100.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
            color = Color.Gray,
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Score Circles
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ScoreCircle(label = "Overall", score = 75)
            ScoreCircle(label = "ATS", score = 60)
            ScoreCircle(label = "Relevance", score = 90)
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Improve Score Button
        Button(
            onClick = { /* navigate later */ },
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewResumeAnalysisScreen() {
    ResumeLensTheme {
        ResumeAnalysisScreen()
    }
}
