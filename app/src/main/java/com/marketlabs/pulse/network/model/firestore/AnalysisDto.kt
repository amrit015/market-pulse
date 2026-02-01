package com.marketlabs.pulse.network.model.firestore

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AnalysisDto(
    val symbol: String = "",
    val rsi: Double = 0.0,
    @param:Json(name = "technical_status") val technicalStatus: String = ""
)