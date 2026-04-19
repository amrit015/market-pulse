package com.marketlabs.pulse.storage.store.riskRadar

import com.marketlabs.pulse.storage.model.riskRadar.MarketRiskAssessment
import com.marketlabs.pulse.storage.model.riskRadar.RiskRadar
import kotlinx.coroutines.flow.Flow

interface LocalRiskRadarDataSource {

    fun getLatestCachedRisk(): Flow<RiskRadar?>
    fun getRiskByDate(dateString: String): Flow<RiskRadar?>
    suspend fun saveRisk(risk: RiskRadar)
    fun getLatestCachedTailRisks(): Flow<MarketRiskAssessment?>
    fun getTailRisksByDate(dateString: String): Flow<MarketRiskAssessment?>
    suspend fun saveTailRisks(assessment: MarketRiskAssessment)
    suspend fun getLastSyncedTimestampRisk(): Long?
    suspend fun updateLastSyncedTimestampRisk(timestamp: Long)
    suspend fun getLastSyncedTimestampTailRisks(): Long?
    suspend fun updateLastSyncedTimestampTailRisks(timestamp: Long)
}