package com.marketlabs.pulse.network.store.news

import android.util.Log
import com.marketlabs.pulse.network.api.NewsApi
import com.marketlabs.pulse.storage.model.news.MarketNews
import com.marketlabs.pulse.storage.model.news.NewsArticle
import javax.inject.Inject

class RemoteNewsDataSourceImpl @Inject constructor(
    private val api: NewsApi
) : RemoteNewsDataSource {

    override suspend fun getLatestNews(): Result<MarketNews> {
        return try {
            val response = api.getLatestNews()

            // Map Network Model -> Domain Model (Cleaned up!)
            val domainNews = MarketNews(
                lastSyncedTimestamp = System.currentTimeMillis(),
                sourceCount = response.sourceCount,
                stories = response.stories?.map { story ->
                    NewsArticle(
                        headline = story.headline,
                        url = story.url,
                        source = story.source,
                        timestamp = story.timestamp,
                        sentiment = story.sentiment,
                        impactSummary = story.impactSummary,
                        tags = story.tags,
                        aiEnriched = story.aiEnriched
                    )
                }
            )

            Result.success(domainNews)
        } catch (e: Exception) {
            Log.e("MarketNews", "Failed to fetch remote news", e)
            Result.failure(e)
        }
    }
}