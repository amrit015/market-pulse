package com.marketlabs.pulse.network.model.finnhub

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// The Data Model for the API Response for FinnHubService
// Finnhub returns a JSON like: { "c": 150.23, "d": -1.2, "dp": -0.85 ... }
@JsonClass(generateAdapter = true)
data class QuoteResponse(
    @param:Json(name = "c") val currentPrice: Double,
    @param:Json(name = "d") val change: Double,
    @param:Json(name = "dp") val percentChange: Double
)