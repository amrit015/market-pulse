package com.marketlabs.pulse.storage.model.riskRadar

import com.marketlabs.pulse.utils.enums.RiskStatus
import com.marketlabs.pulse.utils.enums.RiskTrend

data class RiskRadar(
    val date: String,
    val lastSyncedTimestamp: Long,
    val lastUpdated: Long,
    val score: Int?,
    val previousScore: Int?,
    val trend: RiskTrend?,
    val status: RiskStatus?,
    val gauges: RiskGauges?
)

data class RiskGauges(
    val recession: Gauge?,
    val foundation: Gauge?,
    val rotation: Gauge?,
    val growthFear: Gauge?,
    val canary: Gauge?
)

data class Gauge(
    val value: Double?,
    val riskScore: Int?,
    val label: String?
)