package com.marketlabs.pulse.network.api

import com.marketlabs.pulse.network.model.charts.NetworkChartResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * `GET /charts/:symbol` — works for any symbol the backend tracks (stocks and every dashboard
 * asset class alike), case-insensitive server-side. `days`/`range` are mutually exclusive
 * server-side (`range` wins if both are somehow passed); callers should only ever pass one.
 * `/stocks/:symbol/chart` (an older route kept only for backward compatibility) is intentionally
 * not modeled here — this is the current endpoint for new code.
 */
interface ChartsApi {
    @GET("charts/{symbol}")
    suspend fun getChart(
        @Path("symbol") symbol: String,
        @Query("days") days: Int? = null,
        @Query("range") range: String? = null
    ): NetworkChartResponse
}
