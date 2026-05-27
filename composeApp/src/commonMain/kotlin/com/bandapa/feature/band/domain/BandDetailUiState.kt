package com.bandapa.feature.band.domain

import com.bandapa.feature.calendar.domain.Event

sealed class BandDetailUiState {
    data object Loading : BandDetailUiState()
    data class Loaded(
        val band: Band,
        val members: List<BandMemberWithProfile>,
        val upcomingEvents: List<Event>,
        val isOwner: Boolean,
    ) : BandDetailUiState()
    data object Updated         : BandDetailUiState()
    data class  Error(val message: String) : BandDetailUiState()
}
