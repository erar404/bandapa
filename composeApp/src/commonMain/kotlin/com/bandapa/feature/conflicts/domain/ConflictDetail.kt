package com.bandapa.feature.conflicts.domain

import com.bandapa.feature.calendar.domain.Event

data class ConflictDetail(
    val conflict: Conflict,
    val eventA: Event,
    val eventB: Event,
    val votes: List<ConflictVote>,
    val myVote: String?,
)
