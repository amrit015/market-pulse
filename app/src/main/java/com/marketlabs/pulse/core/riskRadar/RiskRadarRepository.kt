package com.marketlabs.pulse.core.riskRadar

import com.marketlabs.pulse.storage.model.riskRadar.MarketRiskAssessment
import com.marketlabs.pulse.storage.model.riskRadar.RiskRadar
import kotlinx.coroutines.flow.Flow

interface RiskRadarRepository {

    fun getRiskStream(): Flow<RiskRadar?>

    suspend fun refreshRisk(force: Boolean): Result<Unit>

    fun getTailRisksStream(): Flow<MarketRiskAssessment?>
    suspend fun refreshTailRisks(force: Boolean): Result<Unit>

    suspend fun getLastSyncedTimestampRisk(): Long?
    suspend fun updateLastSyncedTimestampRisk(timestamp: Long)
    suspend fun getLastSyncedTimestampTailRisks(): Long?
    suspend fun updateLastSyncedTimestampTailRisks(timestamp: Long)
}