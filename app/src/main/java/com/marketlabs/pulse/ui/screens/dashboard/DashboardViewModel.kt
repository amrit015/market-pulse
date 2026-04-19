package com.marketlabs.pulse.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marketlabs.pulse.core.dashboard.DashboardRepository
import com.marketlabs.pulse.core.weeklyPlaybook.WeeklyPlaybookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: DashboardRepository,
    private val playbookRepository: WeeklyPlaybookRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    private val _isRefreshing = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)

    // We group the 3 simple status flows into a Triple to stay under the 5-flow limit of 'combine'
    val uiState: StateFlow<DashboardUiState> = combine(
        repository.getMarketStateStream(),
        repository.getDashboardAssetsStream(),
        playbookRepository.getPlaybookStream(),
        combine(_isLoading, _isRefreshing, _errorMessage) { loading, refreshing, error ->
            Triple(loading, refreshing, error)
        }
    ) { state, assets, playbook, statusTriple ->

        // Unpack the triple
        val (loading, refreshing, error) = statusTriple

        DashboardUiState(
            marketState = state,
            assets = assets,
            weeklyPlaybook = playbook,
            isLoading = loading && assets.isEmpty(),
            isRefreshing = refreshing,
            errorMessage = error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState(isLoading = true)
    )

    /**
     * Called by the UI when the screen becomes visible.
     */
    fun onStart() {
        fetchDashboard(force = false)
    }

    /**
     * Called by the UI when the app goes to the background.
     * We kill the WebSockets to save the user's battery and data!
     */
    fun onStop() {
        repository.closeWebSockets()
    }

    fun refreshDashboard() {
        fetchDashboard(force = true)
    }

    fun clearError() {
        _errorMessage.value = null
    }

    private fun fetchDashboard(force: Boolean) {
        viewModelScope.launch {
            if (force) _isRefreshing.value = true else _isLoading.value = true
            _errorMessage.value = null

            try {
                // 💡 NEW: Fetch both concurrently so the dashboard loads faster
                val dashboardDeferred = async { repository.refreshDashboard(force) }
                val playbookDeferred = async { playbookRepository.refreshPlaybook(force) }

                dashboardDeferred.await()
                playbookDeferred.await()
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Failed to load dashboard"
            } finally {
                _isLoading.value = false
                _isRefreshing.value = false
            }
        }
    }
}