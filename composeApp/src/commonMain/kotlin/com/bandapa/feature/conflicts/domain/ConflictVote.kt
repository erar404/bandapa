package com.bandapa.feature.conflicts.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ConflictVote(
    val id: String = "",
    @SerialName("conflict_id") val conflictId: String = "",
    @SerialName("user_id")     val userId: String = "",
    @SerialName("voted_for")   val votedFor: String = "",
    @SerialName("created_at")  val createdAt: String = "",
)
