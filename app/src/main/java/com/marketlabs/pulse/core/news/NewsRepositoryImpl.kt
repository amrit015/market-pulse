package com.marketlabs.pulse.core.news

import android.util.Log
import com.marketlabs.pulse.network.store.news.RemoteNewsDataSource
import com.marketlabs.pulse.storage.model.news.MarketNews
import com.marketlabs.pulse.storage.store.news.LocalNewsDataSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class NewsRepositoryImpl @Inject constructor(
    private val localDataSource: LocalNewsDataSource,
    private val remoteDataSource: RemoteNewsDataSource
) : NewsRepository {

    override fun getNewsStream(): Flow<MarketNews?> = localDataSource.getLatestNews()

    /**
     * Refreshes the news from the network.
     * Note: Cache expiration logic has been removed. This method is now strictly
     * driven by the SyncManager or explicit user pull-to-refresh actions.
     */
    override suspend fun refreshNews(force: Boolean): Result<Unit> {
        return try {
            Log.d("MarketNews", "🌐 Fetching latest news from Firebase...")

            remoteDataSource.getLatestNews().onSuccess { freshNews ->
                localDataSource.saveNews(freshNews)
                Log.d("MarketNews", "💾 Successfully cached ${freshNews.stories?.size} stories.")
            }.onFailure {
                Log.e("MarketNews", "❌ Network fetch failed", it)
                throw it
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("MarketNews", "Failed to refresh news repository", e)
            Result.failure(e)
        }
    }

    /**
     * Retrieves the timestamp of the last successful sync from the local cache.
     */
    override suspend fun getLastSyncedTimestamp(): Long? {
        return localDataSource.getLastSyncedTimestamp()
    }

    /**
     * Updates the local cache with the latest sync timestamp.
     */
    override suspend fun updateLastSyncedTimestamp(timestamp: Long) {
        localDataSource.updateLastSyncedTimestamp(timestamp)
    }
}