package com.marketlabs.pulse.ui.screens.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marketlabs.pulse.core.summary.SummaryRepository
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
    private val repository: SummaryRepository
) : ViewModel() {

    // 👇 COMBINE BOTH STREAMS INTO ONE STATE
    val summaryUiState: StateFlow<SummaryUiState> = combine(
        repository.getMarketPulseStream(),
        repository.getDailyPulseStream()
    ) { v3Data, v2Data ->
        // We only consider it "Success" if we have the primary V3 data
        if (v3Data != null) {
            SummaryUiState.Success(dataV3 = v3Data, dataV2 = v2Data)
        } else {
            SummaryUiState.Loading
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SummaryUiState.Loading
    )

    private val _errorEvents: Channel<String> = Channel()
    val errorEvents = _errorEvents.receiveAsFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    init {
        refreshData(force = false)
    }

    fun refreshData(force: Boolean = false) = viewModelScope.launch {
        _isRefreshing.value = true
        val result = repository.refreshMarketSummary(force)
        result.onFailure { error ->
            _errorEvents.send(error.message ?: "Connection failed")
        }
        _isRefreshing.value = false
    }
}