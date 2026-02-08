package com.marketlabs.pulse.network.api

import com.marketlabs.pulse.network.model.summary.NetworkMarketPulse
import retrofit2.http.GET
import retrofit2.http.Path

interface MarketPulseApi {

    // fetching the latest "Current" pulse
    @GET("daily_pulse/latest")
    suspend fun getLatestPulse(): NetworkMarketPulse

    // fetching by specific date (Optional)
    @GET("daily_pulse/{dateId}")
    suspend fun getPulseByDate(@Path("dateId") dateId: String): NetworkMarketPulse
}