package com.marketlabs.pulse.network.api

import com.marketlabs.pulse.network.model.indicators.NetworkMacroVitals
import com.marketlabs.pulse.network.model.indicators.NetworkMarketAction
import com.marketlabs.pulse.network.model.indicators.NetworkMarketPhase
import retrofit2.http.GET

interface IndicatorsApi {
    @GET("indicators/phase")
    suspend fun getMarketPhase(): NetworkMarketPhase

    @GET("indicators/vitals")
    suspend fun getMacroVitals(): NetworkMacroVitals

    @GET("indicators/action")
    suspend fun getMarketAction(): NetworkMarketAction
}