package com.marketlabs.pulse.ui.screens.riskRadar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marketlabs.pulse.core.riskRadar.RiskRadarRepository
import com.marketlabs.pulse.core.sync.SyncManager
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
    private val repository: RiskRadarRepository,
    private val syncManager: SyncManager // 💡 INJECT THE SYNC MANAGER
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)

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
        // 💡 Prevent Empty State flash on load
        initialValue = RiskRadarUiState(isLoading = true)
    )

    /**
     * Wakes up the SyncManager to monitor for backend updates.
     */
    fun onStart() {
        syncManager.startListening()
    }

    /**
     * Puts the listener to sleep to save battery.
     */
    fun onStop() {
        syncManager.stopListening()
    }

    /**
     * Explicit User Pull-To-Refresh.
     * Refreshes both Risk Radar and Tail Risks concurrently.
     */
    fun refreshRisk(force: Boolean = true) {
        viewModelScope.launch {
            _isLoading.update { true }
            _errorMessage.update { null }

            val riskDeferred = async { repository.refreshRisk(force) }
            val tailRisksDeferred = async { repository.refreshTailRisks(force) }

            val results = listOf(riskDeferred.await(), tailRisksDeferred.await())

            val firstError = results.firstOrNull { it.isFailure }?.exceptionOrNull()
            if (firstError != null) {
                _errorMessage.update { firstError.localizedMessage ?: "Failed to fetch risk data" }
            }

            _isLoading.update { false }
        }
    }
}