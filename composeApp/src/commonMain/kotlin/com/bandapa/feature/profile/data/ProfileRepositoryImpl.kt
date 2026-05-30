package com.bandapa.feature.profile.data

import com.bandapa.feature.band.domain.Profile
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class ProfileRepositoryImpl(private val supabase: SupabaseClient) : ProfileRepository {

    override suspend fun getProfile(): Profile {
        val userId = supabase.auth.currentUserOrNull()?.id ?: error("Not authenticated")
        return supabase.from("users").select {
            filter { eq("id", userId) }
        }.decodeSingle<Profile>()
    }

    override suspend fun updateProfile(name: String): Profile {
        val userId = supabase.auth.currentUserOrNull()?.id ?: error("Not authenticated")
        return supabase.from("users").update(
            buildJsonObject { put("full_name", name.trim()) }
        ) {
            filter { eq("id", userId) }
            select()
        }.decodeSingle<Profile>()
    }

    override suspend fun uploadDisplayPicture(bytes: ByteArray): Profile {
        val userId = supabase.auth.currentUserOrNull()?.id ?: error("Not authenticated")
        val path = "$userId/avatar.jpg"
        supabase.storage.from("avatars").upload(path, bytes) { upsert = true }
        val url = supabase.storage.from("avatars").publicUrl(path)
        return supabase.from("users").update(
            buildJsonObject { put("display_picture", url) }
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
