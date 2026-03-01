package com.marketlabs.pulse.storage.store.marketIndex

import com.marketlabs.pulse.storage.database.entity.MarketIndexEntity
import kotlinx.coroutines.flow.Flow

interface LocalMarketDataSource {

    fun getLocalMarketIndices(): Flow<List<MarketIndexEntity>>

    suspend fun cacheMarketIndices(entities: List<MarketIndexEntity>)
}