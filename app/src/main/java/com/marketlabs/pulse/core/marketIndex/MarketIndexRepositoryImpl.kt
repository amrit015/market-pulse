package com.marketlabs.pulse.core.marketIndex

import com.marketlabs.pulse.network.store.marketIndex.RemoteMarketDataSource
import com.marketlabs.pulse.storage.database.entity.MarketIndexEntity
import com.marketlabs.pulse.storage.model.MarketTrend
import com.marketlabs.pulse.storage.store.marketIndex.LocalMarketDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MarketIndexRepositoryImpl @Inject constructor(
    private val remoteMarketDataSource: RemoteMarketDataSource,
    private val localMarketDataSource: LocalMarketDataSource
) : MarketIndexRepository{

    /**
     * Exposes a continuous stream of market data from the local database.
     * The UI observes this flow to stay updated.
     */
    val marketIndices: Flow<List<MarketIndexEntity>> = localMarketDataSource.getLocalMarketIndices()

    override suspend fun refreshMarketIndicesData() = withContext(Dispatchers.IO) {
        // A. Get the Analysis (DTOs)
        val analysisList = remoteMarketDataSource.getRemoteMarketAnalysis()

        if (analysisList.isEmpty()) return@withContext

        // B. Fetch Prices & Map to Entities
        val entities = analysisList.map { analysis ->

            // Fetch live price (or null if failed)
            val quote = remoteMarketDataSource.getRemoteMarketIndexPrice(analysis.symbol)

            // Convert String Trend ("Bullish") -> Enum (MarketTrend.BULLISH)
            val trendEnum = MarketTrend.fromString(analysis.technicalStatus)

            MarketIndexEntity(
                symbol = analysis.symbol,
                trend = trendEnum,
                rsi = analysis.rsi,
                currentPrice = quote?.currentPrice ?: 0.0,
                percentChange = quote?.percentChange ?: 0.0,
                lastUpdated = System.currentTimeMillis()
            )
        }

        // C. Update the local database
        localMarketDataSource.cacheMarketIndices(entities)
    }

}