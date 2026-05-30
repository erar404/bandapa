package com.bandapa.feature.band.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class BandMember(
    val id: String = "",
    @SerialName("band_id")  val bandId: String = "",
    @SerialName("user_id")  val userId: String = "",
    @SerialName("is_admin") val isAdmin: Boolean = false,
    @SerialName("joined_at") val joinedAt: String = "",
) {
    @Transient val role: String = if (isAdmin) "admin" else "member"
}
