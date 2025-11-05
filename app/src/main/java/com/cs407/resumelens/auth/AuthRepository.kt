package com.cs407.resumelens.auth

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.auth.*

class AuthRepository(
    private val auth: FirebaseAuth = Firebase.auth
) {
    val currentUser get() = auth.currentUser

    fun signUp(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e ->
                // Friendlier messages for common cases
                val msg = when (e) {
                    is FirebaseAuthWeakPasswordException ->
                        "Password is too short"
                    is FirebaseAuthUserCollisionException ->
                        "An account already exists with that email"
                    is FirebaseAuthInvalidCredentialsException ->
                        "Invalid Email Format"
                    else -> e.message ?: "Sign up failed"
                }
                onError(msg)
            }
    }

    fun signIn(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e ->
                // Map common “no account” and bad password errors
                val msg = when (e) {
                    is FirebaseAuthInvalidUserException ->
                        "No account found for that email. Try signing up."
                    is FirebaseAuthInvalidCredentialsException ->
                        // Could be bad password or bad email format
                        "Incorrect password or invalid credentials"
                    else -> e.message ?: "Log in failed"
                }
                onError(msg)
            }
    }

    fun signOut() = auth.signOut()
}