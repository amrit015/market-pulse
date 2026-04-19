package com.marketlabs.pulse.core.indicators

import com.marketlabs.pulse.storage.model.indicators.MarketIndicators
import kotlinx.coroutines.flow.Flow

interface IndicatorsRepository {

    fun getIndicatorsStream(): Flow<MarketIndicators?>

    suspend fun refreshIndicators(force: Boolean): Result<Unit>

    suspend fun getLastSyncedTimestamp(): Long?
    suspend fun updateLastSyncedTimestamp(timestamp: Long)
}