package com.marketlabs.pulse.storage.store.marketIndex

import com.marketlabs.pulse.storage.database.dao.MarketIndexDao
import com.marketlabs.pulse.storage.database.entity.MarketIndexEntity
import javax.inject.Inject

class LocalMarketDataSourceImpl @Inject constructor(
    private val marketIndexDao: MarketIndexDao
) : LocalMarketDataSource {

    override fun getLocalMarketIndices() = marketIndexDao.getAllMarketIndices()

    override suspend fun cacheMarketIndices(entities: List<MarketIndexEntity>) =
        marketIndexDao.insertMarketIndices(entities)
}