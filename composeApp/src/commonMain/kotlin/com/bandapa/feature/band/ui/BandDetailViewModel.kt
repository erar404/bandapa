package com.bandapa.feature.band.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bandapa.feature.band.data.BandRepository
import com.bandapa.feature.band.domain.BandDetailUiState
import com.bandapa.feature.calendar.data.CalendarRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BandDetailViewModel(
    private val bandId: String,
    private val bandRepo: BandRepository,
    private val calendarRepo: CalendarRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<BandDetailUiState>(BandDetailUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = BandDetailUiState.Loading
            try {
                val userId = bandRepo.getCurrentUserId() ?: ""
                val band    = bandRepo.getBandById(bandId)
                val members = bandRepo.getMembersWithProfiles(bandId)
                val events  = calendarRepo.getEventsForBand(bandId)
                _uiState.value = BandDetailUiState.Loaded(
                    band           = band,
                    members        = members,
                    upcomingEvents = events,
                    isOwner        = band.ownerId == userId,
                )
            } catch (e: Exception) {
                _uiState.value = BandDetailUiState.Error(e.message ?: "Failed to load band")
            }
        }
    }

    fun updateBand(
        name: String,
        description: String,
        genresRaw: String,
        dateFormed: String,
        label: String,
        spotifyUrl: String,
    ) {
        if (name.isBlank()) {
            _uiState.value = BandDetailUiState.Error("Band name is required")
            return
        }
        val genres = genresRaw.split(",").map { it.trim() }.filter { it.isNotBlank() }
        viewModelScope.launch {
            try {
                val updated = bandRepo.updateBand(
                    id          = bandId,
                    name        = name,
                    description = description.takeIf { it.isNotBlank() },
                    genres      = genres,
                    dateFormed  = dateFormed.takeIf { it.isNotBlank() },
                    label       = label.takeIf { it.isNotBlank() },
                    spotifyUrl  = spotifyUrl.takeIf { it.isNotBlank() },
                )
                val state = _uiState.value
                if (state is BandDetailUiState.Loaded) {
                    _uiState.value = state.copy(band = updated)
                }
            } catch (e: Exception) {
                _uiState.value = BandDetailUiState.Error(e.message ?: "Update failed")
            }
        }
    }

    fun removeMember(memberId: String) {
        viewModelScope.launch {
            try {
                bandRepo.removeMember(memberId)
                load()
            } catch (e: Exception) {
                _uiState.value = BandDetailUiState.Error(e.message ?: "Failed to remove member")
            }
        }
    }

    fun clearError() {
        if (_uiState.value is BandDetailUiState.Error) load()
    }
}
