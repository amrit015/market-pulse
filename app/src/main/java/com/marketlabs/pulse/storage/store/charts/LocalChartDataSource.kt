package com.marketlabs.pulse.storage.store.charts

import com.marketlabs.pulse.storage.model.charts.ChartRange
import com.marketlabs.pulse.storage.model.charts.ChartSeries
import kotlinx.coroutines.flow.Flow

interface LocalChartDataSource {

    fun getChartStream(symbol: String, range: ChartRange): Flow<ChartSeries?>
    suspend fun saveChart(chart: ChartSeries)
}
