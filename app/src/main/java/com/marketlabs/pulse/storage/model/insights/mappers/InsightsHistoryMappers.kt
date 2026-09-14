package com.marketlabs.pulse.storage.model.insights.mappers

import com.marketlabs.pulse.network.model.insights.NetworkInsightsHistoryPoint
import com.marketlabs.pulse.storage.database.entity.InsightsHistoryEntity
import com.marketlabs.pulse.storage.model.insights.InsightsHistoryPoint
import com.marketlabs.pulse.storage.model.insights.InsightsHistorySeries

fun List<NetworkInsightsHistoryPoint>.toDomain(metricId: String, lastSyncedTimestamp: Long): InsightsHistorySeries {
    return InsightsHistorySeries(
        metricId = metricId,
        points = mapNotNull { it.toDomain() },
        lastSyncedTimestamp = lastSyncedTimestamp
    )
}

private fun NetworkInsightsHistoryPoint.toDomain(): InsightsHistoryPoint? {
    val resolvedDate = date ?: return null
    val resolvedValue = value ?: return null
    return InsightsHistoryPoint(date = resolvedDate, value = resolvedValue, status = status)
}

fun InsightsHistorySeries.toEntity(): InsightsHistoryEntity {
    return InsightsHistoryEntity(
        metricId = metricId,
        lastSyncedTimestamp = lastSyncedTimestamp,
        points = points
    )
}

fun InsightsHistoryEntity.toDomain(): InsightsHistorySeries {
    return InsightsHistorySeries(
        metricId = metricId,
        points = points.orEmpty(),
        lastSyncedTimestamp = lastSyncedTimestamp
    )
}
