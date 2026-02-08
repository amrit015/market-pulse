package com.marketlabs.pulse.network.store.summary

import com.marketlabs.pulse.network.api.MarketPulseApi
import com.marketlabs.pulse.storage.model.summary.MarketPulse
import com.marketlabs.pulse.storage.model.summary.mappers.toDomain
import javax.inject.Inject

class RemoteMarketSummaryDataSourceImpl @Inject constructor(
    private val api: MarketPulseApi
) : RemoteMarketSummaryDataSource {

    override suspend fun getLatestMarketPulse(): Result<MarketPulse> {
        return try {
            // 1. Fetch raw data (NetworkMarketPulse)
            val networkResponse = api.getLatestPulse()

            // 2. Validate essential data (Optional but recommended)
            if (networkResponse.reportType == null) {
                return Result.failure(Exception("Invalid Data: Missing Report Type"))
            }

            // 3. Map to Domain Model (MarketPulse)
            // This triggers all your Enum conversions defined in Mappers.kt
            val domainData = networkResponse.toDomain()

            // 4. Return Success
            Result.success(domainData)

        } catch (e: Exception) {
            // 5. Catch Network/Parsing errors
            // (e.g., No Internet, 404, or Malformed JSON)
            Result.failure(e)
        }
    }
}