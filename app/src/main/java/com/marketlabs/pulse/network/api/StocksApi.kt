package com.marketlabs.pulse.network.api

import com.marketlabs.pulse.network.model.stocks.NetworkStockDeepDive
import com.marketlabs.pulse.network.model.stocks.NetworkStockDetail
import com.marketlabs.pulse.network.model.stocks.NetworkStockPreviewsResponse
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * 💡 THOUGHT PROCESS:
 * There is no tracked-symbols call: `getStockPreviews()` already returns every analyzed symbol
 * with `symbol` embedded in one call, so there's no tracked-list-then-N-fetches pattern.
 */
interface StocksApi {
    @GET("stocks/previews")
    suspend fun getStockPreviews(): NetworkStockPreviewsResponse

    @GET("stocks/{symbol}/detail")
    suspend fun getStockDetail(@Path("symbol") symbol: String): NetworkStockDetail

    /** 404s until this symbol's first-ever deep dive has run — see `RemoteStockDataSourceImpl.getStockDeepDive`. */
    @GET("stocks/{symbol}/detail/deep")
    suspend fun getStockDeepDive(@Path("symbol") symbol: String): NetworkStockDeepDive
}
