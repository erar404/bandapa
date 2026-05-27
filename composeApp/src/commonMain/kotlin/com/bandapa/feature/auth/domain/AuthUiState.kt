package com.bandapa.feature.auth.domain

sealed class AuthUiState {
    data object Idle                    : AuthUiState()
    data object Loading                 : AuthUiState()
    data object Success                 : AuthUiState()
    data object EmailConfirmationPending : AuthUiState()
    data class  Error(val message: String) : AuthUiState()
}
