package com.cs407.resumelens.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cs407.resumelens.R
import com.cs407.resumelens.data.UserViewModel
import com.cs407.resumelens.ui.components.ProfileMenu
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun DashboardScreen(
    onNavigateToPolishResume: () -> Unit = {},
    onNavigateToResumeAnalysis: (String?) -> Unit = {},
    onOpenProfile: () -> Unit = {},
    onNavigateToProfileSettings: () -> Unit = {},
    onNavigateToResumeTips: () -> Unit = {},
    onSignOut: () -> Unit = {},
    userViewModel: UserViewModel = viewModel(),
    dashboardViewModel: com.cs407.resumelens.data.DashboardViewModel = viewModel()
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val profileDrawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val userState by userViewModel.state.collectAsStateWithLifecycle()
    val dashboardState by dashboardViewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Show error message in snackbar
    LaunchedEffect(dashboardState.errorMessage) {
        dashboardState.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
        }
    }
    
    // Pull-to-refresh state
    val refreshing = dashboardState.isLoading
    val pullRefreshState = rememberPullRefreshState(
        refreshing = refreshing,
        onRefresh = { dashboardViewModel.refresh() }
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        //onDismissRequest = { scope.launch { drawerState.close() } },
        drawerContent = {
            ProfileMenu(
                userName = userState.userProfile?.name ?: "User",
                username = userState.userProfile?.username ?: "",
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
                    userName = userState.userProfile?.name ?: "User",
                    userEmail = userState.userProfile?.email ?: "",
                    username = userState.userProfile?.username ?: "",
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
                },
                snackbarHost = { SnackbarHost(snackbarHostState) }
            ) { padding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .pullRefresh(pullRefreshState)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                    ) {

                        Text("Total Resume Edits", fontSize = 16.sp, color = Color.Gray)
                    Text(
                        text = "${dashboardState.totalEdits}",
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(12.dp))

                    // Graph: Hide if empty, show bars if data exists
                    if (dashboardState.graphBars.isEmpty()) {
                        // Empty state message
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Upload your resume to see activity here.",
                                color = Color.Gray,
                                fontSize = 14.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    } else {
                        // Show bars
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
                            dashboardState.graphBars.forEach { score ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(maxOf(score.coerceIn(0, 100), 4).dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(Color(0xFF9E9E9E))
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        StatCard(
                            title = "Resume Corrections",
                            value = "${dashboardState.totalCorrections}",
                            icon = R.drawable.resume_icon,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "AI Checker",
                            value = "${dashboardState.aiCheckerPercent}%",
                            icon = R.drawable.resume_icon,
                            modifier = Modifier.weight(1f)
                        )
                    }


                    Spacer(Modifier.height(20.dp))

                    Text("Resume History", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(10.dp))

                    LazyColumn {
                        if (dashboardState.historyItems.isEmpty()) {
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "No resumes analyzed yet",
                                        color = Color.Gray,
                                        fontSize = 16.sp
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        text = "Tap the + button to get started!",
                                        color = Color.Gray,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        } else {
                            items(dashboardState.historyItems) { historyItem ->
                                ResumeHistoryItem(
                                    title = historyItem.versionLabel,
                                    corrections = historyItem.correctionsCount,
                                    suggestions = historyItem.suggestionsCount,
                                    onClick = {
                                        onNavigateToResumeAnalysis(historyItem.analysisId)
                                    }
                                )
                            }
                        }
                    }
                }
                    
                    // Loading indicator overlay
                    if (dashboardState.isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color.White)
                        }
                    }
                    
                    // Pull-to-refresh indicator
                    PullRefreshIndicator(
                        refreshing = refreshing,
                        state = pullRefreshState,
                        modifier = Modifier.align(Alignment.TopCenter)
                    )
                }
            }
        }
    }
}

// Profile Sidebar Component (modal drawer overlay)
@Composable
private fun ProfileSidebar(
    userName: String = "User",
    userEmail: String = "",
    username: String = "",
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
                Text(userName, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                if (username.isNotBlank()) {
                    Text("@$username", color = Color.Gray, fontSize = 14.sp)
                } else {
                    Text("Premium account", color = Color.Gray, fontSize = 14.sp)
                }
            }

            Spacer(Modifier.height(24.dp))

            if (userEmail.isNotBlank()) {
                InfoItem("Email", userEmail)
            }
            if (username.isNotBlank()) {
                InfoItem("Username", username)
            }

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
