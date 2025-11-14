package com.cs407.resumelens.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
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
import com.android.car.ui.toolbar.MenuItem
import com.cs407.resumelens.R

@Composable
fun ProfileMenu(
    onProfileClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onResumeTipsClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .width(280.dp)
            .background(Color.White, RoundedCornerShape(20.dp))
            .padding(vertical = 20.dp)
    ) {
        // Header Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.default_profile_pic),
                contentDescription = "Profile picture",
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(50))
            )
            Spacer(Modifier.height(10.dp))
            Text("Brittany Dinan", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("@b.dinan5", color = Color.Gray, fontSize = 14.sp)
        }

        Spacer(Modifier.height(16.dp))
        Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)

        // Menu Items
        Column(modifier = Modifier.padding(top = 8.dp)) {
            MenuItem(icon = R.drawable.profile_logo, text = "Profile", onClick = onProfileClick)
            MenuItem(icon = R.drawable.settings_icon, text = "Settings", onClick = onSettingsClick)
            MenuItem(icon = R.drawable.resume_icon, text = "Resume Tips", onClick = onResumeTipsClick)
        }

        Divider(
            color = Color(0xFFE0E0E0),
            thickness = 1.dp,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        MenuItem(icon = R.drawable.logout_icon, text = "Log out", onClick = onLogoutClick)
    }
}

@Composable
private fun MenuItem(icon: Int, text: String, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = text,
            tint = Color.Black,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.width(14.dp))
        Text(text, fontSize = 16.sp)
    }
}
