package com.marketlabs.pulse.network.store.summary

import android.util.Log
import com.marketlabs.pulse.network.api.MarketPulseApi
import com.marketlabs.pulse.storage.model.summary.MarketPulse
import com.marketlabs.pulse.storage.model.summary.mappers.toDomain
import retrofit2.HttpException
import javax.inject.Inject

class RemoteSummaryDataSourceImpl @Inject constructor(
    private val api: MarketPulseApi
) : RemoteSummaryDataSource {

    // ============================================================================
    // V3 (Gemini 3.x Pro - Main Content from 'market_pulse')
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

    // Calendar strip -- a specific past date. `market_position`/`whatChanged`/`whatsNew` on the
    // returned MarketPulse are composed by the backend at request time regardless of `dateId`
    // (see api/marketPulse.ts's marketPulseComposer.ts in the backend repo) -- always today's live
    // values, never historically accurate to the requested date. The Summary screen only renders
    // those three for today's selection for that reason; every other field is written into that
    // date's own doc at generation time and is genuinely historical.
    override suspend fun getMarketPulseByDate(dateId: String): Result<MarketPulse?> {
        return try {
            val networkResponse = api.getPulseByDate(dateId)

            if (networkResponse.reportType == null) {
                return Result.failure(Exception("Invalid Data: Missing Report Type"))
            }

            Result.success(networkResponse.toDomain())
        } catch (e: HttpException) {
            if (e.code() == 404) {
                Log.d("MarketPulse", "No report for $dateId")
                Result.success(null)
            } else {
                Log.e("MarketPulse", "Failed to fetch pulse for $dateId", e)
                Result.failure(e)
            }
        } catch (e: Exception) {
            Log.e("MarketPulse", "Failed to fetch pulse for $dateId", e)
            Result.failure(e)
        }
    }
}