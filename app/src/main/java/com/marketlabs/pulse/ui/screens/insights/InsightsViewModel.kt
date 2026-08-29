package com.marketlabs.pulse.ui.screens.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marketlabs.pulse.core.marketRisk.MarketRiskRepository
import com.marketlabs.pulse.core.positioning.MarketPositioningRepository
import com.marketlabs.pulse.core.posture.MarketPostureRepository
import com.marketlabs.pulse.core.sync.SyncManager
import com.marketlabs.pulse.core.weeklyPlaybook.WeeklyPlaybookRepository
import com.marketlabs.pulse.data.insights.InsightsUiStateRepository
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
    private val positioningRepository: MarketPositioningRepository,
    private val insightsUiStateRepository: InsightsUiStateRepository,
    private val syncManager: SyncManager
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)
    private val _selectedTabIndex = MutableStateFlow(0)

    // 💡 Array<Any?>-based combine() overload -- now 9 streams, well past the max arity (5) of
    // Kotlin's named-parameter combine() overload. Each value is cast back to its real type by
    // index rather than by name.
    val uiState: StateFlow<InsightsUiState> = combine(
        riskRepository.getTailRisksStream(),
        playbookRepository.getPlaybookStream(),
        postureRepository.getPostureStream(),
        positioningRepository.getPositioningStream(),
        insightsUiStateRepository.isPositioningIntroDismissed, // 💡 NEW stream (2026-08-27 interpretive-layer spec)
        insightsUiStateRepository.isPostureIntroDismissed, // 💡 NEW stream
        _isLoading,
        _errorMessage,
        _selectedTabIndex // 💡 NEW stream -- drives the pinned PulseTabRow
    ) { values ->
        InsightsUiState(
            isLoading = values[6] as Boolean,
            weeklyPlaybook = values[1] as com.marketlabs.pulse.storage.model.weeklyPlaybook.WeeklyPlaybook?,
            tailRisks = values[0] as com.marketlabs.pulse.storage.model.marketRisk.MarketRiskAssessment?,
            marketPosture = values[2] as com.marketlabs.pulse.storage.model.posture.DomainMarketPosture?,
            marketPositioning = values[3] as com.marketlabs.pulse.storage.model.positioning.DomainMarketPositioning?,
            selectedTabIndex = values[8] as Int, // 💡 NEW data mapping
            isPositioningIntroDismissed = values[4] as Boolean, // 💡 NEW data mapping
            isPostureIntroDismissed = values[5] as Boolean, // 💡 NEW data mapping
            errorMessage = values[7] as String?
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

    /** Called when the reader taps a tab in the pinned `PulseTabRow`. Index into `InsightsTab.entries`. */
    fun onTabSelected(index: Int) {
        _selectedTabIndex.value = index
    }

    /** Called when the reader taps "Got it" on the Positioning first-time explainer -- persists permanently. */
    fun dismissPositioningIntro() {
        viewModelScope.launch { insightsUiStateRepository.dismissPositioningIntro() }
    }

    /** Called when the reader taps "Got it" on the Posture first-time explainer -- persists permanently. */
    fun dismissPostureIntro() {
        viewModelScope.launch { insightsUiStateRepository.dismissPostureIntro() }
    }

    fun refreshInsights(force: Boolean = true) {
        viewModelScope.launch {
            _isLoading.update { true }
            _errorMessage.update { null }

            // Fetch all concurrently
            val tailRisksDeferred = async { riskRepository.refreshTailRisks(force) }
            val playbookDeferred = async { playbookRepository.refreshPlaybook(force) }
            val postureDeferred = async { postureRepository.refreshPosture(force) }
            val positioningDeferred = async { positioningRepository.refreshPositioning(force) }

            val results = listOf(
                tailRisksDeferred.await(),
                playbookDeferred.await(),
                postureDeferred.await(),
                positioningDeferred.await()
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