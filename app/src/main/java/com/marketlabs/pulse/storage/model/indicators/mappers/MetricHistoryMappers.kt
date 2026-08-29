package com.marketlabs.pulse.storage.model.indicators.mappers

import com.marketlabs.pulse.network.model.indicators.NetworkMetricHistoryPoint
import com.marketlabs.pulse.storage.database.entity.MetricHistoryEntity
import com.marketlabs.pulse.storage.model.indicators.MetricHistoryPoint
import com.marketlabs.pulse.storage.model.indicators.MetricHistorySeries
import com.marketlabs.pulse.utils.enums.SignalColor

fun List<NetworkMetricHistoryPoint>.toDomain(metricId: String, lastSyncedTimestamp: Long): MetricHistorySeries {
    return MetricHistorySeries(
        metricId = metricId,
        points = mapNotNull { it.toDomain() },
        lastSyncedTimestamp = lastSyncedTimestamp
    )
}

private fun NetworkMetricHistoryPoint.toDomain(): MetricHistoryPoint? {
    val resolvedDate = date ?: return null
    val resolvedValue = value ?: return null
    return MetricHistoryPoint(
        date = resolvedDate,
        value = resolvedValue,
        valueDisplay = valueDisplay,
        signalColor = SignalColor.fromString(signalColor)
    )
}

fun MetricHistorySeries.toEntity(): MetricHistoryEntity {
    return MetricHistoryEntity(
        metricId = metricId,
        lastSyncedTimestamp = lastSyncedTimestamp,
        points = points
    )
}

fun MetricHistoryEntity.toDomain(): MetricHistorySeries {
    return MetricHistorySeries(
        metricId = metricId,
        points = points.orEmpty(),
        lastSyncedTimestamp = lastSyncedTimestamp
    )
}
