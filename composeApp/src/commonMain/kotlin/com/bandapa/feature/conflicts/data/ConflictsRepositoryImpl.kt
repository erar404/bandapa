package com.bandapa.feature.conflicts.data

import com.bandapa.feature.calendar.domain.Event
import com.bandapa.feature.conflicts.domain.Conflict
import com.bandapa.feature.conflicts.domain.ConflictDetail
import com.bandapa.feature.conflicts.domain.ConflictVote
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class ConflictsRepositoryImpl(private val supabase: SupabaseClient) : ConflictsRepository {

    override suspend fun getOpenConflicts(): List<ConflictDetail> {
        val userId = supabase.auth.currentUserOrNull()?.id
            ?: error("Not authenticated")

        val conflicts = supabase.from("conflicts").select {
            filter { eq("status", "open") }
        }.decodeList<Conflict>()
            .sortedByDescending { it.createdAt }

        if (conflicts.isEmpty()) return emptyList()

        val eventIds = conflicts.flatMap { listOf(it.eventAId, it.eventBId) }.distinct()
        val events   = supabase.from("events").select {
            filter { isIn("id", eventIds) }
        }.decodeList<Event>()
        val eventMap = events.associateBy { it.id }

        val conflictIds = conflicts.map { it.id }
        val votes = supabase.from("conflict_votes").select {
            filter { isIn("conflict_id", conflictIds) }
        }.decodeList<ConflictVote>()
        val votesByConflict = votes.groupBy { it.conflictId }

        return conflicts.mapNotNull { c ->
            val a = eventMap[c.eventAId] ?: return@mapNotNull null
            val b = eventMap[c.eventBId] ?: return@mapNotNull null
            val cvotes = votesByConflict[c.id] ?: emptyList()
            ConflictDetail(
                conflict = c,
                eventA   = a,
                eventB   = b,
                votes    = cvotes,
                myVote   = cvotes.firstOrNull { it.userId == userId }?.votedFor,
            )
        }
    }

    override suspend fun vote(conflictId: String, eventId: String) {
        val userId = supabase.auth.currentUserOrNull()?.id
            ?: error("Not authenticated")

        supabase.from("conflict_votes").delete {
            filter {
                eq("conflict_id", conflictId)
                eq("user_id", userId)
            }
        }
        supabase.from("conflict_votes").insert(
            buildJsonObject {
                put("conflict_id", conflictId)
                put("user_id", userId)
                put("voted_for", eventId)
            }
        )
    }

    override suspend fun dismiss(conflictId: String) {
        supabase.from("conflicts").update(
            buildJsonObject { put("status", "resolved") }
        ) {
            filter { eq("id", conflictId) }
        }
    }
}
