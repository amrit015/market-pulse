package com.marketlabs.pulse.core.intraday

import android.util.Log
import com.marketlabs.pulse.network.api.IntradayApi
import com.marketlabs.pulse.storage.model.intraday.IntradaySeries
import com.marketlabs.pulse.storage.model.intraday.mappers.toDomain
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
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
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

    override fun trackSymbol(symbol: String) {
        val refCount = (trackedRefCounts[symbol] ?: 0) + 1
        trackedRefCounts[symbol] = refCount
        if (refCount > 1) return // Already polling for another caller.

        pollJobs[symbol] = scope.launch {
            while (isActive) {
                fetchOnce(symbol)
                delay(POLL_INTERVAL_MS)
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
            val today = currentEasternDate()
            // The doc is reset, not appended, on a new trading day -- a response fetched before
            // today's first poll tick can be a prior day's leftover rather than an empty one, so
            // this is the freshness check the backend spec calls out, not a redundant one.
            val series = response.date
                ?.takeIf { it == today }
                ?.let { response.toDomain(symbol = symbol, date = it) }
            _seriesMap.update { it + (symbol to series) }
        } catch (e: HttpException) {
            if (e.code() == 404) {
                Log.d("Intraday", "No intraday data yet for $symbol")
                _seriesMap.update { it + (symbol to null) }
            } else {
                Log.e("Intraday", "Failed to fetch intraday for $symbol", e)
                // Transient server error -- leave whatever was last successfully fetched in place
                // rather than blanking a working sparkline over one bad poll.
            }
        } catch (e: Exception) {
            Log.e("Intraday", "Failed to fetch intraday for $symbol", e)
        }
    }

    private companion object {
        const val POLL_INTERVAL_MS = 30_000L
        val dateFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

        fun currentEasternDate(): String =
            ZonedDateTime.now(ZoneId.of("America/New_York")).toLocalDate().format(dateFormatter)
    }
}
