package com.marketlabs.pulse.ui.screens.insights.glossary

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marketlabs.pulse.R
import com.marketlabs.pulse.core.charts.computeAvailableChartRanges
import com.marketlabs.pulse.core.charts.filteredForRange
import com.marketlabs.pulse.core.charts.resolveEffectiveRange
import com.marketlabs.pulse.core.glossary.MetricGlossaryBand
import com.marketlabs.pulse.core.glossary.MetricGlossaryProvider
import com.marketlabs.pulse.core.insights.InsightsHistoryRepository
import com.marketlabs.pulse.storage.model.charts.ChartRange
import com.marketlabs.pulse.ui.screens.insights.glossary.GlossaryDetailViewModel.Companion.ARG_CHART_METRIC_ID
import com.marketlabs.pulse.ui.screens.insights.glossary.GlossaryDetailViewModel.Companion.ARG_DESCRIPTION
import com.marketlabs.pulse.ui.screens.insights.glossary.GlossaryDetailViewModel.Companion.ARG_METRIC_IDS
import com.marketlabs.pulse.ui.screens.insights.glossary.GlossaryDetailViewModel.Companion.ARG_STATUS
import com.marketlabs.pulse.ui.screens.insights.glossary.GlossaryDetailViewModel.Companion.ARG_TITLE
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Drives the pushed `glossaryDetail/{title}/{metricIds}/{chartMetricId}/{description}/{status}`
 * destination for a whole Positioning/Posture card. `metricIds` is comma-joined (every id in
 * `core/glossary/` is plain lowercase/dot/underscore, so no delimiter collision risk, unlike
 * `title`/`description`/`status` which carry spaces/parens and are `Uri.encode()`-d by the caller
 * -- read back here with no manual decode, since Navigation's own path-segment matching already
 * decodes those; see the earlier single-value version of this class for exactly why a second,
 * mismatched manual decode pass crashed). `title`/`sections`/`mergedBands`/`currentBandIndex` stay
 * a plain synchronous in-memory lookup, computed once in `init`, exactly as before this class had a
 * chart -- glossary content never changes underneath an already-open page.
 *
 * `chartMetricId`/`historyPoints`/`selectedChartRange`/`availableChartRanges` are new: an async
 * `InsightsHistoryRepository` read, fetched once in `onStart()` (capped at [HISTORY_LIMIT], same
 * reasoning as `MetricDetailViewModel`'s identical constant for Indicators), plus the same
 * date-based range-picker machinery that ViewModel uses (`core/charts/HistoryChartRangeFiltering.kt`,
 * shared rather than duplicated). This is the one part of this ViewModel that genuinely needs to be
 * a `StateFlow` now -- everything else here was already synchronous before this chart existed.
 */
@HiltViewModel
class GlossaryDetailViewModel @Inject constructor(
    @ApplicationContext context: Context,
    glossaryProvider: MetricGlossaryProvider,
    private val insightsHistoryRepository: InsightsHistoryRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val title: String = checkNotNull(savedStateHandle[ARG_TITLE]) {
        "GlossaryDetailViewModel requires a non-null \"$ARG_TITLE\" nav argument"
    }
    private val chartMetricId: String = checkNotNull(savedStateHandle[ARG_CHART_METRIC_ID]) {
        "GlossaryDetailViewModel requires a non-null \"$ARG_CHART_METRIC_ID\" nav argument"
    }

    private val sections: List<GlossarySection>
    private val mergedBands: List<MetricGlossaryBand>
    private val currentBandIndex: Int?
    private val description: String?

    private val _isHistoryLoading = MutableStateFlow(false)
    private val _selectedChartRange = MutableStateFlow(ChartRange.ONE_MONTH)

    init {
        val metricIdsRaw: String = checkNotNull(savedStateHandle[ARG_METRIC_IDS]) {
            "GlossaryDetailViewModel requires a non-null \"$ARG_METRIC_IDS\" nav argument"
        }
        val metricIds: List<String> = metricIdsRaw.split(",")
        description = (savedStateHandle.get<String>(ARG_DESCRIPTION)).takeUnless { it.isNullOrBlank() }
        val status: String? = (savedStateHandle.get<String>(ARG_STATUS)).takeUnless { it.isNullOrBlank() }

        sections = metricIds.mapNotNull { metricId ->
            val entry = glossaryProvider.get(metricId) ?: return@mapNotNull null
            GlossarySection(
                label = context.getString(labelResFor(metricId)),
                whatItIs = entry.whatItIs,
                howToRead = entry.howToRead,
                gotchas = entry.gotchas
            )
        }

        // 💡 Bands are collected across ALL of this card's glossary entries, not just one -- see
        // GlossaryDetailUiState's doc comment for why (a short-interest instrument's one overall
        // status is computed from two different fields' worth of thresholds).
        mergedBands = metricIds
            .mapNotNull { glossaryProvider.get(it) }
            .flatMap { it.bands }
            .distinctBy { it.label }

        currentBandIndex = status?.let { s ->
            mergedBands.indexOfFirst { it.label.equals(s.trim(), ignoreCase = true) }.takeIf { it >= 0 }
        }
    }

    val uiState: StateFlow<GlossaryDetailUiState> = combine(
        insightsHistoryRepository.getHistoryStream(chartMetricId),
        _isHistoryLoading,
        _selectedChartRange
    ) { historySeries, isHistoryLoading, selectedChartRange ->
        val allPoints = historySeries?.points.orEmpty()
        val availableChartRanges = allPoints.computeAvailableChartRanges(dateOf = { it.date })
        // A picker with 0 or 1 real options isn't a picker -- GlossaryDetailScreen hides it
        // entirely in that case and shows the full unfiltered series instead, same convention
        // MetricDetailViewModel uses for Indicators (see resolveEffectiveRange's own doc comment).
        val effectiveRange = resolveEffectiveRange(selectedChartRange, availableChartRanges)
        GlossaryDetailUiState(
            title = title,
            description = description,
            sections = sections,
            mergedBands = mergedBands,
            currentBandIndex = currentBandIndex,
            chartMetricId = chartMetricId,
            historyPoints = if (effectiveRange != null) allPoints.filteredForRange(effectiveRange, dateOf = { it.date }) else allPoints,
            isHistoryLoading = isHistoryLoading,
            selectedChartRange = effectiveRange ?: selectedChartRange,
            availableChartRanges = availableChartRanges
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = GlossaryDetailUiState(
            title = title,
            description = description,
            sections = sections,
            mergedBands = mergedBands,
            currentBandIndex = currentBandIndex,
            chartMetricId = chartMetricId,
            selectedChartRange = _selectedChartRange.value
        )
    )

    /** Called by the UI when the screen becomes visible. */
    fun onStart() {
        viewModelScope.launch {
            _isHistoryLoading.value = true
            // Deliberately silent on failure -- the chart is a supporting element on this page,
            // not its main content, same reasoning as MetricDetailViewModel's identical fetch.
            insightsHistoryRepository.refreshHistory(chartMetricId, limit = HISTORY_LIMIT)
            _isHistoryLoading.value = false
        }
    }

    /** No stream of its own to stop -- `insightsHistoryRepository`'s stream is scoped to this page's own `chartMetricId`, not shared with any tab. */
    fun onStop() = Unit

    /** Called by the range picker -- a local re-slice of the already-fetched series, no network call. */
    fun onRangeSelected(range: ChartRange) {
        _selectedChartRange.value = range
    }

    companion object {
        /** Must match the nav argument names in the `glossaryDetail/{title}/{metricIds}/{chartMetricId}/{description}/{status}` route. */
        const val ARG_TITLE = "title"
        const val ARG_METRIC_IDS = "metricIds"
        const val ARG_CHART_METRIC_ID = "chartMetricId"
        const val ARG_DESCRIPTION = "description"
        const val ARG_STATUS = "status"

        /** Same hard cap Indicators' `MetricDetailViewModel` uses -- see that constant's own doc comment. */
        private const val HISTORY_LIMIT = 180

        /**
         * Static metric-id -> display-label mapping for a merged card's sub-section headers.
         * Duplicated from each card's own title string rather than passed as a further nav
         * argument -- these 9 ids are a fixed, closed set (the same 9 `core/glossary/` entries the
         * 2026-08-27 interpretive-layer spec added), so a compile-time mapping here is simpler
         * than round-tripping more `Uri.encode()`-d strings through the nav route.
         */
        private fun labelResFor(metricId: String): Int = when (metricId) {
            "posture.naaim_exposure" -> R.string.posture_naaim_title
            "posture.dark_pool_index" -> R.string.posture_dix_title
            "posture.net_liquidity" -> R.string.posture_net_liquidity_title
            "positioning.aaii_bull_bear_spread" -> R.string.positioning_retail_sentiment_title
            "positioning.cot_nc_net_pct_oi" -> R.string.positioning_futures_caption
            "positioning.cot_percentile" -> R.string.positioning_cot_percentile_title
            "positioning.short_interest_days_to_cover" -> R.string.positioning_days_to_cover_title
            "positioning.short_interest_shares" -> R.string.positioning_short_shares_title
            "positioning.short_interest_mom_change" -> R.string.positioning_mom_change_title
            else -> R.string.glossary_entry_unavailable
        }
    }
}
