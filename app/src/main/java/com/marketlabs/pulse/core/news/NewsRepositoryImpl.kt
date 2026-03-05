package com.marketlabs.pulse.core.news

import android.util.Log
import com.marketlabs.pulse.network.store.news.RemoteNewsDataSource
import com.marketlabs.pulse.storage.model.news.MarketNews
import com.marketlabs.pulse.storage.store.news.LocalNewsDataSource
import com.marketlabs.pulse.utils.CachePolicy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class NewsRepositoryImpl @Inject constructor(
    private val localDataSource: LocalNewsDataSource,
    private val remoteDataSource: RemoteNewsDataSource
) : NewsRepository {

    override fun getNewsStream(): Flow<MarketNews?> = localDataSource.getLatestNews()

    override suspend fun refreshNews(force: Boolean): Result<Unit> {
        return try {
            val localData = localDataSource.getLatestNews().firstOrNull()

            // caching logic
            val shouldFetch = when {
                force -> true
                localData?.lastSyncedTimestamp == null -> true // Database is completely empty
                else -> CachePolicy.isQuarterHourExpired(localData.lastSyncedTimestamp)
            }

            if (!shouldFetch) {
                Log.d("MarketNews", "✅ Cache is fresh (Current 15-min block). Skipping network call.")
                return Result.success(Unit)
            }

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
}