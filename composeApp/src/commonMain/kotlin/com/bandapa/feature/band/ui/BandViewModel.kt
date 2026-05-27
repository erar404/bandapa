package com.bandapa.feature.band.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bandapa.feature.band.data.BandRepository
import com.bandapa.feature.band.domain.Band
import com.bandapa.feature.band.domain.BandUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BandViewModel(private val repo: BandRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<BandUiState>(BandUiState.Idle)
    val uiState: StateFlow<BandUiState> = _uiState.asStateFlow()

    private var _foundBand: Band? = null

    fun createBand(
        name: String,
        description: String,
        genresRaw: String,
        dateFormed: String,
        label: String,
    ) {
        if (name.isBlank()) {
            _uiState.value = BandUiState.Error("Band name is required")
            return
        }
        val genres = genresRaw.split(",").map { it.trim() }.filter { it.isNotBlank() }
        viewModelScope.launch {
            _uiState.value = BandUiState.Loading
            try {
                val band = repo.createBand(
                    name        = name,
                    description = description.takeIf { it.isNotBlank() },
                    genres      = genres,
                    dateFormed  = dateFormed.takeIf { it.isNotBlank() },
                    label       = label.takeIf { it.isNotBlank() },
                )
                _uiState.value = BandUiState.BandCreated(band)
            } catch (e: Exception) {
                _uiState.value = BandUiState.Error(e.message ?: "Failed to create band")
            }
        }
    }

    fun lookUpBand(inviteCode: String) {
        if (inviteCode.length != 6) {
            _uiState.value = BandUiState.Error("Enter the full 6-character invite code")
            return
        }
        viewModelScope.launch {
            _uiState.value = BandUiState.Loading
            try {
                val band = repo.getBandByInviteCode(inviteCode)
                _foundBand = band
                _uiState.value = BandUiState.BandFound(band)
            } catch (e: Exception) {
                _uiState.value = BandUiState.Error(e.message ?: "Band not found — check the code and try again")
            }
        }
    }

    fun confirmJoin() {
        val band = _foundBand ?: return
        viewModelScope.launch {
            _uiState.value = BandUiState.Loading
            try {
                repo.joinBand(band.id)
                _uiState.value = BandUiState.JoinedBand(band)
            } catch (e: Exception) {
                _uiState.value = BandUiState.Error(e.message ?: "Failed to join band")
            }
        }
    }

    fun clearError() {
        if (_uiState.value is BandUiState.Error) _uiState.value = BandUiState.Idle
    }

    fun resetState() { _uiState.value = BandUiState.Idle; _foundBand = null }
}
