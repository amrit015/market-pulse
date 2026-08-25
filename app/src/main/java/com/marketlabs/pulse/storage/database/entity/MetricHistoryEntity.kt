package com.marketlabs.pulse.storage.database.entity

import androidx.room.Entity
import androidx.room.TypeConverters
import com.marketlabs.pulse.storage.database.converters.MetricHistoryConverters
import com.marketlabs.pulse.storage.model.indicators.MetricHistoryPoint

/**
 * One row per `metricId` -- unlike `market_charts`, there's no range key: the spec explicitly
 * says not to build a range picker for this yet, so each metric only ever has one cached series
 * (whatever the backend's default/max lookback returns). `points` is a JSON-blob column via
 * `MetricHistoryConverters`, same convention as `ChartEntity.points`.
 */
@Entity(tableName = "metric_history", primaryKeys = ["metricId"])
@TypeConverters(MetricHistoryConverters::class)
data class MetricHistoryEntity(
    val metricId: String,
    val lastSyncedTimestamp: Long,
    val points: List<MetricHistoryPoint>? = null
)
