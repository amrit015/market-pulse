package com.marketlabs.pulse.storage.model.insights

/**
 * One metric's charted history, cached per `metricId` -- covers all 14 Posture/Positioning metric
 * ids (see `InsightsHistoryPillar`), one shared model since the backend response shape is identical
 * across both pillars.
 */
data class InsightsHistorySeries(
    val metricId: String,
    val points: List<InsightsHistoryPoint> = emptyList(),
    val lastSyncedTimestamp: Long
)

/**
 * Deliberately raw, mirroring `NetworkInsightsHistoryPoint` -- no pre-formatted display string or
 * color here, matching this domain's own backend shape (unlike Indicators' `MetricHistoryPoint`,
 * which gets a backend-computed `valueDisplay`/`signalColor`). `value` has no fixed unit across
 * metrics; formatting it is a UI-layer concern (see `GlossaryDetailScreen`'s per-metric formatter),
 * not something baked into this domain model, matching every other `Domain*` model's own
 * raw-backend-shape convention in this app. `status` is free text with no fixed vocabulary or
 * color enum -- shown as plain text where shown at all, per product decision.
 */
data class InsightsHistoryPoint(
    val date: String,
    val value: Double,
    val status: String?
)
