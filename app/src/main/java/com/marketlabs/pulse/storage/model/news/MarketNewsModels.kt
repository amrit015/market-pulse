package com.marketlabs.pulse.storage.model.news

data class MarketNews(
    val lastSyncedTimestamp: Long,
    val sourceCount: Int? = null,
    val stories: List<NewsArticle>? = null
)

data class NewsArticle(
    val headline: String? = null,
    val url: String? = null,
    val source: String? = null,
    val timestamp: Long? = null,
    val sentiment: String? = null,
    val impactSummary: String? = null,
    val tags: List<String>? = null,
    val aiEnriched: Boolean? = null
)