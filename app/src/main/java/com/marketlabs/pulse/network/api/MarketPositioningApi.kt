package com.marketlabs.pulse.network.api

import com.marketlabs.pulse.network.model.insights.NetworkInsightsHistoryPoint
import com.marketlabs.pulse.network.model.positioning.NetworkMarketPositioning
import retrofit2.http.GET
import retrofit2.http.Query

interface MarketPositioningApi {
    @GET("insights/positioning")
    suspend fun getMarketPositioning(): NetworkMarketPositioning

    // See MarketPostureApi.getPostureHistory's identical doc comment.
    @GET("insights/positioning/history")
    suspend fun getPositioningHistory(
        @Query("metric") metric: String,
        @Query("limit") limit: Int? = null
    ): List<NetworkInsightsHistoryPoint>
}
