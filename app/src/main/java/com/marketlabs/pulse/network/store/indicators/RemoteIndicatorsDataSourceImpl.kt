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
                // 1. Fetch the 3 new pillars concurrently
                val phaseDef = async { api.getMarketPhase() }
                val vitalsDef = async { api.getMacroVitals() }
                val actionDef = async { api.getMarketAction() }

                val phaseRes = phaseDef.await()
                val vitalsRes = vitalsDef.await()
                val actionRes = actionDef.await()

                // 2. Safely map them into our Master Domain Object
                val domainModel = MarketIndicators(
                    dateId = dateId,
                    lastSyncedTimestamp = System.currentTimeMillis(),
                    marketPhase = phaseRes.toDomain(),
                    macroVitals = vitalsRes.toDomain(),
                    marketAction = actionRes.toDomain()
                )

                Result.success(domainModel)
            }
        } catch (e: Exception) {
            Log.e("MarketIndicators", "Failed to fetch Three Pillar Data", e)
            Result.failure(e)
        }
    }
}