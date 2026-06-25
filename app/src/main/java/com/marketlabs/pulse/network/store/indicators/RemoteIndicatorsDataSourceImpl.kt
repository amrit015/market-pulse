package com.marketlabs.pulse.network.store.indicators

import android.util.Log
import com.marketlabs.pulse.network.api.IndicatorsApi
import com.marketlabs.pulse.storage.model.indicators.MarketIndicators
import com.marketlabs.pulse.storage.model.indicators.mappers.toDomain
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class RemoteIndicatorsDataSourceImpl @Inject constructor(
    private val api: IndicatorsApi
) : RemoteIndicatorsDataSource {

    override suspend fun getLatestIndicators(dateId: String): Result<MarketIndicators> {
        return try {
            coroutineScope {
                // 1. Fetch the 5 new pillars concurrently
                val synthesisDef = async { api.getAiSynthesis() }
                val tacticalDef = async { api.getTacticalMomentum() }
                val riskDef = async { api.getSystemicRisk() }
                val valuationDef = async { api.getValuation() }
                val vitalsDef = async { api.getMacroVitals() }

                val synthesisRes = synthesisDef.await()
                val tacticalRes = tacticalDef.await()
                val riskRes = riskDef.await()
                val valuationRes = valuationDef.await()
                val vitalsRes = vitalsDef.await()

                // 2. Safely map them into our Master Domain Object
                val domainModel = MarketIndicators(
                    dateId = dateId,
                    lastSyncedTimestamp = System.currentTimeMillis(),
                    aiSynthesis = synthesisRes.toDomain(),
                    tacticalMomentum = tacticalRes.toDomain(),
                    systemicRisk = riskRes.toDomain(),
                    valuation = valuationRes.toDomain(),
                    macroVitals = vitalsRes.toDomain()
                )

                Result.success(domainModel)
            }
        } catch (e: Exception) {
            Log.e("MarketIndicators", "Failed to fetch Five Pillar Data", e)
            Result.failure(e)
        }
    }
}