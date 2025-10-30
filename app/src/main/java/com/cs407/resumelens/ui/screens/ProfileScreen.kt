package com.cs407.resumelens.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cs407.resumelens.R

@Composable
fun ProfileScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.back_button),
                contentDescription = "Back",
                tint = Color.Black,
                modifier = Modifier.size(28.dp)
            )
            Icon(
                painter = painterResource(id = R.drawable.settings_icon),
                contentDescription = "Settings",
                tint = Color.Black,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(Modifier.height(24.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(contentAlignment = Alignment.BottomEnd) {
                Image(
                    painter = painterResource(id = R.drawable.default_profile_pic),
                    contentDescription = "Profile picture",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(50))
                )
                Icon(
                    painter = painterResource(id = R.drawable.crown_icon),
                    contentDescription = "Premium",
                    tint = Color.Gray,
                    modifier = Modifier
                        .size(18.dp)
                        .offset(x = 4.dp, y = 4.dp)
                )
            }
            Spacer(Modifier.height(8.dp))
            Text("Brittany Dinan", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Text("Premium account", color = Color.Gray, fontSize = 14.sp)
        }

        Spacer(Modifier.height(24.dp))

        InfoItem("Email", "email@email.com")
        InfoItem("Phone", "(+123) 000 111 222 333")
        InfoItem("Location", "New York, USA")

        Spacer(Modifier.height(24.dp))

        Text("Achievements", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(Modifier.height(12.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFF2F2F2))
                .padding(16.dp)
        ) {
            Text("Quantified Impact", fontWeight = FontWeight.SemiBold)
            Text(
                "Add measurable results to 5+ bullet points",
                color = Color.Gray,
                fontSize = 14.sp
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Progress", color = Color.Gray, fontSize = 12.sp)
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .height(6.dp)
                        .width(150.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color.LightGray)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(30.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color.Black)
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text("1/5", fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
private fun InfoItem(label: String, value: String) {
    Column(Modifier.padding(vertical = 4.dp)) {
        Text(label, color = Color.Gray, fontSize = 14.sp)
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Medium)
    }
}
