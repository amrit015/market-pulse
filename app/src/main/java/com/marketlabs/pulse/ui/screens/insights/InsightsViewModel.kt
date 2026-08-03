package com.marketlabs.pulse.ui.screens.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marketlabs.pulse.core.marketRisk.MarketRiskRepository
import com.marketlabs.pulse.core.posture.MarketPostureRepository
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
    private val postureRepository: MarketPostureRepository,
    private val syncManager: SyncManager
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)

    // 💡 Update combine to handle the 5 streams
    val uiState: StateFlow<InsightsUiState> = combine(
        riskRepository.getTailRisksStream(),
        playbookRepository.getPlaybookStream(),
        postureRepository.getPostureStream(), // 💡 NEW stream
        _isLoading,
        _errorMessage
    ) { tailRisksData, playbookData, postureData, loading, error ->
        InsightsUiState(
            isLoading = loading,
            weeklyPlaybook = playbookData,
            tailRisks = tailRisksData,
            marketPosture = postureData, // 💡 NEW data mapping
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

            // Fetch all concurrently
            val tailRisksDeferred = async { riskRepository.refreshTailRisks(force) }
            val playbookDeferred = async { playbookRepository.refreshPlaybook(force) }
            val postureDeferred = async { postureRepository.refreshPosture(force) }

            val results = listOf(
                tailRisksDeferred.await(),
                playbookDeferred.await(),
                postureDeferred.await()
            )

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