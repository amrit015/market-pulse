package com.marketlabs.pulse.core.news

import com.marketlabs.pulse.storage.model.news.MarketNews
import kotlinx.coroutines.flow.Flow

interface NewsRepository {

    fun getNewsStream(): Flow<MarketNews?>
    suspend fun refreshNews(force: Boolean = false): Result<Unit>

    suspend fun getLastSyncedTimestamp(): Long?
    suspend fun updateLastSyncedTimestamp(timestamp: Long)
}