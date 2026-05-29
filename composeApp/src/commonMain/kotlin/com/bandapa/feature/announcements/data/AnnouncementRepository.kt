package com.bandapa.feature.announcements.data

import com.bandapa.feature.announcements.domain.Announcement
import kotlinx.coroutines.flow.Flow

interface AnnouncementRepository {
    suspend fun getActiveAnnouncements(): List<Announcement>
    fun newAnnouncementsFlow(): Flow<Announcement>
}
