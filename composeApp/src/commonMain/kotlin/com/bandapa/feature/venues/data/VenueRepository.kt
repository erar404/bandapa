package com.bandapa.feature.venues.data

import com.bandapa.feature.venues.domain.Venue

interface VenueRepository {
    suspend fun getVenues(): List<Venue>
    suspend fun createVenue(name: String, address: String?, city: String?): Venue
    suspend fun deleteVenue(id: String)
}
