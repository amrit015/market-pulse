package com.marketlabs.pulse.network.api

import com.marketlabs.pulse.network.model.finnhub.QuoteResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface FinnHubService {

    @GET("quote")
    suspend fun getQuote(
        @Query("symbol") symbol: String
    ): QuoteResponse
}