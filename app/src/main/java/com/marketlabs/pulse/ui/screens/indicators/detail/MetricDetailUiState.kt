package com.marketlabs.pulse.ui.screens.indicators.detail

import com.marketlabs.pulse.core.glossary.MetricGlossaryEntry
import com.marketlabs.pulse.storage.model.charts.ChartRange
import com.marketlabs.pulse.storage.model.indicators.DomainUnifiedMetric
import com.marketlabs.pulse.storage.model.indicators.MetricHistoryPoint

/**
 * Drives the pushed metric-detail page (replaces `IndicatorDetailSheet` -- every indicator card
 * tap now pushes a real destination instead of opening a sheet, matching `AssetDetailUiState`'s
 * shape). `metric`/`glossaryEntry` are cross-references into already-loaded state (the live
 * reading from `IndicatorsRepository`'s stream, the glossary bundle from `MetricGlossaryProvider`),
 * not separate fetches -- same "no data of its own beyond the tap target" reasoning
 * `AssetDetailUiState` uses. `historyPoints` is this page's own on-demand `MetricHistoryRepository`
 * read, same pattern `AssetDetailUiState`/`StockDetailUiState` use for their period charts --
 * already sliced down to `selectedChartRange` by the ViewModel, not the raw fetched series.
 * `availableChartRanges` mirrors those same two UiStates' own field of the same name (which
 * buttons `ChartRangePicker` should show, given how far this metric's own history actually goes).
 */
data class MetricDetailUiState(
    val metricId: String,
    val metric: DomainUnifiedMetric? = null,
    val glossaryEntry: MetricGlossaryEntry? = null,
    val historyPoints: List<MetricHistoryPoint> = emptyList(),
    val isHistoryLoading: Boolean = false,
    val selectedChartRange: ChartRange = ChartRange.FIVE_DAY,
    val availableChartRanges: List<ChartRange> = ChartRange.entries - ChartRange.ONE_DAY
)
