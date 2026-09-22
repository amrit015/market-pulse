package com.marketlabs.pulse.network.api

import com.marketlabs.pulse.network.model.intraday.NetworkIntradayResponse
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * `GET /intraday/:symbol` — one unified endpoint for both the stock sparkline and the
 * ~23-symbol dashboard tile sparkline; the backend scopes which symbols actually have
 * data (two different pollers feed the same collection), the client just calls this the same way
 * for either. `/stocks/:symbol/intraday` (the older route) is intentionally not modeled — this is
 * the current endpoint for new code.
 */
interface IntradayApi {
    @GET("intraday/{symbol}")
    suspend fun getIntraday(@Path("symbol") symbol: String): NetworkIntradayResponse
}
