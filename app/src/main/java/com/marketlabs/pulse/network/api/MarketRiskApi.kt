package com.marketlabs.pulse.network.api

import com.marketlabs.pulse.network.model.marketRisk.NetworkMarketRiskAssessment
import retrofit2.http.GET

interface MarketRiskApi {

    @GET("risk/tail-risks")
    suspend fun getLatestTailRisks(): NetworkMarketRiskAssessment
}