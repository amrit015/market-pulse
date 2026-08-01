package com.marketlabs.pulse.ui.screens.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marketlabs.pulse.core.marketRisk.MarketRiskRepository
import com.marketlabs.pulse.core.sync.SyncManager
import com.marketlabs.pulse.core.weeklyPlaybook.WeeklyPlaybookRepository
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
class InsightsViewModel @Inject constructor(
    private val riskRepository: MarketRiskRepository,
    private val playbookRepository: WeeklyPlaybookRepository,
    private val syncManager: SyncManager
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<InsightsUiState> = combine(
        riskRepository.getTailRisksStream(),
        playbookRepository.getPlaybookStream(),
        _isLoading,
        _errorMessage
    ) { tailRisksData, playbookData, loading, error ->
        InsightsUiState(
            isLoading = loading,
            weeklyPlaybook = playbookData,
            tailRisks = tailRisksData,
            errorMessage = error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = InsightsUiState(isLoading = true)
    )

    fun onStart() {
        syncManager.startListening()
    }

    fun onStop() {
        syncManager.stopListening()
    }

    fun refreshInsights(force: Boolean = true) {
        viewModelScope.launch {
            _isLoading.update { true }
            _errorMessage.update { null }

            // Fetch both concurrently
            val tailRisksDeferred = async { riskRepository.refreshTailRisks(force) }
            val playbookDeferred = async { playbookRepository.refreshPlaybook(force) }

            val results = listOf(tailRisksDeferred.await(), playbookDeferred.await())

            val firstError = results.firstOrNull { it.isFailure }?.exceptionOrNull()
            if (firstError != null) {
                _errorMessage.update { firstError.localizedMessage ?: "Failed to fetch insights data" }
            }

            _isLoading.update { false }
        }
    }

    fun clearError() {
        _errorMessage.update { null }
    }
}