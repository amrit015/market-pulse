package com.marketlabs.pulse.network.api

import com.marketlabs.pulse.network.model.posture.NetworkMarketPosture
import retrofit2.http.GET

interface MarketPostureApi {
    @GET("insights/posture")
    suspend fun getMarketPosture(): NetworkMarketPosture
}