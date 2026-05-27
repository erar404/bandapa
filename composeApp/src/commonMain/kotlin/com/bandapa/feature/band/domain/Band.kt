package com.bandapa.feature.band.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Band(
    val id: String = "",
    val name: String = "",
    val description: String? = null,
    val genres: List<String> = emptyList(),
    @SerialName("date_formed") val dateFormed: String? = null,
    val label: String? = null,
    @SerialName("invite_code")  val inviteCode: String = "",
    @SerialName("owner_id")     val ownerId: String = "",
    @SerialName("spotify_url")  val spotifyUrl: String? = null,
    @SerialName("created_at")   val createdAt: String = "",
)
