package com.marketlabs.pulse.storage.store.summary

import com.marketlabs.pulse.storage.model.summary.MarketPulse
import kotlinx.coroutines.flow.Flow

interface LocalSummaryDataSource {

    fun getLatestMarketPulse(): Flow<MarketPulse?>
    suspend fun saveMarketPulse(pulse: MarketPulse)

    fun getLatestDailyPulse(): Flow<MarketPulse?>
    suspend fun saveDailyPulse(pulse: MarketPulse)

    suspend fun clearAll()
}