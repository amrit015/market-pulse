package com.marketlabs.pulse.network.store.indicators

import android.util.Log
import com.marketlabs.pulse.network.api.IndicatorsApi
import com.marketlabs.pulse.storage.model.indicators.MarketIndicators
import com.marketlabs.pulse.storage.model.indicators.mappers.toDomain // You'll create this mapping extension
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class RemoteIndicatorsDataSourceImpl @Inject constructor(
    private val api: IndicatorsApi
) : RemoteIndicatorsDataSource {

    override suspend fun getLatestIndicators(dateId: String): Result<MarketIndicators> {
        return try {
            coroutineScope {
                // Fetch all 4 documents concurrently
                val summaryDef = async { api.getSummary() }
                val trendDef = async { api.getTrend() }
                val healthDef = async { api.getHealth() }
                val riskDef = async { api.getRisk() }

                val summaryRes = summaryDef.await()
                val trendRes = trendDef.await()
                val healthRes = healthDef.await()
                val riskRes = riskDef.await()

                if (summaryRes.verdict?.score == null) {
                    return@coroutineScope Result.failure(Exception("Invalid Data: Missing Traffic Light Score"))
                }

                // Map the 4 network responses into a single Domain object
                val domainModel = MarketIndicators(
                    dateId = dateId,
                    lastSyncedTimestamp = System.currentTimeMillis(),
                    lastUpdated = summaryRes.lastUpdated ?: 0L,
                    summary = summaryRes.toDomain(),
                    trendPhase = trendRes.toDomain(),
                    healthPhase = healthRes.toDomain(),
                    riskPhase = riskRes.toDomain()
                )

                Result.success(domainModel)
            }
        } catch (e: Exception) {
            Log.e("MarketIndicators", "Failed to fetch Traffic Light Data", e)
            Result.failure(e)
        }
    }
}