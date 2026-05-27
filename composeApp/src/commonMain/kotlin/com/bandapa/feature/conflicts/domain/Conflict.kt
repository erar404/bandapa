package com.bandapa.feature.conflicts.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Conflict(
    val id: String = "",
    @SerialName("event_a_id") val eventAId: String = "",
    @SerialName("event_b_id") val eventBId: String = "",
    @SerialName("band_id")    val bandId: String? = null,
    val status: String = "open",
    @SerialName("created_at") val createdAt: String = "",
)
