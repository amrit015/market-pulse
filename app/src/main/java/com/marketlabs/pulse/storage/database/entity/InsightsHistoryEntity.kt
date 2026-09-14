package com.marketlabs.pulse.storage.database.entity

import androidx.room.Entity
import androidx.room.TypeConverters
import com.marketlabs.pulse.storage.database.converters.InsightsHistoryConverters
import com.marketlabs.pulse.storage.model.insights.InsightsHistoryPoint

/**
 * One row per `metricId` -- mirrors `MetricHistoryEntity`'s exact shape (no range key, one cached
 * series per metric), for the Posture/Positioning glossary-detail page's history chart instead of
 * Indicators' metric-detail page. Kept as its own table rather than reusing `metric_history` --
 * `points`' element type differs (`InsightsHistoryPoint` has no `valueDisplay`/`signalColor`), and
 * this app's own convention keeps Posture/Positioning's storage layer separate from Indicators'.
 */
@Entity(tableName = "insights_history", primaryKeys = ["metricId"])
@TypeConverters(InsightsHistoryConverters::class)
data class InsightsHistoryEntity(
    val metricId: String,
    val lastSyncedTimestamp: Long,
    val points: List<InsightsHistoryPoint>? = null
)
