package com.bandapa.feature.calendar.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Event(
    val id: String = "",
    @SerialName("user_id")   val userId: String = "",
    @SerialName("band_id")   val bandId: String? = null,
    @SerialName("venue_id")  val venueId: String? = null,
    val title: String = "",
    val description: String? = null,
    @SerialName("start_time") val startTime: String = "",
    @SerialName("end_time")   val endTime: String = "",
    val location: String? = null,
    @SerialName("is_all_day") val isAllDay: Boolean = false,
    @SerialName("created_at") val createdAt: String = "",
)
