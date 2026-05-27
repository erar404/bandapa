package com.bandapa.feature.band.data

import com.bandapa.feature.band.domain.Band
import com.bandapa.feature.band.domain.BandMember
import com.bandapa.feature.band.domain.BandMemberWithProfile

interface BandRepository {
    suspend fun createBand(
        name: String,
        description: String?,
        genres: List<String>,
        dateFormed: String?,
        label: String?,
    ): Band

    suspend fun getBandByInviteCode(inviteCode: String): Band
    suspend fun getBandById(id: String): Band

    suspend fun joinBand(bandId: String): BandMember

    suspend fun getMyBands(): List<Band>

    suspend fun updateBand(
        id: String,
        name: String,
        description: String?,
        genres: List<String>,
        dateFormed: String?,
        label: String?,
        spotifyUrl: String?,
    ): Band

    suspend fun getMembersWithProfiles(bandId: String): List<BandMemberWithProfile>
    suspend fun removeMember(memberId: String)
    suspend fun getCurrentUserId(): String?
}
