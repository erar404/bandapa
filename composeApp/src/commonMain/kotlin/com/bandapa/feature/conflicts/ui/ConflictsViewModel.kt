package com.bandapa.feature.conflicts.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bandapa.feature.band.data.BandRepository
import com.bandapa.feature.conflicts.data.ConflictsRepository
import com.bandapa.feature.conflicts.domain.ConflictsUiState
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class ConflictsViewModel(
    private val repo: ConflictsRepository,
    private val bandRepo: BandRepository,
    private val supabase: SupabaseClient,
) : ViewModel() {

    private val _uiState      = MutableStateFlow<ConflictsUiState>(ConflictsUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private var realtimeChannel: RealtimeChannel? = null

    init {
        loadConflicts()
        startRealtime()
    }

    fun loadConflicts(isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isRefresh) {
                _isRefreshing.value = true
            } else {
                _uiState.value = ConflictsUiState.Loading
            }
            try {
                val bands = bandRepo.getMyBands()
                if (bands.isEmpty()) {
                    _uiState.value = ConflictsUiState.NoBands
                } else {
                    _uiState.value = ConflictsUiState.Loaded(repo.getOpenConflicts())
                }
            } catch (e: Exception) {
                _uiState.value = ConflictsUiState.Error(e.message ?: "Failed to load conflicts")
            }
            _isRefreshing.value = false
        }
    }

    fun refresh() = loadConflicts(isRefresh = true)

    fun vote(conflictId: String, eventId: String) {
        viewModelScope.launch {
            try {
                repo.vote(conflictId, eventId)
                loadConflicts()
            } catch (e: Exception) {
                _uiState.value = ConflictsUiState.Error(e.message ?: "Vote failed")
            }
        }
    }

    fun dismiss(conflictId: String) {
        viewModelScope.launch {
            try {
                repo.dismiss(conflictId)
                loadConflicts()
            } catch (e: Exception) {
                _uiState.value = ConflictsUiState.Error(e.message ?: "Dismiss failed")
            }
        }
    }

    private fun startRealtime() {
        val ch = supabase.channel("conflicts-watch")
        realtimeChannel = ch

        ch.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "conflicts"
        }.onEach {
            loadConflicts()
        }.launchIn(viewModelScope)

        viewModelScope.launch {
            try { ch.subscribe() } catch (_: Exception) {}
        }
    }

    override fun onCleared() {
        super.onCleared()
        realtimeChannel?.let { ch ->
            viewModelScope.launch {
                try { supabase.realtime.removeChannel(ch) } catch (_: Exception) {}
            }
        }
    }
}
