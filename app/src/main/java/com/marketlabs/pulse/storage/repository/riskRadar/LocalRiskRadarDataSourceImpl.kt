package com.marketlabs.pulse.storage.repository.riskRadar

import com.marketlabs.pulse.storage.database.dao.RiskRadarDao
import com.marketlabs.pulse.storage.model.riskRadar.RiskRadar
import com.marketlabs.pulse.storage.model.riskRadar.mappers.toDomain
import com.marketlabs.pulse.storage.model.riskRadar.mappers.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LocalRiskRadarDataSourceImpl @Inject constructor(
    private val dao: RiskRadarDao
) : LocalRiskRadarDataSource {

    override fun getLatestCachedRisk(): Flow<RiskRadar?> {
        return dao.getLatestCachedRisk().map { it?.toDomain() }
    }

    override fun getRiskByDate(dateString: String): Flow<RiskRadar?> {
        return dao.getRiskByDate(dateString).map { it?.toDomain() }
    }

    override suspend fun saveRisk(risk: RiskRadar) {
        // Maps the Domain model down to a Room Entity before saving
        dao.insertRisk(risk.toEntity())
    }
}