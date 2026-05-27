package com.bandapa.feature.band.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Profile(
    val id: String = "",
    val email: String? = null,
    val name: String? = null,
    @SerialName("created_at") val createdAt: String = "",
)
