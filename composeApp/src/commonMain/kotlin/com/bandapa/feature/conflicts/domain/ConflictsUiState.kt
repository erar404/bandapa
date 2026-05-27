package com.bandapa.feature.conflicts.domain

sealed class ConflictsUiState {
    data object Loading                              : ConflictsUiState()
    data class  Loaded(val items: List<ConflictDetail>) : ConflictsUiState()
    data class  Error(val message: String)           : ConflictsUiState()
}
