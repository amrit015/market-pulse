package com.marketlabs.pulse.ui.screens.indicators.detail

import com.marketlabs.pulse.core.glossary.MetricGlossaryEntry
import com.marketlabs.pulse.storage.model.indicators.DomainUnifiedMetric
import com.marketlabs.pulse.storage.model.indicators.MetricHistoryPoint

/**
 * Drives the pushed metric-detail page (replaces `IndicatorDetailSheet` -- every indicator card
 * tap now pushes a real destination instead of opening a sheet, matching `AssetDetailUiState`'s
 * shape). `metric`/`glossaryEntry` are cross-references into already-loaded state (the live
 * reading from `IndicatorsRepository`'s stream, the glossary bundle from `MetricGlossaryProvider`),
 * not separate fetches -- same "no data of its own beyond the tap target" reasoning
 * `AssetDetailUiState` uses. `historyPoints` is this page's own on-demand `MetricHistoryRepository`
 * read, same pattern `AssetDetailUiState`/`StockDetailUiState` use for their period charts.
 */
data class MetricDetailUiState(
    val metricId: String,
    val metric: DomainUnifiedMetric? = null,
    val glossaryEntry: MetricGlossaryEntry? = null,
    val historyPoints: List<MetricHistoryPoint> = emptyList(),
    val isHistoryLoading: Boolean = false
)
