package com.marketlabs.pulse.network.store.riskRadar

import android.util.Log
import com.marketlabs.pulse.network.api.RiskRadarApi
import com.marketlabs.pulse.storage.model.riskRadar.RiskRadar
import com.marketlabs.pulse.storage.model.riskRadar.mappers.toDomain
import javax.inject.Inject


class RemoteRiskRadarDataSourceImpl @Inject constructor(
    private val api: RiskRadarApi
) : RemoteRiskRadarDataSource {

    override suspend fun getLatestRisk(): Result<RiskRadar> {
        return try {
            val networkResponse = api.getLatestRisk()

            // Optional validation matching your MarketSummary logic
            if (networkResponse.score == null) {
                return Result.failure(Exception("Invalid Data: Missing Risk Score"))
            }

            // Immediately map to the Domain model on success
            Result.success(networkResponse.toDomain())

        } catch (e: Exception) {
            Log.e("RiskRadar", "Failed to fetch Risk Radar Data", e)
            Result.failure(e)
        }
    }
}