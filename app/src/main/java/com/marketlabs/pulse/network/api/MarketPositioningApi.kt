package com.marketlabs.pulse.network.api

import com.marketlabs.pulse.network.model.positioning.NetworkMarketPositioning
import retrofit2.http.GET

interface MarketPositioningApi {
    @GET("insights/positioning")
    suspend fun getMarketPositioning(): NetworkMarketPositioning
}
