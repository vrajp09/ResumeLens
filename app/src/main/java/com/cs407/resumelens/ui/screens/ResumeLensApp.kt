package com.cs407.resumelens.ui.screens

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun ResumeLensApp() {
    val nav = rememberNavController()

    NavHost(
        navController = nav,
        startDestination = Screen.Welcome.route
    ) {
        composable(Screen.Welcome.route) {
            WelcomeScreen(
                onSignUp = { nav.navigate(Screen.SignUp.route) },
                onLogIn = { nav.navigate(Screen.LogIn.route) }
            )
        }
        composable(Screen.SignUp.route) {
            SignUpScreen(
                onBack = { nav.popBackStack() },
                onSignUpComplete = {
                    // After sign up you can route to login or a future Home screen
                    nav.navigate(Screen.LogIn.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = false }
                    }
                }
            )
        }
        composable(Screen.LogIn.route) {
            LogInScreen(
                onBack = { nav.popBackStack() },
                onLogIn = {
                    // TODO route to Home when you add it
                    // For now we just pop back to welcome
                    nav.popBackStack(Screen.Welcome.route, false)
                },
                onRecoverPassword = { /* TODO */ }
            )
        }
    }
}

sealed class Screen(val route: String) {
    data object Welcome : Screen("welcome")
    data object SignUp : Screen("signup")
    data object LogIn  : Screen("login")
}