package com.marketlabs.pulse.core.intraday

import android.util.Log
import com.marketlabs.pulse.network.api.IntradayApi
import com.marketlabs.pulse.storage.model.intraday.IntradaySeries
import com.marketlabs.pulse.storage.model.intraday.mappers.toDomain
import com.marketlabs.pulse.utils.enums.AssetType
import com.marketlabs.pulse.utils.marketZone
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IntradayRepositoryImpl @Inject constructor(
    private val api: IntradayApi
) : IntradayRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val pollJobs = mutableMapOf<String, Job>()
    // Ref-counted rather than a plain tracked-set: a symbol can now be tracked by more than one
    // independent caller at once (e.g. a stock's Analysis-tab preview card and its own pushed
    // Detail page's 1D chart, both open at the same time), and an untrack from one shouldn't kill
    // the poll another caller still needs.
    private val trackedRefCounts = mutableMapOf<String, Int>()
    private val _seriesMap = MutableStateFlow<Map<String, IntradaySeries?>>(emptyMap())

    override fun trackSymbol(symbol: String, assetType: AssetType, pollIntervalMs: Long) {
        val refCount = (trackedRefCounts[symbol] ?: 0) + 1
        trackedRefCounts[symbol] = refCount
        if (refCount > 1) return // Already polling for another caller.

        pollJobs[symbol] = scope.launch {
            while (isActive) {
                // Bars for this asset class only change while its own market is open -- polling a
                // closed market every 30s is pure waste (same principle as `ChartsRepositoryImpl`'s
                // market-hours-closed cache check). The loop itself keeps its normal cadence so a
                // market that just opened is picked up within one tick, it just skips the network
                // call while closed --
                //
                // EXCEPT when nothing has been fetched for this symbol yet this process lifetime
                // (`containsKey`, not `[symbol] != null` -- a confirmed no-feed-ever 404 stores an
                // explicit null value, which must still count as "already tried"). Skipping the very
                // first fetch just because the market happens to be closed would mean a cold app
                // launch during closed hours never hydrates the last completed session's bars at
                // all -- the sparkline/1D chart would show nothing instead of that frozen last
                // session until the next open, instead of the intended "keep showing the last real
                // session until the market reopens" behavior `fetchOnce`'s own doc comment below
                // describes. One unconditional fetch bootstraps that, then the gate takes over.
                val hasFetchedBefore = _seriesMap.value.containsKey(symbol)
                if (isMarketOpenFor(assetType) || !hasFetchedBefore) {
                    fetchOnce(symbol)
                }
                delay(pollIntervalMs)
            }
        }
    }

    override fun untrackSymbol(symbol: String) {
        val refCount = (trackedRefCounts[symbol] ?: return) - 1
        if (refCount > 0) {
            trackedRefCounts[symbol] = refCount
            return
        }
        trackedRefCounts.remove(symbol)
        pollJobs.remove(symbol)?.cancel()
    }

    override fun getIntradayStream(symbol: String): Flow<IntradaySeries?> =
        _seriesMap.map { it[symbol] }

    private suspend fun fetchOnce(symbol: String) {
        try {
            val response = api.getIntraday(symbol)
            val date = response.date
            // The doc is reset, not appended, on a new trading day, so a response fetched before
            // today's first bar exists can carry a prior trading day's date -- outside regular
            // trading hours (pre-9:30am ET, or any time over a weekend/holiday) that's the normal
            // case, not a stale/broken one. That response is still the last real, completed
            // session's chart, and is trusted as-is (under its own real date, not relabeled as
            // "today") rather than discarded -- the whole point of polling is to keep showing that
            // real chart until a response actually dated today arrives, not to blank it out from
            // under whatever was last real data.
            if (date != null) {
                _seriesMap.update { it + (symbol to response.toDomain(symbol = symbol, date = date)) }
            }
        } catch (e: HttpException) {
            if (e.code() == 404) {
                Log.d("Intraday", "No intraday data yet for $symbol")
                // Distinct from the stale-date case above -- this means no doc has ever existed
                // for this symbol, not "not today's doc yet." Still only blanks the series if
                // nothing is already cached, so a transient 404 (e.g. a brief backend hiccup)
                // right after a real fetch doesn't wipe out a chart that was already showing.
                _seriesMap.update { current -> if (current[symbol] != null) current else current + (symbol to null) }
            } else {
                Log.e("Intraday", "Failed to fetch intraday for $symbol", e)
                // Transient server error -- leave whatever was last successfully fetched in place
                // rather than blanking a working sparkline over one bad poll.
            }
        } catch (e: Exception) {
            Log.e("Intraday", "Failed to fetch intraday for $symbol", e)
        }
    }

    /**
     * Per-asset-class schedule, mirroring the real backend windows (`dashboardEngine.ts`'s
     * `getMarketStatus()`): equities/sectors/indices Mon-Fri 9:30am-4:00pm ET; futures/commodities
     * Sun 6pm ET through Fri 5pm ET (CME Globex) -- the short Mon-Thu 5-6pm ET daily maintenance
     * halt is deliberately not modeled, same simplification as the chart cache's cutoff, too short
     * to be worth the extra branch; crypto is always open, 24/7; sentiment/unknown default to
     * "always poll" since neither has a well-defined closed window worth risking a missed update
     * over. Not trading-calendar-aware (holidays) on purpose -- worst case this polls once when it
     * didn't strictly need to, it never skips a poll that should have happened.
     */
    private fun isMarketOpenFor(assetType: AssetType): Boolean = when (assetType) {
        AssetType.EQUITY, AssetType.SECTOR, AssetType.INDEX -> isRegularEquityHoursNow()
        AssetType.FUTURE, AssetType.COMMODITY -> isFuturesHoursNow()
        AssetType.CRYPTO, AssetType.SENTIMENT, AssetType.UNKNOWN -> true
    }

    private fun isRegularEquityHoursNow(): Boolean {
        val now = ZonedDateTime.now(marketZone)
        if (now.dayOfWeek == DayOfWeek.SATURDAY || now.dayOfWeek == DayOfWeek.SUNDAY) return false
        val open = now.toLocalTime() >= LocalTime.of(9, 30)
        val beforeClose = now.toLocalTime() < LocalTime.of(16, 0)
        return open && beforeClose
    }

    private fun isFuturesHoursNow(): Boolean {
        val now = ZonedDateTime.now(marketZone)
        return when (now.dayOfWeek) {
            DayOfWeek.SATURDAY -> false
            DayOfWeek.SUNDAY -> now.toLocalTime() >= LocalTime.of(18, 0)
            DayOfWeek.FRIDAY -> now.toLocalTime() < LocalTime.of(17, 0)
            else -> true
        }
    }

}
