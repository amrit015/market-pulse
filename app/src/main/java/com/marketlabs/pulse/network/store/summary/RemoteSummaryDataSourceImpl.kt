package com.marketlabs.pulse.network.store.summary

import android.util.Log
import com.marketlabs.pulse.network.api.MarketPulseApi
import com.marketlabs.pulse.storage.model.summary.MarketPulse
import com.marketlabs.pulse.storage.model.summary.mappers.toDomain
import javax.inject.Inject

class RemoteSummaryDataSourceImpl @Inject constructor(
    private val api: MarketPulseApi
) : RemoteSummaryDataSource {

    // ============================================================================
    // V3 (Gemini 3.1 Pro - Main Content from 'market_pulse')
    // ============================================================================
    override suspend fun getLatestMarketPulse(): Result<MarketPulse> {
        return try {
            // NOTE: Ensure your MarketPulseApi has this updated method name
            val networkResponse = api.getLatestMarketPulse()

            if (networkResponse.reportType == null) {
                return Result.failure(Exception("Invalid Data: Missing Report Type"))
            }

            Result.success(networkResponse.toDomain())

        } catch (e: Exception) {
            Log.e("MarketPulse", "Failed to fetch V3 Data", e)
            Result.failure(e)
        }
    }

    // ============================================================================
    // V2.5 (Gemini 2.5 Pro - Banner Content from 'daily_pulse')
    // ============================================================================
    override suspend fun getLatestDailyPulse(): Result<MarketPulse> {
        return try {
            // NOTE: Ensure your MarketPulseApi has this new method
            val networkResponse = api.getLatestDailyPulse()

            if (networkResponse.reportType == null) {
                return Result.failure(Exception("Invalid Data: Missing Report Type"))
            }

            Result.success(networkResponse.toDomain())

        } catch (e: Exception) {
            Log.e("DailyPulse", "Failed to fetch V2.5 Data", e)
            Result.failure(e)
        }
    }
}