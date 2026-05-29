package com.bandapa.feature.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bandapa.feature.auth.data.AuthRepository
import com.bandapa.feature.auth.domain.AuthUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val repo: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun loginWithEmail(identifier: String, password: String) {
        if (identifier.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState.Error("Username/email and password are required")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val email = if (identifier.contains("@")) {
                    identifier.trim()
                } else {
                    repo.getEmailByUsername(identifier.trim())
                        ?: throw Exception("No account found for that username")
                }
                repo.signInWithEmail(email, password)
                _uiState.value = AuthUiState.Success
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Login failed. Please try again.")
            }
        }
    }

    fun signUp(
        email: String,
        password: String,
        confirmPassword: String,
        username: String,
        firstName: String,
        lastName: String,
        contactNumber: String,
    ) {
        val error = when {
            email.isBlank() || password.isBlank() -> "Email and password are required"
            username.isBlank()                    -> "Username is required"
            password != confirmPassword           -> "Passwords do not match"
            password.length < 8                   -> "Password must be at least 8 characters"
            else                                  -> null
        }
        if (error != null) {
            _uiState.value = AuthUiState.Error(error)
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                repo.signUpWithEmail(email, password, username, firstName, lastName, contactNumber)
                val confirmedAt = repo.getCurrentUser()?.emailConfirmedAt
                _uiState.value = if (confirmedAt != null) AuthUiState.Success
                                 else AuthUiState.EmailConfirmationPending
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Sign up failed. Please try again.")
            }
        }
    }

    fun clearError() {
        if (_uiState.value is AuthUiState.Error) _uiState.value = AuthUiState.Idle
    }

    fun resetState() { _uiState.value = AuthUiState.Idle }
}
