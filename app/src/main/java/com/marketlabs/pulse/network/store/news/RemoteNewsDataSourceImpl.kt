package com.marketlabs.pulse.network.store.news

import android.util.Log
import com.marketlabs.pulse.network.api.NewsApi
import com.marketlabs.pulse.network.model.news.NetworkNewsArticle
import com.marketlabs.pulse.storage.model.news.MarketNews
import com.marketlabs.pulse.storage.model.news.NewsArticle
import javax.inject.Inject

// todo: move to mapper
private fun NetworkNewsArticle.toDomain(): NewsArticle {
    return NewsArticle(
        headline = headline,
        url = url,
        source = source,
        timestamp = timestamp,
        sentiment = sentiment,
        impactSummary = impactSummary,
        tags = tags,
        aiEnriched = aiEnriched
    )
}

class RemoteNewsDataSourceImpl @Inject constructor(
    private val api: NewsApi
) : RemoteNewsDataSource {

    override suspend fun getLatestNews(): Result<MarketNews> {
        return try {
            val response = api.getLatestNews()

            val domainNews = MarketNews(
                lastSyncedTimestamp = System.currentTimeMillis(),
                lastUpdated = response.lastUpdated ?: 0L,
                sourceCount = response.sourceCount,
                stories = response.stories?.map { it.toDomain() }
            )

            Result.success(domainNews)
        } catch (e: Exception) {
            Log.e("MarketNews", "Failed to fetch remote news", e)
            Result.failure(e)
        }
    }

    /**
     * The last 2 archived days, flattened into one story list. Each day in the response keeps
     * its own `stories[]`; there's no day-level context (like the archive date) worth carrying
     * into the domain layer, so this just concatenates them -- ordering across the merged full
     * list is restored later by NewsMappers' `sortedByDescending { it.timestamp }`.
     */
    override suspend fun getNewsHistory(): Result<List<NewsArticle>> {
        return try {
            val days = api.getNewsHistory()
            val stories = days.flatMap { day -> day.stories.orEmpty().map { it.toDomain() } }
            Result.success(stories)
        } catch (e: Exception) {
            Log.e("MarketNews", "Failed to fetch news history", e)
            Result.failure(e)
        }
    }
}