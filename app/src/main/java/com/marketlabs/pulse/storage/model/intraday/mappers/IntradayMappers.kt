package com.marketlabs.pulse.storage.model.intraday.mappers

import com.marketlabs.pulse.network.model.intraday.NetworkIntradayBar
import com.marketlabs.pulse.network.model.intraday.NetworkIntradayResponse
import com.marketlabs.pulse.storage.model.intraday.IntradayPoint
import com.marketlabs.pulse.storage.model.intraday.IntradaySeries

/**
 * `symbol` is trusted from the caller (the requested symbol) rather than the response body,
 * mirroring `RemoteStockDataSourceImpl`'s "the caller already knows what it asked for" reasoning.
 * `date` is the response's own `date` field, passed in explicitly rather than re-read from
 * `this.date` here -- `IntradayRepositoryImpl.fetchOnce` needs that value non-null before it even
 * decides whether to call this mapper at all, and the resulting series is *not* assumed to be
 * "today"'s: a response dated before today's session has started (pre-9:30am ET, weekends,
 * holidays) is the last completed session's real data, and is mapped and cached under its own
 * real date rather than being discarded.
 */
fun NetworkIntradayResponse.toDomain(symbol: String, date: String): IntradaySeries {
    return IntradaySeries(
        symbol = symbol,
        date = date,
        previousClose = prevClose,
        points = bars.orEmpty().mapNotNull { it.toDomain() }
    )
}

/** `"09:35"` -> minutes since midnight (575). Drops a bar with an unparseable `t` or a missing `price`. */
private fun NetworkIntradayBar.toDomain(): IntradayPoint? {
    val time = t ?: return null
    val resolvedPrice = price ?: return null
    val parts = time.split(":")
    if (parts.size != 2) return null
    val hours = parts[0].toIntOrNull() ?: return null
    val minutes = parts[1].toIntOrNull() ?: return null
    return IntradayPoint(minutesSinceMidnightEt = hours * 60 + minutes, price = resolvedPrice)
}
