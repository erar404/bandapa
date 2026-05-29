package com.bandapa.feature.auth.data

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class AuthRepositoryImpl(private val supabase: SupabaseClient) : AuthRepository {

    override val sessionStatus: StateFlow<SessionStatus>
        get() = supabase.auth.sessionStatus

    override fun getCurrentUser(): UserInfo? = supabase.auth.currentUserOrNull()

    override suspend fun signInWithEmail(email: String, password: String) {
        supabase.auth.signInWith(Email) {
            this.email    = email
            this.password = password
        }
    }

    override suspend fun signUpWithEmail(
        email: String,
        password: String,
        username: String,
        firstName: String,
        lastName: String,
        contactNumber: String,
    ) {
        supabase.auth.signUpWith(Email) {
            this.email    = email
            this.password = password
            data = buildJsonObject {
                put("username",   username.trim())
                put("first_name", firstName.trim())
                put("last_name",  lastName.trim())
                put("full_name",  "${firstName.trim()} ${lastName.trim()}".trim())
                if (contactNumber.isNotBlank()) put("contact_number", contactNumber.trim())
            }
        }
    }

    override suspend fun getEmailByUsername(username: String): String? =
        try {
            supabase.postgrest.rpc(
                "get_email_by_username",
                buildJsonObject { put("uname", username.lowercase().trim()) }
            ).decodeAs<String>().takeIf { it.isNotBlank() }
        } catch (_: Exception) {
            null
        }

    override suspend fun signOut() {
        supabase.auth.signOut()
    }
}
