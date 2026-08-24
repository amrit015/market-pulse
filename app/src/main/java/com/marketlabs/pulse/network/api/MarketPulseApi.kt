package com.marketlabs.pulse.network.api

import com.marketlabs.pulse.network.model.summary.NetworkMarketPulse
import retrofit2.http.GET
import retrofit2.http.Path

interface MarketPulseApi {

    // Fetches from the 'market_pulse' collection (Gemini 3.x)
    @GET("pulse/v3/latest") // Replace with your actual Express route
    suspend fun getLatestMarketPulse(): NetworkMarketPulse

    // fetching by specific date (Optional)
    @GET("pulse/v3/{dateId}")
    suspend fun getPulseByDate(@Path("dateId") dateId: String): NetworkMarketPulse
}