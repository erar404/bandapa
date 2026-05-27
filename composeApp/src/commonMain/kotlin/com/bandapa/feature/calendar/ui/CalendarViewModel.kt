package com.bandapa.feature.calendar.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bandapa.feature.band.data.BandRepository
import com.bandapa.feature.band.domain.Band
import com.bandapa.feature.calendar.data.CalendarRepository
import com.bandapa.feature.calendar.domain.CalendarUiState
import com.bandapa.feature.calendar.domain.Event
import com.bandapa.feature.venues.data.VenueRepository
import com.bandapa.feature.venues.domain.Venue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

class CalendarViewModel(
    private val calendarRepo: CalendarRepository,
    private val bandRepo: BandRepository,
    private val venueRepo: VenueRepository,
) : ViewModel() {

    private val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

    private val _displayedMonth = MutableStateFlow(LocalDate(today.year, today.monthNumber, 1))
    val displayedMonth = _displayedMonth.asStateFlow()

    private val _selectedDay = MutableStateFlow<LocalDate?>(today)
    val selectedDay = _selectedDay.asStateFlow()

    private val _eventsForMonth = MutableStateFlow<List<Event>>(emptyList())
    val eventsForMonth = _eventsForMonth.asStateFlow()

    private val _myBands = MutableStateFlow<List<Band>>(emptyList())
    val myBands = _myBands.asStateFlow()

    private val _venues = MutableStateFlow<List<Venue>>(emptyList())
    val venues = _venues.asStateFlow()

    private val _uiState = MutableStateFlow<CalendarUiState>(CalendarUiState.Idle)
    val uiState = _uiState.asStateFlow()

    init {
        loadCurrentMonth()
        loadMyBands()
        loadVenues()
    }

    fun selectDay(date: LocalDate) { _selectedDay.value = date }

    fun prevMonth() {
        val cur = _displayedMonth.value
        _displayedMonth.value = if (cur.monthNumber == 1)
            LocalDate(cur.year - 1, 12, 1)
        else
            LocalDate(cur.year, cur.monthNumber - 1, 1)
        loadCurrentMonth()
    }

    fun nextMonth() {
        val cur = _displayedMonth.value
        _displayedMonth.value = if (cur.monthNumber == 12)
            LocalDate(cur.year + 1, 1, 1)
        else
            LocalDate(cur.year, cur.monthNumber + 1, 1)
        loadCurrentMonth()
    }

    fun createEvent(
        title: String,
        date: LocalDate,
        startHhmm: String,
        endHhmm: String,
        bandId: String?,
        venueId: String?,
        location: String,
        isAllDay: Boolean,
    ) {
        if (title.isBlank()) {
            _uiState.value = CalendarUiState.Error("Title is required")
            return
        }
        viewModelScope.launch {
            _uiState.value = CalendarUiState.Loading
            try {
                val startIso = if (isAllDay) "${date}T00:00:00Z" else "${date}T${startHhmm}:00Z"
                val endIso   = if (isAllDay) "${date}T23:59:59Z" else "${date}T${endHhmm}:00Z"
                val event = Event(
                    title    = title.trim(),
                    startTime = startIso,
                    endTime   = endIso,
                    bandId   = bandId.takeIf { !it.isNullOrBlank() },
                    venueId  = venueId.takeIf { !it.isNullOrBlank() },
                    location = location.trim().takeIf { it.isNotEmpty() },
                    isAllDay = isAllDay,
                )
                val created = calendarRepo.createEvent(event)
                _uiState.value = CalendarUiState.EventCreated(created)
                loadCurrentMonth()
            } catch (e: Exception) {
                _uiState.value = CalendarUiState.Error(e.message ?: "Failed to create event")
            }
        }
    }

    fun deleteEvent(id: String) {
        viewModelScope.launch {
            try {
                calendarRepo.deleteEvent(id)
                loadCurrentMonth()
            } catch (e: Exception) {
                _uiState.value = CalendarUiState.Error(e.message ?: "Failed to delete event")
            }
        }
    }

    fun clearError() { _uiState.value = CalendarUiState.Idle }
    fun resetState() { _uiState.value = CalendarUiState.Idle }

    private fun loadCurrentMonth() {
        val month = _displayedMonth.value
        viewModelScope.launch {
            try {
                _eventsForMonth.value = calendarRepo.getEventsForMonth(month.year, month.monthNumber)
            } catch (_: Exception) {}
        }
    }

    private fun loadMyBands() {
        viewModelScope.launch {
            try { _myBands.value = bandRepo.getMyBands() } catch (_: Exception) {}
        }
    }

    private fun loadVenues() {
        viewModelScope.launch {
            try { _venues.value = venueRepo.getVenues() } catch (_: Exception) {}
        }
    }
}
