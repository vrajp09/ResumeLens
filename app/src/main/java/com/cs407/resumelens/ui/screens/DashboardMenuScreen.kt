package com.cs407.resumelens.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
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
fun DashboardMenuScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            shadowElevation = 8.dp,
            modifier = Modifier
                .width(320.dp)
                .wrapContentHeight()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile Section
                Image(
                    painter = painterResource(id = R.drawable.default_profile_pic),
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(40.dp))
                )

                Spacer(modifier = Modifier.height(12.dp))
                Text("Brittany Dinan", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("@b.dinan5", color = Color.Gray, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(24.dp))

                Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))

                // Menu Items
                MenuItem(R.drawable.profile_logo, "Profile")
                MenuItem(R.drawable.settings_icon, "Settings")
                MenuItem(R.drawable.resume_icon, "Resume Tips")

                Spacer(modifier = Modifier.height(8.dp))
                Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))
                MenuItem(R.drawable.logout_icon, "Log out")
            }
        }
    }
}

@Composable
fun MenuItem(iconRes: Int, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = text,
            tint = Color.Black,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = text, fontSize = 16.sp, color = Color.Black)
    }
}

@Preview(showSystemUi = true)
@Composable
fun PreviewDashboardMenuScreen() {
    ResumeLensTheme {
        DashboardMenuScreen()
    }
}
