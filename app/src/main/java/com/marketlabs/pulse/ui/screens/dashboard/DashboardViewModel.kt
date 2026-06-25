package com.marketlabs.pulse.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marketlabs.pulse.core.dashboard.DashboardRepository
import com.marketlabs.pulse.core.sync.SyncManager
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
    private val syncManager: SyncManager
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    private val _isRefreshing = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<DashboardUiState> = combine(
        repository.getMarketStateStream(),
        repository.getDashboardAssetsStream(),
        combine(_isLoading, _isRefreshing, _errorMessage) { loading, refreshing, error ->
            Triple(loading, refreshing, error)
        }
    ) { state, assets, statusTriple ->

        val (loading, refreshing, error) = statusTriple

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

    fun onStart() {
        syncManager.startListening()
        fetchDashboard(force = false)
    }

    fun onStop() {
        repository.closeWebSockets()
        syncManager.stopListening()
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