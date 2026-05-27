package com.bandapa.feature.band.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bandapa.feature.band.data.BandRepository
import com.bandapa.feature.band.domain.Band
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BandsListViewModel(private val repo: BandRepository) : ViewModel() {

    private val _bands        = MutableStateFlow<List<Band>>(emptyList())
    val bands = _bands.asStateFlow()

    private val _isLoading    = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    init { load() }

    fun load(isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isRefresh) _isRefreshing.value = true
            else           _isLoading.value    = true
            try { _bands.value = repo.getMyBands() } catch (_: Exception) {}
            _isLoading.value    = false
            _isRefreshing.value = false
        }
    }

    fun refresh() = load(isRefresh = true)
}
