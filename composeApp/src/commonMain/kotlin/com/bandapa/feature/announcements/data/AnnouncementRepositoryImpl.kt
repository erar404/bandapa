package com.bandapa.feature.announcements.data

import com.bandapa.feature.announcements.domain.Announcement
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement

private val json = Json { ignoreUnknownKeys = true }

class AnnouncementRepositoryImpl(private val supabase: SupabaseClient) : AnnouncementRepository {

    override suspend fun getActiveAnnouncements(): List<Announcement> =
        supabase.from("announcements").select {
            filter { eq("is_active", true) }
            order("created_at", Order.DESCENDING)
        }.decodeList<Announcement>()

    override fun newAnnouncementsFlow(): Flow<Announcement> = channelFlow {
        val realtimeChannel = supabase.channel("bandapa-announcements-new")
        val changes = realtimeChannel.postgresChangeFlow<PostgresAction.Insert>(schema = "bandapa-main") {
            table = "announcements"
        }
        realtimeChannel.subscribe()
        try {
            changes.collect { action ->
                runCatching { json.decodeFromJsonElement<Announcement>(action.record) }
                    .getOrNull()
                    ?.let { trySend(it) }
            }
        } finally {
            supabase.realtime.removeChannel(realtimeChannel)
        }
    }
}
