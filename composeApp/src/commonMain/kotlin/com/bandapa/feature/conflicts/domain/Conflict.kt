package com.bandapa.feature.conflicts.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Conflict(
    val id: String = "",
    @SerialName("band_event_id")     val eventAId: String = "",
    @SerialName("personal_event_id") val eventBId: String = "",
    val status: String = "pending",
    @SerialName("created_at") val createdAt: String = "",
)
