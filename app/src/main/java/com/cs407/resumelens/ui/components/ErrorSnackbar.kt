package com.cs407.resumelens.ui.components

import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*

@Composable
fun ErrorSnackbar(
    errorText: String?,
    onCleared: () -> Unit = {}
) {
    val host = remember { SnackbarHostState() }

    LaunchedEffect(errorText) {
        if (!errorText.isNullOrBlank()) {
            host.showSnackbar(errorText)
            onCleared()
        }
    }
    SnackbarHost(hostState = host)
}
