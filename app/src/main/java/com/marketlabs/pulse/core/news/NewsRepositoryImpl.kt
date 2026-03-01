package com.marketlabs.pulse.core.news

import android.util.Log
import com.marketlabs.pulse.network.store.news.RemoteNewsDataSource
import com.marketlabs.pulse.storage.model.news.MarketNews
import com.marketlabs.pulse.storage.store.news.LocalNewsDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class NewsRepositoryImpl @Inject constructor(
    private val localDataSource: LocalNewsDataSource,
    private val remoteDataSource: RemoteNewsDataSource
) : NewsRepository {

    // 1. Expose the Flow directly from the local Room database
    // The UI will collect this and automatically update whenever the DB changes.
    override fun getNewsStream(): Flow<MarketNews?> = localDataSource.getLatestNews()

    // 2. The Smart Fetch Logic
    override suspend fun refreshNews(force: Boolean): Result<Unit> {
        return try {
            val localData = localDataSource.getLatestNews().firstOrNull()
            val currentTime = System.currentTimeMillis()

            // Determine if a network request is actually needed
            val shouldFetch = when {
                force -> true // Rule 1: User explicitly pulled to refresh
                localData == null -> true // Rule 2: Database is completely empty
                else -> {
                    // Rule 3: The 15-Minute Window
                    // The Firebase newsEngine updates every 15 minutes.
                    val timeSinceLastSync = currentTime - localData.lastSyncedTimestamp
                    val fifteenMinutesInMillis = 15 * 60 * 1000

                    timeSinceLastSync > fifteenMinutesInMillis
                }
            }

            if (!shouldFetch) {
                Log.d("MarketNews", "✅ Cache is fresh (< 15 mins). Skipping network call.")
                return Result.success(Unit)
            }

            Log.d("MarketNews", "🌐 Fetching latest news from Firebase...")

            remoteDataSource.getLatestNews().onSuccess { freshNews ->
                // Save to Room. The UI is observing the Flow and will update instantly.
                // Note: The `toEntity()` mapper we wrote automatically sets the new sync time to System.currentTimeMillis()
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