package com.bandapa.feature.calendar.data

import com.bandapa.feature.calendar.domain.Event
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class CalendarRepositoryImpl(private val supabase: SupabaseClient) : CalendarRepository {

    override suspend fun getEventsForMonth(year: Int, month: Int): List<Event> {
        val tz     = TimeZone.UTC
        val start  = LocalDate(year, month, 1).atStartOfDayIn(tz)
        val end    = start.plus(DateTimePeriod(months = 1), tz)

        return supabase.from("events").select {
            filter {
                gte("start_time", start.toString())
                lt("start_time",  end.toString())
            }
        }.decodeList<Event>()
    }

    override suspend fun getEventsForBand(bandId: String, limit: Int): List<Event> {
        val now = Clock.System.now().toString()
        return supabase.from("events").select {
            filter {
                eq("band_id", bandId)
                gte("start_time", now)
            }
        }.decodeList<Event>()
            .sortedBy { it.startTime }
            .take(limit)
    }

    override suspend fun getTodayEvents(): List<Event> {
        val tz    = TimeZone.currentSystemDefault()
        val today = Clock.System.todayIn(tz)
        return getEventsForMonth(today.year, today.monthNumber)
            .filter { it.startTime.startsWith(today.toString()) }
            .sortedBy { it.startTime }
    }

    override suspend fun createEvent(event: Event): Event {
        val userId = supabase.auth.currentUserOrNull()?.id
            ?: error("Not authenticated")

        return supabase.from("events").insert(
            buildJsonObject {
                put("owner_id",   userId)
                put("title",      event.title)
                put("event_type", event.eventType)
                put("start_time", event.startTime)
                put("end_time",   event.endTime)
                put("is_all_day", event.isAllDay)
                put("band_id",    if (event.bandId.isNullOrBlank()) JsonNull else JsonPrimitive(event.bandId))
                put("venue_id",   if (event.venueId.isNullOrBlank()) JsonNull else JsonPrimitive(event.venueId))
                event.description?.takeIf { it.isNotBlank() }?.let { put("description", it) }
                event.location?.takeIf { it.isNotBlank() }?.let { put("location", it) }
            }
        ) { select() }.decodeSingle<Event>()
    }

    override suspend fun deleteEvent(id: String) {
        supabase.from("events").delete {
            filter { eq("id", id) }
        }
    }
}
