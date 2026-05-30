package com.bandapa.feature.band.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Profile(
    val id: String = "",
    val email: String? = null,
    @SerialName("full_name") val name: String? = null,
    val username: String? = null,
    @SerialName("display_picture") val displayPicture: String? = null,
    @SerialName("created_at") val createdAt: String = "",
)
