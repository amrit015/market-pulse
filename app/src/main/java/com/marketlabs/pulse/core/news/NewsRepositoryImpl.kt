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
     *
     * Fetches today's stories (`/news/latest`) and the last 2 archived days
     * (`/news/history`) and merges them into one cached list, so the News tab has enough
     * context even on a quiet news day. The history fetch is treated as non-fatal -- a
     * broken/slow history call shouldn't block today's news from showing, so its failure
     * degrades to an empty list instead of failing the whole refresh.
     */
    override suspend fun refreshNews(force: Boolean): Result<Unit> {
        return try {
            Log.d("MarketNews", "🌐 Fetching latest news from Firebase...")

            val latestNews = remoteDataSource.getLatestNews().getOrElse {
                Log.e("MarketNews", "❌ Network fetch failed", it)
                throw it
            }

            val historyStories = remoteDataSource.getNewsHistory()
                .onFailure { Log.e("MarketNews", "⚠️ Failed to fetch news history (non-fatal)", it) }
                .getOrDefault(emptyList())

            val mergedNews = latestNews.copy(stories = latestNews.stories.orEmpty() + historyStories)
            localDataSource.saveNews(mergedNews)
            Log.d(
                "MarketNews",
                "💾 Successfully cached ${mergedNews.stories?.size} stories " +
                    "(${historyStories.size} from the last 2 days)."
            )

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