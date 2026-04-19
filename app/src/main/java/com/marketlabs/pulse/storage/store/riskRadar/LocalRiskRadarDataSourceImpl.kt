package com.marketlabs.pulse.storage.store.riskRadar

import com.marketlabs.pulse.storage.database.dao.RiskRadarDao
import com.marketlabs.pulse.storage.model.riskRadar.MarketRiskAssessment
import com.marketlabs.pulse.storage.model.riskRadar.RiskRadar
import com.marketlabs.pulse.storage.model.riskRadar.mappers.toDomain
import com.marketlabs.pulse.storage.model.riskRadar.mappers.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LocalRiskRadarDataSourceImpl @Inject constructor(
    private val dao: RiskRadarDao
) : LocalRiskRadarDataSource {

    override fun getLatestCachedRisk(): Flow<RiskRadar?> = dao.getLatestCachedRisk().map { it?.toDomain() }
    override fun getRiskByDate(dateString: String): Flow<RiskRadar?> = dao.getRiskByDate(dateString).map { it?.toDomain() }

    override suspend fun saveRisk(risk: RiskRadar) {
        dao.insertRisk(risk.toEntity())
    }

    override fun getLatestCachedTailRisks(): Flow<MarketRiskAssessment?> = dao.getLatestCachedTailRisks().map { it?.toDomain() }
    override fun getTailRisksByDate(dateString: String): Flow<MarketRiskAssessment?> = dao.getTailRisksByDate(dateString).map { it?.toDomain() }

    override suspend fun saveTailRisks(assessment: MarketRiskAssessment) {
        dao.insertTailRisks(assessment.toEntity())
    }

    // ========================================================================
    // SYNC MANAGER TIMESTAMPS
    // ========================================================================

    override suspend fun getLastSyncedTimestampRisk(): Long? = dao.getLastSyncedTimestampRisk()
    override suspend fun updateLastSyncedTimestampRisk(timestamp: Long) = dao.updateLastSyncedTimestampRisk(timestamp)

    override suspend fun getLastSyncedTimestampTailRisks(): Long? = dao.getLastSyncedTimestampTailRisks()
    override suspend fun updateLastSyncedTimestampTailRisks(timestamp: Long) = dao.updateLastSyncedTimestampTailRisks(timestamp)
}