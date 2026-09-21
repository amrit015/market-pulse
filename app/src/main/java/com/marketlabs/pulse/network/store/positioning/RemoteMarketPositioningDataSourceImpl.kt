package com.marketlabs.pulse.network.store.positioning

import android.util.Log
import com.marketlabs.pulse.network.api.MarketPositioningApi
import com.marketlabs.pulse.storage.model.positioning.DomainMarketPositioning
import com.marketlabs.pulse.storage.model.positioning.mappers.toDomain
import javax.inject.Inject

/**
 * 💡 `GET /insights/positioning` 404s with `{"error":"No market positioning data found."}` until
 * the backend's scheduled engine has produced a document. That 404 surfaces here as a plain
 * Retrofit `HttpException`, caught by the same generic try/catch every other domain's remote
 * data source uses -- callers see it as an ordinary `Result.failure`, same as any other network
 * error, so there is no special "no data yet" handling.
 */
class RemoteMarketPositioningDataSourceImpl @Inject constructor(
    private val api: MarketPositioningApi
) : RemoteMarketPositioningDataSource {

    override suspend fun getLatestPositioning(): Result<DomainMarketPositioning> {
        return try {
            val response = api.getMarketPositioning()
            Result.success(response.toDomain())
        } catch (e: Exception) {
            Log.e("MarketPositioning", "Failed to fetch remote positioning", e)
            Result.failure(e)
        }
    }
}
