package com.marketlabs.pulse.network.store.positioning

import android.util.Log
import com.marketlabs.pulse.network.api.MarketPositioningApi
import com.marketlabs.pulse.storage.model.positioning.DomainMarketPositioning
import com.marketlabs.pulse.storage.model.positioning.mappers.toDomain
import javax.inject.Inject

/**
 * 💡 As of 2026-08-26, `GET /insights/positioning` has never successfully returned data -- the
 * backend's scheduled engine (8pm ET weekdays) hasn't completed a run yet, and the route 404s with
 * `{"error":"No market positioning data found."}` (curl-verified). That 404 surfaces here as a
 * plain Retrofit `HttpException`, caught by the same generic try/catch every other domain's remote
 * data source already uses -- callers see it as an ordinary `Result.failure`, same as any other
 * network error, so no special "no data yet" handling was added. Re-verify this once the backend
 * has produced a real document.
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
