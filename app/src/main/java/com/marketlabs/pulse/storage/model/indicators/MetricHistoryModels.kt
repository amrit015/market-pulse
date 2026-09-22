package com.marketlabs.pulse.storage.model.indicators

import com.marketlabs.pulse.utils.enums.SignalColor

/**
 * One metric's charted history, cached per `metricId` (no range key -- each metric caches one
 * series, whatever the backend returns for its default/max lookback; a range picker slices it
 * client-side rather than fetching a selected window).
 */
data class MetricHistorySeries(
    val metricId: String,
    val points: List<MetricHistoryPoint> = emptyList(),
    val lastSyncedTimestamp: Long
)

/**
 * `value` has no fixed unit across metrics (raw percent, ratio, index level, dollars...) --
 * `valueDisplay` is the backend's own pre-formatted label and is what the chart's marker/caption
 * actually renders, never a client-derived format guessed from `value` alone.
 */
data class MetricHistoryPoint(
    val date: String,
    val value: Double,
    val valueDisplay: String?,
    val signalColor: SignalColor
)
