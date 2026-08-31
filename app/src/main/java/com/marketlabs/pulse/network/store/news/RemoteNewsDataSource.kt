package com.marketlabs.pulse.network.store.news

import com.marketlabs.pulse.storage.model.news.MarketNews
import com.marketlabs.pulse.storage.model.news.NewsArticle

interface RemoteNewsDataSource {

    suspend fun getLatestNews(): Result<MarketNews>

    /** Flattened stories from the last 2 archived days (see `NewsApi.getNewsHistory`). */
    suspend fun getNewsHistory(): Result<List<NewsArticle>>
}