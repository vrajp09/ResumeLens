package com.cs407.resumelens.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cs407.resumelens.R
import com.cs407.resumelens.data.UserViewModel
import com.cs407.resumelens.ui.theme.ResumeLensTheme

@Composable
fun ProfileSettingsScreen(
    onBack: () -> Unit = {},
    onSignOut: () -> Unit = {},
    userViewModel: UserViewModel = viewModel()
) {
    val userState by userViewModel.state.collectAsStateWithLifecycle()
    
    LaunchedEffect(Unit) {
        userViewModel.refreshProfile()
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    painter = painterResource(id = R.drawable.back_button),
                    contentDescription = "Back"
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Settings",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.weight(1f))

            IconButton(onClick = { /* handle notifications later */ }) {
                Icon(
                    painter = painterResource(id = R.drawable.bell),
                    contentDescription = "Notifications"
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.default_profile_pic),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(30.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = userState.userProfile?.name ?: "User",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                if (userState.userProfile?.username?.isNotBlank() == true) {
                    Text(
                        text = "@${userState.userProfile?.username}",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                } else {
                    Text(
                        text = userState.userProfile?.email ?: "",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        Text(
            text = "Options",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(10.dp))

        SimpleSettingOption(R.drawable.theme, "Theme")
        SimpleSettingOption(R.drawable.card, "Manage Subscription")
        SimpleSettingOption(R.drawable.security, "Security")
        SimpleSettingOption(R.drawable.help, "Help Center")

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = onSignOut,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text(text = "Log out", color = Color.White, fontSize = 16.sp)
        }
    }
}

@Composable
fun SimpleSettingOption(iconId: Int, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* handle click later */ }
            .padding(vertical = 8.dp)
    ) {
        Icon(
            painter = painterResource(id = iconId),
            contentDescription = label,
            tint = Color.Black,
            modifier = Modifier.size(22.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(text = label, fontSize = 16.sp, color = Color.Black)
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewProfileSettingsScreen() {
    ResumeLensTheme {
        ProfileSettingsScreen()
    }
}
