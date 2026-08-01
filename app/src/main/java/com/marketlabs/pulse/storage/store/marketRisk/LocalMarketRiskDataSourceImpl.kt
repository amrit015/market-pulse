package com.marketlabs.pulse.storage.store.marketRisk

import com.marketlabs.pulse.storage.database.dao.MarketRiskDao
import com.marketlabs.pulse.storage.model.marketRisk.MarketRiskAssessment
import com.marketlabs.pulse.storage.model.marketRisk.mappers.toDomain
import com.marketlabs.pulse.storage.model.marketRisk.mappers.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LocalMarketRiskDataSourceImpl @Inject constructor(
    private val dao: MarketRiskDao
) : LocalMarketRiskDataSource {

    override fun getLatestCachedTailRisks(): Flow<MarketRiskAssessment?> = dao.getLatestCachedTailRisks().map { it?.toDomain() }
    override fun getTailRisksByDate(dateString: String): Flow<MarketRiskAssessment?> = dao.getTailRisksByDate(dateString).map { it?.toDomain() }

    override suspend fun saveTailRisks(assessment: MarketRiskAssessment) {
        dao.insertTailRisks(assessment.toEntity())
    }

    // ========================================================================
    // SYNC MANAGER TIMESTAMPS
    // ========================================================================

    override suspend fun getLastSyncedTimestampTailRisks(): Long? = dao.getLastSyncedTimestampTailRisks()
    override suspend fun updateLastSyncedTimestampTailRisks(timestamp: Long) = dao.updateLastSyncedTimestampTailRisks(timestamp)
}