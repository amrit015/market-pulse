package com.marketlabs.pulse.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marketlabs.pulse.core.dashboard.DashboardRepository
import com.marketlabs.pulse.storage.model.dashboard.AssetOverview
import com.marketlabs.pulse.storage.model.dashboard.MarketState
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
    private val repository: DashboardRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    private val _isRefreshing = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<DashboardUiState> = combine(
        repository.getMarketStateStream(),
        repository.getDashboardAssetsStream(),
        _isLoading,
        _isRefreshing,
        _errorMessage
    ) { state: MarketState?, assets: List<AssetOverview?>, loading: Boolean, refreshing: Boolean, error: String? ->
        DashboardUiState(
            marketState = state,
            assets = assets,
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
                // The repository handles the 15-min AI caching and WebSocket connection internally
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