package com.marketlabs.pulse.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marketlabs.pulse.core.dashboard.DashboardRepository
import com.marketlabs.pulse.core.news.NewsRepository
import com.marketlabs.pulse.core.sync.SyncManager
import com.marketlabs.pulse.storage.model.news.NewsArticle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

// News-preview StateFlow below added with Claude Code assistance.

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: DashboardRepository,
    private val syncManager: SyncManager,
    private val newsRepository: NewsRepository
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

    // --- News preview widget (top few stories; "See all" / a card tap navigates to News) ---

    /**
     * Top 3 latest stories for the Dashboard's condensed `NewsPreviewSection`. No explicit refresh
     * call needed here — `syncManager.startListening()` (already called above) keeps `NewsRepository`
     * populated app-wide, the same way it does for every other domain's Room cache.
     */
    val latestNewsPreview: StateFlow<List<NewsArticle>> = newsRepository.getNewsStream()
        .map { news -> news?.stories?.take(NEWS_PREVIEW_COUNT) ?: emptyList() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private companion object {
        private const val NEWS_PREVIEW_COUNT = 3
    }
}