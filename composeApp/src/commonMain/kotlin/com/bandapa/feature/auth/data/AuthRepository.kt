package com.bandapa.feature.auth.data

import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val sessionStatus: StateFlow<SessionStatus>
    fun getCurrentUser(): UserInfo?
    suspend fun signInWithEmail(email: String, password: String)
    suspend fun signUpWithEmail(
        email: String,
        password: String,
        username: String,
        firstName: String,
        lastName: String,
        contactNumber: String,
    )
    suspend fun getEmailByUsername(username: String): String?
    suspend fun signOut()
}
