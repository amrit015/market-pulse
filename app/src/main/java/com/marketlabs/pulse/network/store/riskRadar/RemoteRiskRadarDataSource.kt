package com.marketlabs.pulse.network.store.riskRadar

import com.marketlabs.pulse.storage.model.riskRadar.RiskRadar

interface RemoteRiskRadarDataSource {

    suspend fun getLatestRisk(todayDateString: String): Result<RiskRadar>
}