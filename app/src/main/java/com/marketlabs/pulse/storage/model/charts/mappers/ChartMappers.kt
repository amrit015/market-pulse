package com.marketlabs.pulse.storage.model.charts.mappers

import com.marketlabs.pulse.network.model.charts.NetworkChartPoint
import com.marketlabs.pulse.network.model.charts.NetworkChartResponse
import com.marketlabs.pulse.storage.database.entity.ChartEntity
import com.marketlabs.pulse.storage.model.charts.ChartPoint
import com.marketlabs.pulse.storage.model.charts.ChartRange
import com.marketlabs.pulse.storage.model.charts.ChartSeries

fun NetworkChartResponse.toDomain(symbol: String, range: ChartRange, lastSyncedTimestamp: Long): ChartSeries {
    return ChartSeries(
        symbol = symbol,
        range = range,
        name = name,
        type = type,
        points = dailyCloses.orEmpty().mapNotNull { it.toDomain() },
        lastSyncedTimestamp = lastSyncedTimestamp
    )
}

private fun NetworkChartPoint.toDomain(): ChartPoint? {
    val resolvedDate = date ?: return null
    val resolvedPrice = price ?: return null
    return ChartPoint(date = resolvedDate, price = resolvedPrice)
}

fun ChartSeries.toEntity(): ChartEntity {
    return ChartEntity(
        symbol = symbol,
        rangeKey = range.rangeKey,
        lastSyncedTimestamp = lastSyncedTimestamp,
        name = name,
        type = type,
        points = points
    )
}

fun ChartEntity.toDomain(range: ChartRange): ChartSeries {
    return ChartSeries(
        symbol = symbol,
        range = range,
        name = name,
        type = type,
        points = points.orEmpty(),
        lastSyncedTimestamp = lastSyncedTimestamp
    )
}
