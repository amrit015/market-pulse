package com.marketlabs.pulse.storage.store.riskRadar

import com.marketlabs.pulse.storage.model.riskRadar.RiskRadar
import kotlinx.coroutines.flow.Flow

interface LocalRiskRadarDataSource {

    fun getLatestCachedRisk(): Flow<RiskRadar?>
    fun getRiskByDate(dateString: String): Flow<RiskRadar?>
    suspend fun saveRisk(risk: RiskRadar)
}