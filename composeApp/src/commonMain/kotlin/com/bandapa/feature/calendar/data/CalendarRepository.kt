package com.bandapa.feature.calendar.data

import com.bandapa.feature.calendar.domain.Event

interface CalendarRepository {
    suspend fun getEventsForMonth(year: Int, month: Int): List<Event>
    suspend fun getEventsForBand(bandId: String, limit: Int = 5): List<Event>
    suspend fun createEvent(event: Event): Event
    suspend fun deleteEvent(id: String)
}
