package com.cs407.resumelens.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cs407.resumelens.auth.AuthViewModel
import kotlinx.coroutines.flow.collectLatest

sealed class Screen(val route: String) {
    data object Welcome : Screen("welcome")
    data object SignUp  : Screen("signup")
    data object LogIn   : Screen("login")
    data object Home    : Screen("home")
}

@Composable
fun ResumeLensApp() {
    val nav = rememberNavController()
    val authVm: AuthViewModel = viewModel()

    // Collect the flow into Compose state (lifecycle-aware)
    val authState by authVm.state.collectAsStateWithLifecycle()

    // Navigate whenever sign-in state changes
    LaunchedEffect(authState.isSignedIn) {
        if (authState.isSignedIn) {
            nav.navigate(Screen.Home.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    // Use a fixed start destination; we'll navigate above if already signed in
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
                onSignUpComplete = { email, password ->
                    authVm.signUp(email, password)
                }
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
        composable(Screen.Home.route) {
            HomeScreen(
                onSignOut = {
                    authVm.signOut()
                    nav.navigate(Screen.Welcome.route) { popUpTo(0) { inclusive = true } }
                }
            )
        }
    }
}