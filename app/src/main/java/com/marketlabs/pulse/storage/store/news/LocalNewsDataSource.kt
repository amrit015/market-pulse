package com.marketlabs.pulse.storage.store.news

import com.marketlabs.pulse.storage.model.news.MarketNews
import kotlinx.coroutines.flow.Flow

interface LocalNewsDataSource {

    fun getLatestNews(): Flow<MarketNews?>
    suspend fun saveNews(news: MarketNews)
    suspend fun clearNews()
    suspend fun getLastSyncedTimestamp(): Long?
    suspend fun updateLastSyncedTimestamp(timestamp: Long)
}