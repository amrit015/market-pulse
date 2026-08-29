package com.marketlabs.pulse.network.api

import com.marketlabs.pulse.network.model.indicators.NetworkAiSynthesis
import com.marketlabs.pulse.network.model.indicators.NetworkIndicatorPillar
import com.marketlabs.pulse.network.model.indicators.NetworkMetricHistoryPoint
import retrofit2.http.GET
import retrofit2.http.Query

interface IndicatorsApi {
    @GET("indicators/synthesis")
    suspend fun getAiSynthesis(): NetworkAiSynthesis

    @GET("indicators/tactical")
    suspend fun getTacticalMomentum(): NetworkIndicatorPillar

    @GET("risk/latest")
    suspend fun getSystemicRisk(): NetworkIndicatorPillar

    @GET("indicators/valuation")
    suspend fun getValuation(): NetworkIndicatorPillar

    @GET("indicators/vitals")
    suspend fun getMacroVitals(): NetworkIndicatorPillar

    // Per-metric history for the charts on the pushed metric-detail page -- one endpoint per
    // pillar, routed via MetricHistoryPillar.forMetricId(metricId) since the pillar isn't part
    // of the id itself. `limit` omitted lets the backend use its own per-route default.
    @GET("indicators/tactical/history")
    suspend fun getTacticalMomentumHistory(
        @Query("metric") metric: String,
        @Query("limit") limit: Int? = null
    ): List<NetworkMetricHistoryPoint>

    @GET("risk/history")
    suspend fun getSystemicRiskHistory(
        @Query("metric") metric: String,
        @Query("limit") limit: Int? = null
    ): List<NetworkMetricHistoryPoint>

    @GET("indicators/valuation/history")
    suspend fun getValuationHistory(
        @Query("metric") metric: String,
        @Query("limit") limit: Int? = null
    ): List<NetworkMetricHistoryPoint>

    @GET("indicators/vitals/history")
    suspend fun getMacroVitalsHistory(
        @Query("metric") metric: String,
        @Query("limit") limit: Int? = null
    ): List<NetworkMetricHistoryPoint>
}