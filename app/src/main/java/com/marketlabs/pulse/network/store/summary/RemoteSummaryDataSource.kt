package com.marketlabs.pulse.network.store.summary

import com.marketlabs.pulse.storage.model.summary.MarketPulse

interface RemoteSummaryDataSource {
    suspend fun getLatestMarketPulse(): Result<MarketPulse>

    /**
     * A specific past date's report. A 404 (no report exists for that date -- weekend/holiday/
     * before the app tracked history) is a normal, expected state, not an error: it resolves as
     * `Result.success(null)`, same convention `RemoteStockDataSourceImpl.getStockDeepDive` already
     * uses for its own cold-start 404. Any other failure is a real error (`Result.failure`), left
     * alone by the caller rather than treated as a confirmed-empty date.
     */
    suspend fun getMarketPulseByDate(dateId: String): Result<MarketPulse?>
}