package com.marketlabs.pulse.network.api

import com.marketlabs.pulse.network.model.riskRadar.NetworkRiskRadar
import retrofit2.http.GET

interface RiskRadarApi {

    @GET("risk/latest")
    suspend fun getLatestRisk(): NetworkRiskRadar
}