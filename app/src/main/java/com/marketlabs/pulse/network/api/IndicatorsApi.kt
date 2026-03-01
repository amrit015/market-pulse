package com.marketlabs.pulse.network.api

import com.marketlabs.pulse.network.model.indicators.NetworkPhaseDetails
import com.marketlabs.pulse.network.model.indicators.NetworkPhaseSummary
import retrofit2.http.GET

interface IndicatorsApi {
    @GET("indicators/summary") // Update path based on your exact backend endpoint
    suspend fun getSummary(): NetworkPhaseSummary

    @GET("indicators/trend")
    suspend fun getTrend(): NetworkPhaseDetails

    @GET("indicators/health")
    suspend fun getHealth(): NetworkPhaseDetails

    @GET("indicators/risk")
    suspend fun getRisk(): NetworkPhaseDetails
}