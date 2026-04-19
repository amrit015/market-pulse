package com.marketlabs.pulse.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marketlabs.pulse.core.dashboard.DashboardRepository
import com.marketlabs.pulse.core.sync.SyncManager
import com.marketlabs.pulse.core.weeklyPlaybook.WeeklyPlaybookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val playbookRepository: WeeklyPlaybookRepository,
    private val syncManager: SyncManager
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    private val _isRefreshing = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<DashboardUiState> = combine(
        repository.getMarketStateStream(),
        repository.getDashboardAssetsStream(),
        playbookRepository.getPlaybookStream(),
        combine(_isLoading, _isRefreshing, _errorMessage) { loading, refreshing, error ->
            Triple(loading, refreshing, error)
        }
    ) { state, assets, playbook, statusTriple ->

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

    fun onStart() {
        // 💡 1. Wake up the global listener
        syncManager.startListening()

        // 2. Fetch the Dashboard layout as normal
        fetchDashboard(force = false)
    }

    fun onStop() {
        repository.closeWebSockets()
        // 💡 Put the listener to sleep to save battery
        syncManager.stopListening()
    }

    fun refreshDashboard() {
        fetchDashboard(force = true)
        // Manually force the playbook to refresh if the user swipes down
        viewModelScope.launch { playbookRepository.refreshPlaybook(force = true) }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    private fun fetchDashboard(force: Boolean) {
        viewModelScope.launch {
            if (force) _isRefreshing.value = true else _isLoading.value = true
            _errorMessage.value = null

            try {
                // 💡 We only manually fetch the dashboard layout now.
                // The SyncManager automatically handles the Playbook in the background!
                repository.refreshDashboard(force)
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Failed to load dashboard"
            } finally {
                _isLoading.value = false
                _isRefreshing.value = false
            }
        }
    }
}