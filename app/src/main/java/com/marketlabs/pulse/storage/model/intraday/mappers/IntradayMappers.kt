package com.marketlabs.pulse.storage.model.intraday.mappers

import com.marketlabs.pulse.network.model.intraday.NetworkIntradayBar
import com.marketlabs.pulse.network.model.intraday.NetworkIntradayResponse
import com.marketlabs.pulse.storage.model.intraday.IntradayPoint
import com.marketlabs.pulse.storage.model.intraday.IntradaySeries

/**
 * `symbol`/`date` are trusted from the caller (the requested symbol, and "today" as ET) rather
 * than the response body, mirroring `RemoteStockDataSourceImpl`'s "the caller already knows what
 * it asked for" reasoning -- this mapper is only ever called after the repository has already
 * confirmed the response's own `date` matches today, so by the time this runs the distinction is
 * moot, but it keeps the domain model's `date` field unambiguous either way.
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
