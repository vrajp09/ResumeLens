package com.cs407.resumelens.auth

import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class AuthUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val isSignedIn: Boolean = Firebase.auth.currentUser != null
)

class AuthViewModel(
    private val repo: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState(isSignedIn = repo.currentUser != null))
    val state: StateFlow<AuthUiState> = _state

    fun signUp(email: String, password: String) {
        if (!validate(email, password)) return
        _state.value = _state.value.copy(loading = true, error = null)
        repo.signUp(
            email, password,
            onSuccess = { _state.value = AuthUiState(isSignedIn = true) },
            onError = { msg -> _state.value = AuthUiState(error = msg, isSignedIn = false) }
        )
    }

    fun signIn(email: String, password: String) {
        if (!validate(email, password)) return
        _state.value = _state.value.copy(loading = true, error = null)
        repo.signIn(
            email, password,
            onSuccess = { _state.value = AuthUiState(isSignedIn = true) },
            onError = { msg -> _state.value = AuthUiState(error = msg, isSignedIn = false) }
        )
    }

    fun signOut() {
        repo.signOut()
        _state.value = AuthUiState(isSignedIn = false)
    }

    fun clearError() { _state.value = _state.value.copy(error = null) }

    private fun validate(email: String, password: String): Boolean {
        if (email.isBlank()) {
            _state.value = _state.value.copy(error = "Email is empty")
            return false
        }
        val emailPattern = Regex("^[\\w.]+@([a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}$")
        if (!emailPattern.matches(email)) {
            _state.value = _state.value.copy(error = "Invalid Email Format")
            return false
        }
        if (password.isBlank()) {
            _state.value = _state.value.copy(error = "Password is empty")
            return false
        }
        if (password.length < 6) {
            _state.value = _state.value.copy(error = "Password is too short")
            return false
        }
        val hasLower = Regex("[a-z]").containsMatchIn(password)
        val hasUpper = Regex("[A-Z]").containsMatchIn(password)
        val hasDigit = Regex("\\d").containsMatchIn(password)
        if (!(hasLower && hasUpper && hasDigit)) {
            _state.value = _state.value.copy(
                error = "Password should contain at least one lowercase letter, one uppercase letter, and one digit"
            )
            return false
        }
        return true
    }
}
