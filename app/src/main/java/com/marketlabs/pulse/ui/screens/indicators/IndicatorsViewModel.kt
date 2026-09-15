package com.marketlabs.pulse.ui.screens.indicators

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marketlabs.pulse.core.indicators.IndicatorsRepository
import com.marketlabs.pulse.core.sync.SyncManager
import com.marketlabs.pulse.data.favorites.FavoriteMetricsRepository
import com.marketlabs.pulse.storage.model.indicators.MarketIndicators
import com.marketlabs.pulse.ui.screens.indicators.views.IndicatorsTab
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class IndicatorsViewModel @Inject constructor(
    private val repository: IndicatorsRepository,
    private val syncManager: SyncManager,
    private val favoriteMetricsRepository: FavoriteMetricsRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    private val _isRefreshing = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)
    // 💡 FAVORITES is first in IndicatorsTab's display order; this starts at Momentum, but
    // `init {}` below bumps it to FAVORITES instead, once, if the reader already has any starred --
    // see that block's own comment for why "once" specifically.
    private val _selectedTabIndex = MutableStateFlow(IndicatorsTab.TACTICAL_MOMENTUM.ordinal)

    // 💡 Landing tab depends on whether the reader has favorited anything yet -- Favorites first if
    // so, Momentum otherwise (empty Favorites tab isn't a useful place to land). This can only be
    // decided once `favoriteMetricIds`' first real value arrives from DataStore, which is
    // asynchronous, so it can't just be the constructor-time default above. Guarded on
    // `_selectedTabIndex.value` still sitting at its untouched default at the moment this resolves --
    // without that check, a reader who taps a different tab before this (rare, but DataStore reads
    // aren't instant) would get silently yanked to Favorites out from under their own tap.
    init {
        viewModelScope.launch {
            val initialFavorites = favoriteMetricsRepository.favoriteMetricIds.first()
            if (initialFavorites.isNotEmpty() && _selectedTabIndex.value == IndicatorsTab.TACTICAL_MOMENTUM.ordinal) {
                _selectedTabIndex.value = IndicatorsTab.FAVORITES.ordinal
            }
        }
    }

    // 💡 Array<Any?>-based combine() overload -- 6 streams, past the max arity (5) of Kotlin's
    // named-parameter combine() overload. Each value is cast back to its real type by index
    // rather than by name, same shape `InsightsViewModel`/`StockAnalysisViewModel` already use.
    val uiState: StateFlow<IndicatorsUiState> = combine(
        repository.getIndicatorsStream(),
        _isLoading,
        _isRefreshing,
        _errorMessage,
        _selectedTabIndex,
        favoriteMetricsRepository.favoriteMetricIds
    ) { values ->
        val indicatorsData = values[0] as MarketIndicators?
        IndicatorsUiState(
            data = indicatorsData,
            isLoading = values[1] as Boolean && indicatorsData == null, // Only show main loader if we have NO data
            isRefreshing = values[2] as Boolean, // Used for the Swipe-to-Refresh spinner
            errorMessage = values[3] as String?,
            selectedTabIndex = values[4] as Int,
            favoriteMetricIds = values[5] as Set<String>
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = IndicatorsUiState(isLoading = true)
    )

    /**
     * Called by the UI when the screen becomes visible.
     * Wakes up the global listener to catch any background updates.
     */
    fun onStart() {
        syncManager.startListening()
    }

    /**
     * Called by the UI when the app goes to the background.
     * Puts the listener to sleep to save battery.
     */
    fun onStop() {
        syncManager.stopListening()
    }

    /**
     * Called by Swipe-To-Refresh on the UI
     */
    fun refreshIndicators() {
        fetchIndicators(force = true)
    }

    /**
     * Clears any active error messages (e.g., after a Snackbar is dismissed)
     */
    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * Called when the pillar tab bar changes selection -- by a tap, or by the tab content
     * settling on a new page after a swipe (see `IndicatorsMainFeed`'s `pagerState` sync).
     */
    fun onTabSelected(index: Int) {
        _selectedTabIndex.value = index
    }

    /** Called when the reader taps a metric card's star -- persists immediately, local-only. */
    fun toggleFavoriteMetric(metricId: String) {
        viewModelScope.launch { favoriteMetricsRepository.toggleFavorite(metricId) }
    }

    private fun fetchIndicators(force: Boolean) {
        viewModelScope.launch {
            if (force) {
                _isRefreshing.value = true
            } else {
                _isLoading.value = true
            }
            _errorMessage.value = null

            val result = repository.refreshIndicators(force)

            if (result.isFailure) {
                _errorMessage.value = result.exceptionOrNull()?.localizedMessage ?: "Failed to load Indicators"
            }

            _isLoading.value = false
            _isRefreshing.value = false
        }
    }
}