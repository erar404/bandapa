package com.bandapa.feature.profile.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bandapa.feature.auth.data.AuthRepository
import com.bandapa.feature.band.data.BandRepository
import com.bandapa.feature.band.domain.Band
import com.bandapa.feature.band.domain.Profile
import com.bandapa.feature.profile.data.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val profile: Profile = Profile(),
    val bands: List<Band> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isUploadingPhoto: Boolean = false,
    val error: String? = null,
    val savedSuccess: Boolean = false,
)

class ProfileViewModel(
    private val profileRepo: ProfileRepository,
    private val bandRepo: BandRepository,
    private val authRepo: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    init { load() }

    private fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val profile = profileRepo.getProfile()
                val bands   = bandRepo.getMyBands()
                val email   = profile.email ?: profileRepo.getCurrentUserEmail()
                _uiState.value = _uiState.value.copy(
                    profile   = profile.copy(email = email),
                    bands     = bands,
                    isLoading = false,
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error     = e.message ?: "Failed to load profile",
                )
            }
        }
    }

    fun saveName(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null, savedSuccess = false)
            try {
                val updated = profileRepo.updateProfile(name)
                _uiState.value = _uiState.value.copy(
                    profile      = updated.copy(email = _uiState.value.profile.email),
                    isSaving     = false,
                    savedSuccess = true,
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error    = e.message ?: "Failed to save name",
                )
            }
        }
    }

    fun uploadPhoto(bytes: ByteArray) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUploadingPhoto = true, error = null)
            try {
                val updated = profileRepo.uploadDisplayPicture(bytes)
                _uiState.value = _uiState.value.copy(
                    profile         = updated.copy(email = _uiState.value.profile.email),
                    isUploadingPhoto = false,
                    savedSuccess     = true,
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isUploadingPhoto = false,
                    error            = e.message ?: "Failed to upload photo",
                )
            }
        }
    }

    fun clearSavedSuccess() {
        _uiState.value = _uiState.value.copy(savedSuccess = false)
    }

    fun signOut() {
        viewModelScope.launch {
            try { authRepo.signOut() } catch (_: Exception) {}
        }
    }
}
