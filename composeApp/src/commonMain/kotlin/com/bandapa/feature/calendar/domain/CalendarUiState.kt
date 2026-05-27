package com.bandapa.feature.calendar.domain

sealed class CalendarUiState {
    data object Idle         : CalendarUiState()
    data object Loading      : CalendarUiState()
    data class  EventCreated(val event: Event)  : CalendarUiState()
    data class  Error(val message: String)      : CalendarUiState()
}
