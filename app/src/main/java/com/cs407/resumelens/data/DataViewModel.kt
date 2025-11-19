package com.cs407.resumelens.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cs407.resumelens.auth.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class DataUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val data: Map<String, Any>? = null
)

class DataViewModel(
    private val firestoreRepo: FirestoreRepository = FirestoreRepository(),
    private val authRepo: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(DataUiState())
    val state: StateFlow<DataUiState> = _state

    fun saveUserProfile(data: Map<String, Any>) {
        val userId = authRepo.currentUser?.uid ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            firestoreRepo.saveUserProfile(userId, data)
                .onSuccess {
                    _state.value = _state.value.copy(loading = false)
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(
                        loading = false,
                        error = e.message ?: "Failed to save profile"
                    )
                }
        }
    }

    fun loadUserProfile() {
        val userId = authRepo.currentUser?.uid ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            firestoreRepo.getUserProfile(userId)
                .onSuccess { data ->
                    _state.value = _state.value.copy(
                        loading = false,
                        data = data
                    )
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(
                        loading = false,
                        error = e.message ?: "Failed to load profile"
                    )
                }
        }
    }
}