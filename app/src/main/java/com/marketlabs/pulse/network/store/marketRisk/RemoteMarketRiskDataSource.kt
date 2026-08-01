package com.marketlabs.pulse.network.store.marketRisk

import com.marketlabs.pulse.storage.model.marketRisk.MarketRiskAssessment

interface RemoteMarketRiskDataSource {

    suspend fun getLatestTailRisks(todayDateString: String): Result<MarketRiskAssessment>
}