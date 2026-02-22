package com.marketlabs.pulse.network.api

import com.marketlabs.pulse.network.model.summary.NetworkMarketPulse
import retrofit2.http.GET
import retrofit2.http.Path

interface MarketPulseApi {

    // Fetches from the 'market_pulse' collection (Gemini 3.1)
    @GET("pulse/v3/latest") // Replace with your actual Express route
    suspend fun getLatestMarketPulse(): NetworkMarketPulse

    // Fetches from the 'daily_pulse' collection (Gemini 2.5)
    @GET("pulse/v2/latest") // Replace with your actual Express route
    suspend fun getLatestDailyPulse(): NetworkMarketPulse

    // fetching by specific date (Optional)
    @GET("daily_pulse/{dateId}")
    suspend fun getPulseByDate(@Path("dateId") dateId: String): NetworkMarketPulse
}