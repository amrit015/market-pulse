package com.marketlabs.pulse.network.api

import com.marketlabs.pulse.network.model.news.NetworkMarketNews
import com.marketlabs.pulse.network.model.news.NetworkNewsHistoryDay
import retrofit2.http.GET

interface NewsApi {
    @GET("news/latest")
    suspend fun getLatestNews(): NetworkMarketNews

    // No `limit` query param sent -- the backend route defaults to the last 2 archived
    // (non-"latest") days when it's omitted, which is exactly the "last 2 days" window this
    // app wants, so there's no reason to duplicate that constant on the client.
    @GET("news/history")
    suspend fun getNewsHistory(): List<NetworkNewsHistoryDay>
}