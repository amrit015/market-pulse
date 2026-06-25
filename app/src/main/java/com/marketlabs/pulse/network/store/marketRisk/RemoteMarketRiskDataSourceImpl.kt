package com.marketlabs.pulse.network.store.marketRisk

import android.util.Log
import com.marketlabs.pulse.network.api.MarketRiskApi
import com.marketlabs.pulse.storage.model.marketRisk.MarketRiskAssessment
import com.marketlabs.pulse.storage.model.marketRisk.mappers.toDomain
import javax.inject.Inject

class RemoteMarketRiskDataSourceImpl @Inject constructor(
    private val api: MarketRiskApi
) : RemoteMarketRiskDataSource {

    override suspend fun getLatestTailRisks(todayDateString: String): Result<MarketRiskAssessment> {
        return try {
            val networkResponse = api.getLatestTailRisks()

            if (networkResponse.risks.isNullOrEmpty()) {
                return Result.failure(Exception("Invalid Data: Missing Risk Factors"))
            }

            Result.success(networkResponse.toDomain(todayDateString))
        } catch (e: Exception) {
            Log.e("MarketRisk", "Failed to fetch Tail Risks Data", e)
            Result.failure(e)
        }
    }
}