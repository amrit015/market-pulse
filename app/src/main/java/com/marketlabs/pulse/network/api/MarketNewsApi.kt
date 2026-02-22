package com.marketlabs.pulse.network.api

import com.marketlabs.pulse.network.model.news.NetworkMarketNews
import retrofit2.http.GET

interface NewsApi {
    @GET("news/latest")
    suspend fun getLatestNews(): NetworkMarketNews
}