package com.bandapa.feature.profile.data

import com.bandapa.feature.band.domain.Profile
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class ProfileRepositoryImpl(private val supabase: SupabaseClient) : ProfileRepository {

    override suspend fun getProfile(): Profile {
        val userId = supabase.auth.currentUserOrNull()?.id ?: error("Not authenticated")
        return supabase.from("profiles").select {
            filter { eq("id", userId) }
        }.decodeSingle<Profile>()
    }

    override suspend fun updateProfile(name: String): Profile {
        val userId = supabase.auth.currentUserOrNull()?.id ?: error("Not authenticated")
        return supabase.from("profiles").update(
            buildJsonObject { put("name", name.trim()) }
        ) {
            filter { eq("id", userId) }
            select()
        }.decodeSingle<Profile>()
    }

    override fun getCurrentUserEmail(): String? =
        supabase.auth.currentUserOrNull()?.email

    override fun getCurrentUserId(): String? =
        supabase.auth.currentUserOrNull()?.id
}
