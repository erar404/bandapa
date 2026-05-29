package com.bandapa.feature.venues.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bandapa.feature.venues.data.GeocodedLocation
import com.bandapa.feature.venues.data.GooglePlacesClient
import com.bandapa.feature.venues.data.PlacePrediction
import com.bandapa.feature.venues.data.VenueRepository
import com.bandapa.feature.venues.domain.Venue
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class VenueUiState(
    val venues: List<Venue> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    // Add-venue form
    val addressQuery: String = "",
    val suggestions: List<PlacePrediction> = emptyList(),
    val geocoded: GeocodedLocation? = null,
    val isGeocoding: Boolean = false,
)

class VenueViewModel(
    private val repo: VenueRepository,
    private val placesClient: GooglePlacesClient,
) : ViewModel() {

    private val _uiState = MutableStateFlow(VenueUiState())
    val uiState = _uiState.asStateFlow()

    private var autocompleteJob: Job? = null

    init { load(showFullLoader = true) }

    private fun load(showFullLoader: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading    = showFullLoader,
                isRefreshing = !showFullLoader,
                error        = null,
            )
            try {
                _uiState.value = _uiState.value.copy(
                    venues       = repo.getVenues(),
                    isLoading    = false,
                    isRefreshing = false,
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading    = false,
                    isRefreshing = false,
                    error        = e.message ?: "Failed to load venues",
                )
            }
        }
    }

    fun refresh() = load(showFullLoader = false)

    // ── Address autocomplete ──────────────────────────────────────────────────

    fun onAddressQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(
            addressQuery = query,
            geocoded     = null,
        )
        autocompleteJob?.cancel()
        if (query.length >= 3) {
            autocompleteJob = viewModelScope.launch {
                delay(350)
                val results = placesClient.autocomplete(query)
                _uiState.value = _uiState.value.copy(suggestions = results)
            }
        } else {
            _uiState.value = _uiState.value.copy(suggestions = emptyList())
        }
    }

    fun onPlaceSelected(prediction: PlacePrediction) {
        _uiState.value = _uiState.value.copy(
            addressQuery = prediction.description,
            suggestions  = emptyList(),
            isGeocoding  = true,
        )
        viewModelScope.launch {
            val location = placesClient.geocode(prediction.description)
            _uiState.value = _uiState.value.copy(
                geocoded    = location,
                isGeocoding = false,
            )
        }
    }

    fun dismissSuggestions() {
        _uiState.value = _uiState.value.copy(suggestions = emptyList())
    }

    fun resetAddForm() {
        autocompleteJob?.cancel()
        _uiState.value = _uiState.value.copy(
            addressQuery = "",
            suggestions  = emptyList(),
            geocoded     = null,
            isGeocoding  = false,
        )
    }

    // ── CRUD ──────────────────────────────────────────────────────────────────

    fun addVenue(name: String, city: String) {
        if (name.isBlank()) return
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null)
            try {
                val created = repo.createVenue(
                    name      = name,
                    address   = state.addressQuery.takeIf { it.isNotBlank() },
                    city      = city.takeIf { it.isNotBlank() },
                    latitude  = state.geocoded?.lat,
                    longitude = state.geocoded?.lng,
                )
                _uiState.value = _uiState.value.copy(
                    venues   = _uiState.value.venues + created,
                    isSaving = false,
                )
                resetAddForm()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error    = e.message ?: "Failed to add venue",
                )
            }
        }
    }

    fun deleteVenue(id: String) {
        viewModelScope.launch {
            try {
                repo.deleteVenue(id)
                _uiState.value = _uiState.value.copy(
                    venues = _uiState.value.venues.filterNot { it.id == id }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message ?: "Failed to delete venue")
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
