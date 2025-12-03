package com.cs407.resumelens.ui.screens

import androidx.compose.foundation.layout.*
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
fun SecurityScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Security", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(id = R.drawable.back_button), contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text("Manage your account's security", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(24.dp))

            // We will make this button functional in a later step if needed
            Button(onClick = { /* TODO: Navigate to change password screen */ }) {
                Text("Change Password")
            }

            Spacer(modifier = Modifier.height(32.dp))

            InfoItem(
                title = "Two-Factor Authentication (2FA)",
                description = "2FA is not yet enabled for your account. This feature is coming soon to provide an extra layer of security."
            )
            InfoItem(
                title = "Data Encryption",
                description = "Your personal information and resume data are securely encrypted both in transit and when stored on our servers."
            )
        }
    }
}

@Composable
private fun InfoItem(title: String, description: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(description, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Divider(modifier = Modifier.padding(top = 12.dp))
    }
}
