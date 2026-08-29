package com.marketlabs.pulse.storage.store.charts

import com.marketlabs.pulse.storage.database.dao.ChartsDao
import com.marketlabs.pulse.storage.model.charts.ChartRange
import com.marketlabs.pulse.storage.model.charts.ChartSeries
import com.marketlabs.pulse.storage.model.charts.mappers.toDomain
import com.marketlabs.pulse.storage.model.charts.mappers.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LocalChartDataSourceImpl @Inject constructor(
    private val dao: ChartsDao
) : LocalChartDataSource {

    override fun getChartStream(symbol: String, range: ChartRange): Flow<ChartSeries?> {
        return dao.getChartStream(symbol, range.rangeKey).map { entity -> entity?.toDomain(range) }
    }

    override suspend fun saveChart(chart: ChartSeries) {
        dao.insertChart(chart.toEntity())
    }
}
