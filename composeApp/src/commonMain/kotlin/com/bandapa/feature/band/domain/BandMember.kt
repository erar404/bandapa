package com.bandapa.feature.band.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BandMember(
    val id: String = "",
    @SerialName("band_id") val bandId: String = "",
    @SerialName("user_id") val userId: String = "",
    val role: String = "member",
    @SerialName("joined_at") val joinedAt: String = "",
)
