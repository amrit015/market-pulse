package com.marketlabs.pulse.core.charts

import android.util.Log
import com.marketlabs.pulse.network.store.charts.RemoteChartDataSource
import com.marketlabs.pulse.storage.model.charts.ChartRange
import com.marketlabs.pulse.storage.store.charts.LocalChartDataSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ChartsRepositoryImpl @Inject constructor(
    private val localDataSource: LocalChartDataSource,
    private val remoteDataSource: RemoteChartDataSource
) : ChartsRepository {

    override fun getChartStream(symbol: String, range: ChartRange) =
        localDataSource.getChartStream(symbol, range)

    override suspend fun refreshChart(symbol: String, range: ChartRange, force: Boolean): Result<Unit> {
        return try {
            Log.d("Charts", "🌐 Fetching $range chart for $symbol...")

            remoteDataSource.getChart(symbol, range)
                .onSuccess { chart -> chart?.let { localDataSource.saveChart(it) } }
                .onFailure { throw it }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("Charts", "Failed to refresh $range chart for $symbol", e)
            Result.failure(e)
        }
    }
}
