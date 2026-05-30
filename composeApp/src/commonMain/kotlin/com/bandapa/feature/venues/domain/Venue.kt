package com.bandapa.feature.venues.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Venue(
    val id: String = "",
    val name: String = "",
    val address: String? = null,
    val city: String? = null,
    @SerialName("venue_type") val venueType: String = "others",
    val latitude: Double? = null,
    val longitude: Double? = null,
    @SerialName("created_by") val createdBy: String? = null,
    @SerialName("created_at") val createdAt: String = "",
)
