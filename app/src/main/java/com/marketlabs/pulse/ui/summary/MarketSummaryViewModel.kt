package com.marketlabs.pulse.ui.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marketlabs.pulse.core.marketSummary.MarketSummaryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MarketSummaryViewModel @Inject constructor(
    private val repository: MarketSummaryRepository
) : ViewModel() {

    // stream: single source of truth (SSOT)
    // Observes DB, survives configuration changes (rotation)
    val summaryUiState: StateFlow<MarketSummaryUiState> = repository.getMarketSummaryStream()
        .map { data ->
            data?.let {
                MarketSummaryUiState.Success(it)
            } ?: run {
                MarketSummaryUiState.Loading
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MarketSummaryUiState.Loading
        )

    // We use a Channel so errors are consumed once and don't reappear on rotation.
    private val _errorEvents: Channel<String> = Channel()
    val errorEvents = _errorEvents.receiveAsFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    init {
        // (Fetches if Today's report is missing OR if Yesterday's report is stale)
        refreshData(force = false)
    }

    /**
     * Triggered by Pull-to-Refresh or User Action.
     * @param force If true, bypasses the "Midnight Rule" and forces a network fetch.
     */
    fun refreshData(force: Boolean = false) = viewModelScope.launch {
        _isRefreshing.value = true
        val result = repository.refreshMarketSummary(force)
        result.onFailure { error ->
            // Send error message to UI
            _errorEvents.send(error.message ?: "Connection failed")
        }
        _isRefreshing.value = false
    }
}