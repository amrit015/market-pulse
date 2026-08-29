package com.marketlabs.pulse.ui.screens.dashboard.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marketlabs.pulse.core.charts.ChartsRepository
import com.marketlabs.pulse.core.dashboard.DashboardRepository
import com.marketlabs.pulse.core.intraday.DashboardIntradayEligibility
import com.marketlabs.pulse.core.intraday.IntradayRepository
import com.marketlabs.pulse.storage.model.charts.ChartRange
import com.marketlabs.pulse.storage.model.charts.ChartSeries
import com.marketlabs.pulse.storage.model.charts.isCoveredByHistory
import com.marketlabs.pulse.storage.model.dashboard.AssetOverview
import com.marketlabs.pulse.storage.model.intraday.IntradaySeries
import com.marketlabs.pulse.ui.screens.dashboard.detail.AssetDetailViewModel.Companion.ARG_SYMBOL
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import javax.inject.Inject

/**
 * Drives the pushed asset-detail page, `SavedStateHandle`-scoped by symbol -- same shape as
 * `StockDetailViewModel`, the precedent for turning a tap into its own nav-graph-scoped ViewModel
 * instance. No data of its own beyond the chart: `asset` is a cross-reference into
 * `DashboardRepository`'s already-running stream (the same one the Overview tab reads), not a
 * separate fetch, so this page shows whatever the tile itself last showed and updates live if the
 * Overview tab's own listener is still active.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AssetDetailViewModel @Inject constructor(
    private val dashboardRepository: DashboardRepository,
    private val chartsRepository: ChartsRepository,
    private val intradayRepository: IntradayRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Decoded back from the URL-encoded form PulseNavGraph's onNavigateToAssetDetail sends --
    // symbols like "^VIX"/"GC=F" aren't plain-URI-safe, see that callback's comment.
    private val symbol: String = run {
        val encodedSymbol: String = checkNotNull(savedStateHandle[ARG_SYMBOL]) {
            "AssetDetailViewModel requires a non-null \"$ARG_SYMBOL\" nav argument"
        }
        URLDecoder.decode(encodedSymbol, StandardCharsets.UTF_8.toString())
    }

    // Same ~23-symbol gate `DashboardViewModel` already applies to sparkline tracking -- VIX,
    // futures, and sentiment have no live tick feed behind `/intraday/:symbol`, so ONE_DAY isn't
    // offered for them at all (see `availableChartRanges` below), and this page never tracks them.
    private val isIntradayEligible = DashboardIntradayEligibility.isEligible(symbol)

    private val _selectedChartRange = MutableStateFlow(ChartRange.FIVE_DAY)
    private val _isChartLoading = MutableStateFlow(false)

    private val matchingAsset: Flow<AssetOverview?> = dashboardRepository.getDashboardAssetsStream()
        .map { assets -> assets.filterNotNull().firstOrNull { it.symbol == symbol } }

    // Independent of whatever range is actually selected -- this is how `availableChartRanges`
    // learns the symbol's real history depth (see `prefetchHistoryCoverage()`, which does the
    // actual fetching), so a recently-added dashboard asset doesn't offer 6M/YTD/1Y buttons that'd
    // all render the same fully-clipped series as a shorter range already shows.
    private val chartFlow: Flow<ChartUiSlice> = combine(
        _selectedChartRange.flatMapLatest { range -> chartsRepository.getChartStream(symbol, range) },
        _selectedChartRange,
        _isChartLoading,
        intradayRepository.getIntradayStream(symbol),
        chartsRepository.getChartStream(symbol, ChartRange.ONE_YEAR)
    ) { series, range, isLoading, intradaySeries, oneYearSeries ->
        val earliestAvailableDate = oneYearSeries?.points?.firstOrNull()?.date?.let {
            runCatching { LocalDate.parse(it) }.getOrNull()
        }
        val historyFilteredRanges = ChartRange.entries.filter { it.isCoveredByHistory(earliestAvailableDate) }
        ChartUiSlice(
            series = series,
            range = range,
            isLoading = isLoading,
            intradaySeries = intradaySeries,
            availableChartRanges = if (isIntradayEligible) historyFilteredRanges else historyFilteredRanges - ChartRange.ONE_DAY
        )
    }

    val uiState: StateFlow<AssetDetailUiState> = combine(matchingAsset, chartFlow) { asset, chart ->
        AssetDetailUiState(
            symbol = symbol,
            asset = asset,
            chartSeries = chart.series,
            selectedChartRange = chart.range,
            isChartLoading = chart.isLoading,
            intradaySeries = chart.intradaySeries,
            availableChartRanges = chart.availableChartRanges
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AssetDetailUiState(symbol = symbol)
    )

    /** Called by the UI when the screen becomes visible. */
    fun onStart() {
        fetchChart(_selectedChartRange.value, force = false)
        prefetchHistoryCoverage()
        if (isIntradayEligible) {
            intradayRepository.trackSymbol(symbol)
        }
    }

    /** Releases this page's intraday hold, if it took one -- `dashboardRepository`'s own stream is owned by the Overview tab, not this screen. */
    fun onStop() {
        if (isIntradayEligible) {
            intradayRepository.untrackSymbol(symbol)
        }
    }

    /** Called when the user taps a range button on the page's `ChartRangePicker`. */
    fun selectChartRange(range: ChartRange) {
        _selectedChartRange.value = range
        // ONE_DAY has no `/charts/:symbol` fetch to do -- the intraday poll started in onStart()
        // (when eligible) is already live regardless of which range is selected.
        if (range != ChartRange.ONE_DAY) {
            fetchChart(range, force = false)
        }
    }

    private fun fetchChart(range: ChartRange, force: Boolean) {
        viewModelScope.launch {
            _isChartLoading.value = true
            // Deliberately silent on failure -- same reasoning as StockDetailViewModel's chart
            // fetch: this is a supporting element on the page, not its main content.
            chartsRepository.refreshChart(symbol, range, force)
            _isChartLoading.value = false
        }
    }

    /**
     * Silently fetches `ONE_YEAR` purely to learn this symbol's real history depth -- see
     * `chartFlow`'s doc comment. Doesn't touch `_isChartLoading`, since that would race with
     * whichever range the user is actually viewing. Skipped when `ONE_YEAR` is already selected,
     * since `fetchChart` is already fetching it.
     */
    private fun prefetchHistoryCoverage() {
        if (_selectedChartRange.value == ChartRange.ONE_YEAR) return
        viewModelScope.launch {
            chartsRepository.refreshChart(symbol, ChartRange.ONE_YEAR, force = false)
        }
    }

    companion object {
        /** Must match the nav argument name in the `assetDetail/{symbol}` route. */
        const val ARG_SYMBOL = "symbol"
    }
}

private data class ChartUiSlice(
    val series: ChartSeries?,
    val range: ChartRange,
    val isLoading: Boolean,
    val intradaySeries: IntradaySeries?,
    val availableChartRanges: List<ChartRange>
)
