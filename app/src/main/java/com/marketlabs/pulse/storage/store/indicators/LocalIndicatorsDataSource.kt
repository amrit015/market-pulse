package com.marketlabs.pulse.storage.store.indicators

import com.marketlabs.pulse.storage.model.indicators.MarketIndicators
import kotlinx.coroutines.flow.Flow

interface LocalIndicatorsDataSource {

    fun getLatestCachedIndicators(): Flow<MarketIndicators?>

    fun getIndicatorsByDate(dateString: String): Flow<MarketIndicators?>

    suspend fun saveIndicators(indicators: MarketIndicators)

    suspend fun getLastSyncedTimestamp(): Long?
    suspend fun updateLastSyncedTimestamp(timestamp: Long)
}