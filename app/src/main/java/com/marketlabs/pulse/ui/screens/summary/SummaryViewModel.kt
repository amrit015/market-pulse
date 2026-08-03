package com.marketlabs.pulse.ui.screens.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marketlabs.pulse.core.summary.SummaryRepository
import com.marketlabs.pulse.core.sync.SyncManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SummaryViewModel @Inject constructor(
    private val repository: SummaryRepository,
    private val syncManager: SyncManager
) : ViewModel() {

    // COMBINE BOTH STREAMS INTO ONE STATE
    val summaryUiState: StateFlow<SummaryUiState> = combine(
        repository.getMarketPulseStream(),
    ) { v3Data ->
        // We only consider it "Success" if we have the primary V3 data
        SummaryUiState.Success(dataV3 = v3Data[0])
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SummaryUiState.Loading
    )

    private val _errorEvents: Channel<String> = Channel()
    val errorEvents = _errorEvents.receiveAsFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    /**
     * Wakes up the global listener when the Summary tab is visible.
     */
    fun onStart() {
        syncManager.startListening()
    }

    /**
     * Puts the listener to sleep when the app is backgrounded.
     */
    fun onStop() {
        syncManager.stopListening()
    }

    /**
     * Retained for manual Pull-To-Refresh.
     */
    fun refreshData(force: Boolean = true) = viewModelScope.launch {
        _isRefreshing.value = true
        val result = repository.refreshMarketSummary(force)
        result.onFailure { error ->
            _errorEvents.send(error.message ?: "Connection failed")
        }
        _isRefreshing.value = false
    }
}