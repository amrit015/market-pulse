package com.marketlabs.pulse.storage.store.news

import com.marketlabs.pulse.storage.database.dao.NewsDao
import com.marketlabs.pulse.storage.model.news.MarketNews
import com.marketlabs.pulse.storage.model.news.mappers.toDomain
import com.marketlabs.pulse.storage.model.news.mappers.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LocalNewsDataSourceImpl @Inject constructor(
    private val dao: NewsDao
) : LocalNewsDataSource {

    override fun getLatestNews(): Flow<MarketNews?> {
        return dao.getLatestNews().map { it?.toDomain() }
    }

    override suspend fun saveNews(news: MarketNews) {
        dao.insertNews(news.toEntity())
    }

    override suspend fun clearNews() {
        dao.clearNews()
    }

    /**
     * Retrieves the last synced timestamp. Returns null if news has not been saved yet.
     */
    override suspend fun getLastSyncedTimestamp(): Long? {
        return dao.getLastSyncedTimestamp()
    }

    /**
     * Updates the last synced timestamp in the local database.
     */
    override suspend fun updateLastSyncedTimestamp(timestamp: Long) {
        dao.updateLastSyncedTimestamp(timestamp)
    }
}