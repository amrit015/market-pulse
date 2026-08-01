package com.marketlabs.pulse.core.marketRisk

import com.marketlabs.pulse.storage.model.marketRisk.MarketRiskAssessment
import kotlinx.coroutines.flow.Flow

interface MarketRiskRepository {

    fun getTailRisksStream(): Flow<MarketRiskAssessment?>
    suspend fun refreshTailRisks(force: Boolean): Result<Unit>
    suspend fun getLastSyncedTimestampTailRisks(): Long?
    suspend fun updateLastSyncedTimestampTailRisks(timestamp: Long)
}