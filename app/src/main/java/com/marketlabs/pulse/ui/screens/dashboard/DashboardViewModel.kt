package com.marketlabs.pulse.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marketlabs.pulse.core.dashboard.DashboardRepository
import com.marketlabs.pulse.core.intraday.DashboardIntradayEligibility
import com.marketlabs.pulse.core.intraday.IntradayRepository
import com.marketlabs.pulse.core.news.NewsRepository
import com.marketlabs.pulse.core.sync.SyncManager
import com.marketlabs.pulse.storage.model.intraday.IntradaySeries
import com.marketlabs.pulse.storage.model.news.NewsArticle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

// News-preview StateFlow below added with Claude Code assistance.

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: DashboardRepository,
    private val syncManager: SyncManager,
    private val newsRepository: NewsRepository,
    private val intradayRepository: IntradayRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    private val _isRefreshing = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)
    private var intradayTrackingJob: Job? = null
    private var trackedIntradaySymbols: Set<String> = emptySet()

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

        // Same "track whatever's currently visible, diff on every list change" shape
        // `StockAnalysisViewModel` uses -- `DashboardIntradayEligibility` gates this to the 23
        // symbols the backend actually polls; everything else would just be a guaranteed 404 on
        // `/intraday/:symbol`, so there's no point tracking it.
        intradayTrackingJob = viewModelScope.launch {
            repository.getDashboardAssetsStream()
                .map { assets -> assets.filterNotNull().map { it.symbol } }
                .distinctUntilChanged()
                .collect { symbols ->
                    val newSymbols = symbols.filter { DashboardIntradayEligibility.isEligible(it) }.toSet()
                    (trackedIntradaySymbols - newSymbols).forEach { intradayRepository.untrackSymbol(it) }
                    (newSymbols - trackedIntradaySymbols).forEach { intradayRepository.trackSymbol(it) }
                    trackedIntradaySymbols = newSymbols
                }
        }
    }

    fun onStop() {
        syncManager.stopListening()
        intradayTrackingJob?.cancel()
        trackedIntradaySymbols.forEach { intradayRepository.untrackSymbol(it) }
        trackedIntradaySymbols = emptySet()
    }

    /** Thin pass-through so `AssetCard` can collect its own symbol's polled intraday bars. */
    fun getIntradayStream(symbol: String): Flow<IntradaySeries?> =
        intradayRepository.getIntradayStream(symbol)

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
