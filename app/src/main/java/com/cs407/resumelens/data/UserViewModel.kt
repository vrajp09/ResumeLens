package com.cs407.resumelens.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cs407.resumelens.auth.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class UserProfile(
    val name: String? = null,
    val email: String? = null,
    val username: String? = null
)

data class UserUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val userProfile: UserProfile? = null
)

class UserViewModel(
    private val firestoreRepo: FirestoreRepository = FirestoreRepository(),
    private val authRepo: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(UserUiState())
    val state: StateFlow<UserUiState> = _state

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        val userId = authRepo.currentUser?.uid ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            firestoreRepo.getUserProfile(userId)
                .onSuccess { data ->
                    val profile = if (data != null) {
                        UserProfile(
                            name = data["name"] as? String,
                            email = data["email"] as? String ?: authRepo.currentUser?.email,
                            username = data["username"] as? String
                        )
                    } else {
                        // If no profile exists, use auth user email
                        UserProfile(
                            email = authRepo.currentUser?.email
                        )
                    }
                    _state.value = _state.value.copy(
                        loading = false,
                        userProfile = profile
                    )
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(
                        loading = false,
                        error = e.message ?: "Failed to load profile",
                        userProfile = UserProfile(
                            email = authRepo.currentUser?.email
                        )
                    )
                }
        }
    }

    fun refreshProfile() {
        loadUserProfile()
    }
}

