package com.marketlabs.pulse.storage.store.indicators

import com.marketlabs.pulse.storage.database.dao.IndicatorsDao
import com.marketlabs.pulse.storage.model.indicators.MarketIndicators
import com.marketlabs.pulse.storage.model.indicators.mappers.toDomain
import com.marketlabs.pulse.storage.model.indicators.mappers.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LocalIndicatorsDataSourceImpl @Inject constructor(
    private val dao: IndicatorsDao
) : LocalIndicatorsDataSource {

    override fun getLatestCachedIndicators(): Flow<MarketIndicators?> {
        return dao.getLatestCachedIndicators().map { it?.toDomain() }
    }

    override fun getIndicatorsByDate(dateString: String): Flow<MarketIndicators?> {
        return dao.getIndicatorsByDate(dateString).map { it?.toDomain() }
    }

    override suspend fun saveIndicators(indicators: MarketIndicators) {
        dao.insertIndicators(indicators.toEntity())
    }
}