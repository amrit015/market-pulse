package com.marketlabs.pulse.ui.screens.indicators.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marketlabs.pulse.core.charts.computeAvailableChartRanges
import com.marketlabs.pulse.core.charts.filteredForRange
import com.marketlabs.pulse.core.charts.resolveEffectiveRange
import com.marketlabs.pulse.core.glossary.MetricGlossaryProvider
import com.marketlabs.pulse.core.indicators.IndicatorsRepository
import com.marketlabs.pulse.core.indicators.MetricHistoryRepository
import com.marketlabs.pulse.storage.model.charts.ChartRange
import com.marketlabs.pulse.storage.model.indicators.DomainUnifiedMetric
import com.marketlabs.pulse.storage.model.indicators.MarketIndicators
import com.marketlabs.pulse.ui.screens.indicators.detail.MetricDetailViewModel.Companion.ARG_METRIC_ID
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Drives the pushed metric-detail page, `SavedStateHandle`-scoped by metric id -- same shape as
 * `AssetDetailViewModel`, the precedent for turning a tap into its own nav-graph-scoped ViewModel
 * instance. `metric` cross-references `IndicatorsRepository`'s already-running stream (the same
 * one the Indicators tab reads) rather than a separate fetch -- `MarketIndicators` already holds
 * every metric across all 4 pillars, so this page shows whatever the tapped card last showed and
 * updates live if the Indicators tab's own listener is still active. `glossaryEntry` is a plain
 * synchronous lookup into `MetricGlossaryProvider`'s in-memory bundle, same as
 * `IndicatorsViewModel.glossaryEntryFor` used to provide for the now-deleted `IndicatorDetailSheet`.
 * `historyPoints` (raw, unfiltered) is the one genuinely new fetch this page needs --
 * `MetricHistoryRepository`'s on-demand read, fetched once in `onStart()` same as every other
 * detail screen's period chart, capped at [HISTORY_LIMIT].
 *
 * The range picker (`selectedChartRange`/`availableChartRanges`/[onRangeSelected]) reuses
 * `StockDetailViewModel`/`AssetDetailViewModel`'s `ChartRange` type and `ChartRangePicker`
 * component, but not their `isCoveredByHistory`-based availability check -- that one only asks
 * "does history reach back this far," which is the wrong question for a series that isn't one
 * point per calendar day (see `MetricHistoryPillar.isMacroCadence`'s own doc comment on irregular
 * spacing). `core/charts/HistoryChartRangeFiltering.kt`'s `computeAvailableChartRanges`/
 * `filteredForRange`/`resolveEffectiveRange` (shared with Posture/Positioning's own
 * `GlossaryDetailViewModel`) filter by each point's own real `date` instead, and only keep a range
 * if it actually shows a different, non-degenerate slice of the series -- see their own doc
 * comments. Also doesn't refetch on selection the way those two ViewModels do: this page's single
 * [HISTORY_LIMIT]-capped fetch already holds everything any range button can show, so switching
 * ranges is a pure client-side re-filter of the one cached series.
 */
@HiltViewModel
class MetricDetailViewModel @Inject constructor(
    private val indicatorsRepository: IndicatorsRepository,
    private val glossaryProvider: MetricGlossaryProvider,
    private val metricHistoryRepository: MetricHistoryRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val metricId: String = checkNotNull(savedStateHandle[ARG_METRIC_ID]) {
        "MetricDetailViewModel requires a non-null \"$ARG_METRIC_ID\" nav argument"
    }

    private val _isHistoryLoading = MutableStateFlow(false)

    // Product decision: every indicator on the Indicators tab opens on the 1M chart. For the
    // monthly/quarterly macro metrics this is only ever a *preferred* starting point, not a
    // guarantee -- `computeAvailableChartRanges` below drops ONE_MONTH for most of them (a true
    // 1-month window around any single release only ever contains that one point, see
    // `MetricHistoryPillar.isMacroCadence`'s own doc comment on irregular spacing), and the
    // `effectiveRange` fallback in `uiState` clamps straight to the widest range that *does* show
    // something real whenever the preferred one isn't in `availableChartRanges`.
    private val _selectedChartRange = MutableStateFlow(ChartRange.ONE_MONTH)

    private val matchingMetric: Flow<DomainUnifiedMetric?> = indicatorsRepository.getIndicatorsStream()
        .map { data -> data?.findMetric(metricId) }

    val uiState: StateFlow<MetricDetailUiState> = combine(
        matchingMetric,
        metricHistoryRepository.getHistoryStream(metricId),
        _isHistoryLoading,
        _selectedChartRange
    ) { metric, historySeries, isHistoryLoading, selectedChartRange ->
        val allPoints = historySeries?.points.orEmpty()
        val availableChartRanges = allPoints.computeAvailableChartRanges(dateOf = { it.date })
        // A picker with 0 or 1 real options isn't a picker -- MetricDetailScreen hides it entirely
        // in that case and shows the full unfiltered series instead (see resolveEffectiveRange's
        // own doc comment).
        val effectiveRange = resolveEffectiveRange(selectedChartRange, availableChartRanges)
        MetricDetailUiState(
            metricId = metricId,
            metric = metric,
            glossaryEntry = glossaryProvider.get(metricId),
            historyPoints = if (effectiveRange != null) allPoints.filteredForRange(effectiveRange, dateOf = { it.date }) else allPoints,
            isHistoryLoading = isHistoryLoading,
            selectedChartRange = effectiveRange ?: selectedChartRange,
            availableChartRanges = availableChartRanges
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MetricDetailUiState(metricId = metricId, selectedChartRange = _selectedChartRange.value)
    )

    /** Called by the UI when the screen becomes visible. */
    fun onStart() {
        viewModelScope.launch {
            _isHistoryLoading.value = true
            // Deliberately silent on failure -- the chart is a supporting element on this page,
            // not its main content, same reasoning as StockDetailViewModel's chart fetch.
            metricHistoryRepository.refreshHistory(metricId, limit = HISTORY_LIMIT)
            _isHistoryLoading.value = false
        }
    }

    /** No shared listener to stop -- `indicatorsRepository`'s stream is owned by the Indicators tab, not this page. */
    fun onStop() = Unit

    /** Called by the range picker -- a local re-slice of the already-fetched series, no network call. */
    fun onRangeSelected(range: ChartRange) {
        _selectedChartRange.value = range
    }

    companion object {
        /** Must match the nav argument name in the `metricDetail/{metricId}` route. */
        const val ARG_METRIC_ID = "metricId"

        /**
         * The backend's own hard cap (spec section 6) -- always passed explicitly rather than
         * relying on the per-route default (30/12/48), since the widest range button always wants
         * as much history as the backend will return. For the monthly/quarterly macro metrics this
         * still comes back as just their handful-a-year of real points; for daily-moving metrics
         * it's ~8-9 months, not a full year, since raising the cap itself needs a backend change
         * this Android-side range picker can't make on its own.
         */
        private const val HISTORY_LIMIT = 180
    }
}

private fun MarketIndicators.findMetric(metricId: String): DomainUnifiedMetric? =
    listOfNotNull(tacticalMomentum, systemicRisk, valuation, macroVitals)
        .flatMap { it.metrics }
        .firstOrNull { it.id == metricId }
