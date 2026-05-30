package com.bandapa.feature.band.data

import com.bandapa.feature.band.domain.Band
import com.bandapa.feature.band.domain.BandMember
import com.bandapa.feature.band.domain.BandMemberWithProfile
import com.bandapa.feature.band.domain.Profile
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.storage.storage
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class BandRepositoryImpl(private val supabase: SupabaseClient) : BandRepository {

    override suspend fun createBand(
        name: String,
        description: String?,
        genres: List<String>,
        dateFormed: String?,
        label: String?,
        spotifyUrl: String?,
        imageBytes: ByteArray?,
    ): Band {
        val userId = supabase.auth.currentUserOrNull()?.id ?: error("Not authenticated")
        var band = supabase.from("bands").insert(
            buildJsonObject {
                put("name", name.trim())
                put("created_by", userId)
                description?.takeIf { it.isNotBlank() }?.let { put("description", it.trim()) }
                if (genres.isNotEmpty()) put("genres", buildJsonArray { genres.forEach { add(it.trim()) } })
                dateFormed?.takeIf { it.isNotBlank() }?.let { put("date_formed", it) }
                label?.takeIf { it.isNotBlank() }?.let { put("label", it.trim()) }
                spotifyUrl?.takeIf { it.isNotBlank() }?.let { put("spotify_artist_id", it.trim()) }
            }
        ) { select() }.decodeSingle<Band>()

        if (imageBytes != null) {
            val path = "${band.id}/cover.jpg"
            supabase.storage.from("band-images").upload(path, imageBytes) { upsert = true }
            val imageUrl = supabase.storage.from("band-images").publicUrl(path)
            band = supabase.from("bands").update(
                buildJsonObject { put("image_url", imageUrl) }
            ) {
                filter { eq("id", band.id) }
                select()
            }.decodeSingle<Band>()
        }

        return band
    }

    override suspend fun getBandByInviteCode(inviteCode: String): Band =
        supabase.postgrest.rpc(
            "get_band_by_invite_code",
            buildJsonObject { put("p_code", inviteCode.uppercase().trim()) }
        ).decodeSingle<Band>()

    override suspend fun getBandById(id: String): Band =
        supabase.from("bands").select {
            filter { eq("id", id) }
        }.decodeSingle<Band>()

    override suspend fun joinBand(bandId: String): BandMember {
        val userId = supabase.auth.currentUserOrNull()?.id ?: error("Not authenticated")
        return supabase.from("band_members").insert(
            buildJsonObject {
                put("band_id", bandId)
                put("user_id", userId)
                put("is_admin", false)
            }
        ) { select() }.decodeSingle<BandMember>()
    }

    override suspend fun getMyBands(): List<Band> {
        val userId = supabase.auth.currentUserOrNull()?.id ?: error("Not authenticated")
        val refs = supabase.from("band_members")
            .select(Columns.raw("band_id")) {
                filter { eq("user_id", userId) }
            }
            .decodeList<BandIdRef>()
        val ids = refs.map { it.bandId }
        if (ids.isEmpty()) return emptyList()
        return supabase.from("bands").select {
            filter { isIn("id", ids) }
        }.decodeList<Band>()
    }

    override suspend fun updateBand(
        id: String,
        name: String,
        description: String?,
        genres: List<String>,
        dateFormed: String?,
        label: String?,
        spotifyUrl: String?,
    ): Band = supabase.from("bands").update(
        buildJsonObject {
            put("name",              name.trim())
            put("description",       if (description.isNullOrBlank()) JsonNull else JsonPrimitive(description.trim()))
            put("genres",            buildJsonArray { genres.forEach { add(it.trim()) } })
            put("date_formed",       if (dateFormed.isNullOrBlank()) JsonNull else JsonPrimitive(dateFormed))
            put("label",             if (label.isNullOrBlank()) JsonNull else JsonPrimitive(label.trim()))
            put("spotify_artist_id", if (spotifyUrl.isNullOrBlank()) JsonNull else JsonPrimitive(spotifyUrl.trim()))
        }
    ) {
        filter { eq("id", id) }
        select()
    }.decodeSingle<Band>()

    override suspend fun getMembersWithProfiles(bandId: String): List<BandMemberWithProfile> {
        val members = supabase.from("band_members").select {
            filter { eq("band_id", bandId) }
        }.decodeList<BandMember>()
        if (members.isEmpty()) return emptyList()
        val userIds = members.map { it.userId }
        val profiles = supabase.from("users").select {
            filter { isIn("id", userIds) }
        }.decodeList<Profile>()
        val profileMap = profiles.associateBy { it.id }
        return members.map { m ->
            BandMemberWithProfile(m, profileMap[m.userId] ?: Profile(id = m.userId))
        }
    }

    override suspend fun removeMember(memberId: String) {
        supabase.from("band_members").delete {
            filter { eq("id", memberId) }
        }
    }

    override suspend fun getCurrentUserId(): String? =
        supabase.auth.currentUserOrNull()?.id
}

@Serializable
private data class BandIdRef(@SerialName("band_id") val bandId: String)
