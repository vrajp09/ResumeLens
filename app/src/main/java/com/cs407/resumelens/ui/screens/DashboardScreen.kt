package com.cs407.resumelens.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.cs407.resumelens.ui.components.ProfileMenu
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToPolishResume: () -> Unit = {},
    onNavigateToResumeAnalysis: (String?) -> Unit = {},
    onOpenProfile: () -> Unit = {},
    onNavigateToProfileSettings: () -> Unit = {},
    onNavigateToResumeTips: () -> Unit = {},
    onSignOut: () -> Unit = {}
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val profileDrawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        //onDismissRequest = { scope.launch { drawerState.close() } },
        drawerContent = {
            ProfileMenu(
                onProfileClick = {
                    scope.launch {
                        drawerState.close()
                        profileDrawerState.open()
                    }
                },
                onSettingsClick = {
                    scope.launch {
                        drawerState.close()
                    }
                    onNavigateToProfileSettings()
                },
                onResumeTipsClick = {
                    scope.launch {
                        drawerState.close()
                    }
                    onNavigateToResumeTips()
                },
                onLogoutClick = {
                    scope.launch {
                        drawerState.close()
                    }
                    onSignOut()
                }
            )
        }
    ) {
        // Profile sidebar drawer
        ModalNavigationDrawer(
            drawerState = profileDrawerState,
            drawerContent = {
                ProfileSidebar(
                    onClose = { scope.launch { profileDrawerState.close() } },
                    onNavigateToSettings = {
                        scope.launch {
                            profileDrawerState.close()
                        }
                        onNavigateToProfileSettings()
                    }
                )
            }
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Dashboard", fontWeight = FontWeight.Bold) },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu")
                            }
                        },
                        actions = {
                            IconButton(onClick = onNavigateToPolishResume) {
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
                                suggestions = listOf(6, 7, 10)[index],
                                onClick = {
                                    // Navigate to resume analysis with resume ID
                                    onNavigateToResumeAnalysis("resume_${index + 1}")
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// Profile Sidebar Component (modal drawer overlay)
@Composable
private fun ProfileSidebar(
    onClose: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
        Column(
            modifier = Modifier
                .width(320.dp)
                .fillMaxHeight()
                .background(Color.White)
                .padding(24.dp)
        ) {
            // Close button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onClose) {
                    Icon(
                        painter = painterResource(id = R.drawable.back_button),
                        contentDescription = "Close",
                        tint = Color.Black
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Profile info
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

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = onNavigateToSettings,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B67A))
            ) {
                Text("Settings", color = Color.White)
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
private fun ResumeHistoryItem(
    title: String,
    corrections: Int,
    suggestions: Int,
    onClick: () -> Unit = {}
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
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
