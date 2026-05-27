package com.bandapa.feature.conflicts.data

import com.bandapa.feature.conflicts.domain.ConflictDetail

interface ConflictsRepository {
    suspend fun getOpenConflicts(): List<ConflictDetail>
    suspend fun vote(conflictId: String, eventId: String)
    suspend fun dismiss(conflictId: String)
}
