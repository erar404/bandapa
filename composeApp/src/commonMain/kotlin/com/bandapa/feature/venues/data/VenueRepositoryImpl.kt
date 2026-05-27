package com.bandapa.feature.venues.data

import com.bandapa.feature.venues.domain.Venue
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class VenueRepositoryImpl(private val supabase: SupabaseClient) : VenueRepository {

    override suspend fun getVenues(): List<Venue> =
        supabase.from("venues").select().decodeList<Venue>()

    override suspend fun createVenue(name: String, address: String?, city: String?): Venue {
        val userId = supabase.auth.currentUserOrNull()?.id ?: error("Not authenticated")
        return supabase.from("venues").insert(
            buildJsonObject {
                put("name", name.trim())
                put("created_by", userId)
                address?.takeIf { it.isNotBlank() }?.let { put("address", it.trim()) }
                city?.takeIf { it.isNotBlank() }?.let { put("city", it.trim()) }
            }
        ) { select() }.decodeSingle<Venue>()
    }

    override suspend fun deleteVenue(id: String) {
        supabase.from("venues").delete {
            filter { eq("id", id) }
        }
    }
}
