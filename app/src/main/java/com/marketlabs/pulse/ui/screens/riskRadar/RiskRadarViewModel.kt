package com.marketlabs.pulse.ui.screens.riskRadar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marketlabs.pulse.core.riskRadar.RiskRadarRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RiskRadarViewModel @Inject constructor(
    private val repository: RiskRadarRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)

    /**
     * Combines local loading/error states with both Room streams.
     */
    val uiState: StateFlow<RiskRadarUiState> = combine(
        repository.getRiskStream(),
        repository.getTailRisksStream(),
        _isLoading,
        _errorMessage
    ) { riskData, tailRisksData, loading, error ->
        RiskRadarUiState(
            isLoading = loading,
            riskRadar = riskData,
            tailRisks = tailRisksData,
            errorMessage = error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = RiskRadarUiState(isLoading = true)
    )

    init {
        refreshRisk(force = false)
    }

    /**
     * Refreshes both Risk Radar and Tail Risks concurrently.
     */
    fun refreshRisk(force: Boolean = true) {
        viewModelScope.launch {
            _isLoading.update { true }
            _errorMessage.update { null }

            // Use async to fetch both simultaneously without blocking each other
            val riskDeferred = async { repository.refreshRisk(force) }
            val tailRisksDeferred = async { repository.refreshTailRisks(force) }

            // Await both results
            val results = listOf(riskDeferred.await(), tailRisksDeferred.await())

            // If either failed, extract the error message
            val firstError = results.firstOrNull { it.isFailure }?.exceptionOrNull()
            if (firstError != null) {
                _errorMessage.update { firstError.localizedMessage ?: "Failed to fetch risk data" }
            }

            _isLoading.update { false }
        }
    }
}