package com.marketlabs.pulse.network.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// The Data Model for the API Response for FinnHubService
// Finnhub returns a JSON like: { "c": 150.23, "d": -1.2, "dp": -0.85 ... }
@JsonClass(generateAdapter = true)
data class QuoteResponse(
    @Json(name = "c") val currentPrice: Double,
    @Json(name = "d") val change: Double,
    @Json(name = "dp") val percentChange: Double
)
