package com.bandapa.feature.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bandapa.feature.band.data.BandRepository
import com.bandapa.feature.band.domain.Band
import com.bandapa.feature.band.domain.Profile
import com.bandapa.feature.calendar.data.CalendarRepository
import com.bandapa.feature.calendar.domain.Event
import com.bandapa.feature.profile.data.ProfileRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val profile: Profile = Profile(),
    val todayEvents: List<Event> = emptyList(),
    val myBands: List<Band> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
)

class HomeViewModel(
    private val profileRepo: ProfileRepository,
    private val bandRepo: BandRepository,
    private val calendarRepo: CalendarRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val profileDeferred = async { profileRepo.getProfile() }
                val bandsDeferred   = async { bandRepo.getMyBands() }
                val eventsDeferred  = async { calendarRepo.getTodayEvents() }

                val profile = profileDeferred.await()
                val email   = profile.email ?: profileRepo.getCurrentUserEmail()
                _uiState.value = HomeUiState(
                    profile     = profile.copy(email = email),
                    myBands     = bandsDeferred.await(),
                    todayEvents = eventsDeferred.await(),
                    isLoading   = false,
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error     = e.message ?: "Failed to load",
                )
            }
        }
    }
}
