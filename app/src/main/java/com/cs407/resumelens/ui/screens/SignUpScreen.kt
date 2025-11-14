package com.cs407.resumelens.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cs407.resumelens.R

@Composable
fun SignUpScreen(
    onBack: () -> Unit,
    onSignUpComplete: (email: String, password: String) -> Unit,
    errorText: String? = null,
    onClearError: () -> Unit = {}
) {
    var fullName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var repeatPassword by rememberSaveable { mutableStateOf("") }
    var showPassword by rememberSaveable { mutableStateOf(false) }

    // Map ViewModel error to correct field
    val emailFieldError: String? = when (errorText) {
        "Email is empty",
        "Invalid Email Format" -> errorText
        else -> null
    }

    val passwordFieldError: String? = when (errorText) {
        "Password is empty",
        "Password is too short",
        "Password should contain at least one lowercase letter, one uppercase letter, and one digit" -> errorText
        else -> null
    }

    // Local repeat-password mismatch
    val repeatMismatch: Boolean = remember(password, repeatPassword) {
        repeatPassword.isNotBlank() && password.isNotBlank() && repeatPassword != password
    }

    // Any other VM error (e.g., Firebase collision) goes at the top
    val topError: String? =
        if (errorText != null && emailFieldError == null && passwordFieldError == null) errorText else null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(R.drawable.header),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .padding(top = 8.dp)
        )

        Spacer(Modifier.height(16.dp))

        Text("Sign up", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text(
            "Create an account",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(12.dp))

        // Optional banner for non-field errors
        if (topError != null) {
            Text(
                text = topError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(8.dp))
        }

        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("Full name") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                // you could call onClearError() here if you want live clearing
            },
            label = { Text("Enter your email") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            singleLine = true,
            isError = emailFieldError != null,
            supportingText = {
                if (emailFieldError != null) {
                    Text(emailFieldError, color = MaterialTheme.colorScheme.error)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Create a username") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Create your password") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            trailingIcon = {
                TextButton(onClick = { showPassword = !showPassword }) {
                    Text(if (showPassword) "Hide" else "Show")
                }
            },
            singleLine = true,
            isError = passwordFieldError != null,
            supportingText = {
                if (passwordFieldError != null) {
                    Text(passwordFieldError, color = MaterialTheme.colorScheme.error)
                }
            },
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = repeatPassword,
            onValueChange = { repeatPassword = it },
            label = { Text("Repeat password") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            singleLine = true,
            isError = repeatMismatch,
            supportingText = {
                if (repeatMismatch) {
                    Text("Passwords do not match", color = MaterialTheme.colorScheme.error)
                }
            },
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(20.dp))

        val enabled = fullName.isNotBlank() &&
                email.isNotBlank() &&
                username.isNotBlank() &&
                password.isNotBlank() &&
                repeatPassword.isNotBlank() &&
                !repeatMismatch

        Button(
            onClick = {
                // VM will set error for invalid email/password; we don't clear here
                onSignUpComplete(email, password)
            },
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(14.dp)
        ) { Text("Sign up") }

        Spacer(Modifier.height(18.dp))
        TextButton(onClick = onBack) { Text("Back") }

        Spacer(Modifier.height(18.dp))
    }
}