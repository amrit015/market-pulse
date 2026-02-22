package com.marketlabs.pulse.network.store.news

import com.marketlabs.pulse.storage.model.news.MarketNews

interface RemoteNewsDataSource {

    suspend fun getLatestNews(): Result<MarketNews>
}