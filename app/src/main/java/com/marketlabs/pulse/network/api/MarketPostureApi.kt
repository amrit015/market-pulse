package com.marketlabs.pulse.network.api

import com.marketlabs.pulse.network.model.insights.NetworkInsightsHistoryPoint
import com.marketlabs.pulse.network.model.posture.NetworkMarketPosture
import retrofit2.http.GET
import retrofit2.http.Query

interface MarketPostureApi {
    @GET("insights/posture")
    suspend fun getMarketPosture(): NetworkMarketPosture

    // Per-metric history for the chart on the pushed glossary-detail page -- `limit` omitted lets
    // the backend use its own default; callers should always pass one explicitly (hard-capped at
    // 180 server-side), same reasoning as IndicatorsApi's own history routes.
    @GET("insights/posture/history")
    suspend fun getPostureHistory(
        @Query("metric") metric: String,
        @Query("limit") limit: Int? = null
    ): List<NetworkInsightsHistoryPoint>
}