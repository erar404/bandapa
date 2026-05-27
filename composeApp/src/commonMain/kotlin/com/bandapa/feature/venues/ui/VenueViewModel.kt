package com.bandapa.feature.venues.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bandapa.feature.venues.data.VenueRepository
import com.bandapa.feature.venues.domain.Venue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class VenueUiState(
    val venues: List<Venue> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
)

class VenueViewModel(private val repo: VenueRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(VenueUiState())
    val uiState = _uiState.asStateFlow()

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

    fun addVenue(name: String, address: String, city: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null)
            try {
                val created = repo.createVenue(
                    name    = name,
                    address = address.takeIf { it.isNotBlank() },
                    city    = city.takeIf { it.isNotBlank() },
                )
                _uiState.value = _uiState.value.copy(
                    venues  = _uiState.value.venues + created,
                    isSaving = false,
                )
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
