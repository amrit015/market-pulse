package com.marketlabs.pulse.network.store.charts

import android.util.Log
import com.marketlabs.pulse.network.api.ChartsApi
import com.marketlabs.pulse.storage.model.charts.ChartRange
import com.marketlabs.pulse.storage.model.charts.ChartSeries
import com.marketlabs.pulse.storage.model.charts.mappers.toDomain
import retrofit2.HttpException
import javax.inject.Inject

class RemoteChartDataSourceImpl @Inject constructor(
    private val api: ChartsApi
) : RemoteChartDataSource {

    override suspend fun getChart(symbol: String, range: ChartRange): Result<ChartSeries?> {
        return try {
            val syncTimestamp = System.currentTimeMillis()
            val response = api.getChart(symbol = symbol, days = range.days, range = range.queryRange)

            Result.success(response.toDomain(symbol = symbol, range = range, lastSyncedTimestamp = syncTimestamp))
        } catch (e: HttpException) {
            if (e.code() == 404) {
                Log.d("Charts", "No chart history yet for $symbol")
                Result.success(null)
            } else {
                Log.e("Charts", "Failed to fetch chart for $symbol", e)
                Result.failure(e)
            }
        } catch (e: Exception) {
            Log.e("Charts", "Failed to fetch chart for $symbol", e)
            Result.failure(e)
        }
    }
}
