package com.bandapa.feature.band.domain

sealed class BandUiState {
    data object Idle    : BandUiState()
    data object Loading : BandUiState()
    data class BandCreated(val band: Band) : BandUiState()
    data class BandFound(val band: Band)   : BandUiState()
    data class JoinedBand(val band: Band)  : BandUiState()
    data class Error(val message: String)  : BandUiState()
}
