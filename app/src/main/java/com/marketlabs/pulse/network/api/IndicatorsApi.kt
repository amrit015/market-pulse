package com.marketlabs.pulse.network.api

import com.marketlabs.pulse.network.model.indicators.NetworkAiSynthesis
import com.marketlabs.pulse.network.model.indicators.NetworkIndicatorPillar
import retrofit2.http.GET

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
}