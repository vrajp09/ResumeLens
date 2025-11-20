package com.cs407.resumelens.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cs407.resumelens.auth.AuthViewModel
import com.cs407.resumelens.data.UserViewModel


sealed class Screen(val route: String) {
    // Auth Flow
    data object Welcome : Screen("welcome")
    data object SignUp  : Screen("signup")
    data object LogIn   : Screen("login")
    
    // Main App Flow
    data object Dashboard : Screen("dashboard")
    data object PolishResume : Screen("polish_resume")
    data object Camera : Screen("camera")
    data object ResumeAnalysis : Screen("resume_analysis")
    
    // Profile & Settings
    data object ProfileSettings : Screen("profile_settings")
    
    // Helpers for navigation arguments
    companion object {
        const val RESUME_ID_ARG = "resumeId"
        fun resumeAnalysis(resumeId: String? = null) = if (resumeId != null) {
            "resume_analysis?analysisId=$resumeId"
        } else {
            "resume_analysis"
        }
    }
}


@Composable
fun ResumeLensApp() {
    val nav = rememberNavController()
    val authVm: AuthViewModel = viewModel()
    val analysisVm: com.cs407.resumelens.analysis.ResumeAnalysisViewModel = viewModel()


    // Collect the flow into Compose state (lifecycle-aware)
    val authState by authVm.state.collectAsStateWithLifecycle()

    // Navigate whenever sign-in state changes
    LaunchedEffect(authState.isSignedIn) {
        if (authState.isSignedIn) {
            nav.navigate(Screen.Dashboard.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    // Use a fixed start destination; navigate above if already signed in
    NavHost(
        navController = nav,
        startDestination = Screen.Welcome.route
    ) {
        composable(Screen.Welcome.route) {
            WelcomeScreen(
                onSignUp = { nav.navigate(Screen.SignUp.route) },
                onLogIn  = { nav.navigate(Screen.LogIn.route) }
            )
        }
        composable(Screen.SignUp.route) {
            SignUpScreen(
                onBack = { nav.popBackStack() },
                onSignUpComplete = { email, password, fullName, username ->
                    authVm.signUp(email, password, fullName, username)
                },
                errorText = authState.error,          // wire VM error into screen
                onClearError = authVm::clearError    // allow screen to clear it
            )
        }
        composable(Screen.LogIn.route) {
            LogInScreen(
                onBack = { nav.popBackStack() },
                onLogIn = { email, password -> authVm.signIn(email, password) },
                onRecoverPassword = { /* TODO */ },
                errorText = authState.error,
                onClearError = authVm::clearError
            )
        }
        composable(Screen.Dashboard.route) {
            val userVm: UserViewModel = viewModel()
            val dashboardVm: com.cs407.resumelens.data.DashboardViewModel = viewModel()
            
            LaunchedEffect(Unit) {
                userVm.refreshProfile()
                dashboardVm.loadDashboardData()
            }
            
            DashboardScreen(
                onNavigateToPolishResume = { nav.navigate(Screen.PolishResume.route) },
                onNavigateToResumeAnalysis = { resumeId ->
                    nav.navigate(Screen.resumeAnalysis(resumeId))
                },
                onOpenProfile = { /* Profile handled as drawer overlay */ },
                onNavigateToProfileSettings = { nav.navigate(Screen.ProfileSettings.route) },
                onNavigateToResumeTips = { /* TODO: Implement resume tips screen */ },
                onSignOut = {
                    authVm.signOut()
                    nav.navigate(Screen.Welcome.route) { popUpTo(0) { inclusive = true } }
                },
                userViewModel = userVm,
                dashboardViewModel = dashboardVm
            )
        }
        composable(Screen.PolishResume.route) {
            PolishResumeScreen(
                onBack = { nav.popBackStack() },
                onContinue = { nav.navigate(Screen.Camera.route) },

                onFileSelected = { uri ->
                    analysisVm.setPendingImageUri(uri)
                    nav.navigate("resume_analysis")
                },

                onPdfSelected = { uri ->
                    analysisVm.setPendingPdfUri(uri)
                    nav.navigate("resume_analysis")
                }
            )

        }

        composable(Screen.Camera.route) {
            CameraScreen(
                onBack = { nav.popBackStack() },
                onPhotoTaken = { imageUri ->
                    analysisVm.setPendingImageUri(imageUri)
                    nav.navigate("resume_analysis") {
                        popUpTo(Screen.Dashboard.route) { inclusive = false }
                    }
                }
            )
        }

        composable(
            route = "resume_analysis?analysisId={analysisId}",
            arguments = listOf(
                navArgument("analysisId") { 
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val analysisId = backStackEntry.arguments?.getString("analysisId")
            
            ResumeAnalysisScreen(
                viewModel = analysisVm,
                analysisId = analysisId,
                onBack = { nav.popBackStack() },
                onImproveScore = {
                    // Loop back to PolishResume
                    nav.navigate(Screen.PolishResume.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = false }
                    }
                }
            )
        }

        composable(Screen.ProfileSettings.route) {
            val userVm: UserViewModel = viewModel()
            LaunchedEffect(Unit) {
                userVm.refreshProfile()
            }
            ProfileSettingsScreen(
                onBack = { nav.popBackStack() },
                onSignOut = {
                    authVm.signOut()
                    nav.navigate(Screen.Welcome.route) { popUpTo(0) { inclusive = true } }
                },
                userViewModel = userVm
            )
        }
    }
}