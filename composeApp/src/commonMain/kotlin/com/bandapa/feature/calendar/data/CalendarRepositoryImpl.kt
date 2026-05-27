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

    override suspend fun createEvent(event: Event): Event {
        val userId = supabase.auth.currentUserOrNull()?.id
            ?: error("Not authenticated")

        return supabase.from("events").insert(
            event.copy(userId = userId)
        ) { select() }.decodeSingle<Event>()
    }

    override suspend fun deleteEvent(id: String) {
        supabase.from("events").delete {
            filter { eq("id", id) }
        }
    }
}
