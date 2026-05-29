package com.bandapa.feature.announcements.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Announcement(
    val id: String,
    val title: String,
    val body: String,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("image_url") val imageUrl: String? = null,
)
