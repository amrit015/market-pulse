package com.marketlabs.pulse.storage.store.marketRisk

import com.marketlabs.pulse.storage.model.marketRisk.MarketRiskAssessment
import kotlinx.coroutines.flow.Flow

interface LocalMarketRiskDataSource {
    fun getLatestCachedTailRisks(): Flow<MarketRiskAssessment?>
    fun getTailRisksByDate(dateString: String): Flow<MarketRiskAssessment?>
    suspend fun saveTailRisks(assessment: MarketRiskAssessment)
    suspend fun getLastSyncedTimestampTailRisks(): Long?
    suspend fun updateLastSyncedTimestampTailRisks(timestamp: Long)
}