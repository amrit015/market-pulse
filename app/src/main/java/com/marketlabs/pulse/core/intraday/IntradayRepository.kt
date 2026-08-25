package com.marketlabs.pulse.core.intraday

import com.marketlabs.pulse.storage.model.intraday.IntradaySeries
import kotlinx.coroutines.flow.Flow

/**
 * Today's intraday bars, backend-polled -- replaces the old client-side Finnhub-WS-driven
 * sparkline. Deliberately skips the standard Remote/Local/Repository 5-layer scaffold
 * (`ARCHITECTURE.md §1` explicitly allows this when nothing needs Room caching): this data resets
 * every trading day server-side and is never meant to survive process death, so caching it
 * locally would be actively wrong, not just unnecessary.
 *
 * One unified endpoint backs both stocks (all of them, unconditionally) and the ~23-symbol
 * dashboard tile set (`DashboardIntradayEligibility` gates which dashboard symbols are worth
 * calling this for at all, since the rest are a guaranteed 404 -- stocks need no such gate).
 */
interface IntradayRepository {

    /**
     * Starts polling [symbol] on a ~30s interval while tracked. Ref-counted -- independent callers
     * (e.g. a stock's preview card and its own pushed Detail page) can track the same symbol at
     * once, and the poll only actually stops once every tracker has called [untrackSymbol].
     */
    fun trackSymbol(symbol: String)

    /** Releases this caller's hold on [symbol]. No-op if this caller was never tracking it. */
    fun untrackSymbol(symbol: String)

    /**
     * `null` means no usable data yet -- never polled (404), a transient fetch failure with
     * nothing cached yet, or a stale prior-day leftover doc (see the repository impl's freshness
     * check). Callers should render nothing beyond their own static baseline in that case, not a
     * synthesized flat line -- see `SparklineChart`'s doc comment.
     */
    fun getIntradayStream(symbol: String): Flow<IntradaySeries?>
}
