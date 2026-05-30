package com.bandapa.feature.profile.data

import com.bandapa.feature.band.domain.Profile

interface ProfileRepository {
    suspend fun getProfile(): Profile
    suspend fun updateProfile(name: String): Profile
    suspend fun uploadDisplayPicture(bytes: ByteArray): Profile
    fun getCurrentUserEmail(): String?
    fun getCurrentUserId(): String?
}
