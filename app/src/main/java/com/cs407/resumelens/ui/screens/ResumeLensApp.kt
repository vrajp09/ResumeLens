package com.cs407.resumelens.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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

    // If a Firebase user is already cached, go straight to Home.
    val start = if (authVm.state.value.isSignedIn) Screen.Home.route else Screen.Welcome.route

    // React to auth state changes: when signed in, go to Home
    LaunchedEffect(Unit) {
        authVm.state.collectLatest { st ->
            if (st.isSignedIn) {
                nav.navigate(Screen.Home.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }

    NavHost(navController = nav, startDestination = start) {
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
                    // Delegate to ViewModel; navigation will happen via the observer above
                    authVm.signUp(email, password)
                }
            )
        }
        composable(Screen.LogIn.route) {
            LogInScreen(
                onBack = { nav.popBackStack() },
                onLogIn = { email, password ->
                    authVm.signIn(email, password)
                },
                onRecoverPassword = { /* add later if desired */ },
                // pass through errors from VM (optional but handy)
                errorText = authVm.state.value.error,
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