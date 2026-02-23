package com.marketlabs.pulse.network.store.summary

import com.marketlabs.pulse.storage.model.summary.MarketPulse

interface RemoteSummaryDataSource {
    suspend fun getLatestMarketPulse(): Result<MarketPulse>
    suspend fun getLatestDailyPulse(): Result<MarketPulse>
}