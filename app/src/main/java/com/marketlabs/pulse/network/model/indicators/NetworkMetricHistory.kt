package com.marketlabs.pulse.network.model.indicators

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * `GET /indicators/{pillar}/history?metric=<id>&limit=<N>` response -- a plain array, oldest
 * first, not wrapped in an envelope object. `[]` (not 404) means the metric id doesn't exist yet
 * or hasn't produced a value -- see `RemoteMetricHistoryDataSourceImpl`, which treats an empty
 * list as a normal "no data yet" result, not an error.
 *
 * `value` has no fixed unit (a raw percent for `cpi_yoy`, a raw ratio for `pe_ratio`, a raw index
 * level for `vix`, etc.) -- `valueDisplay` is the backend's own pre-formatted label (`"63.42%"`,
 * `"25.79x"`) and is what `IndicatorHistoryChart` actually shows on the marker/caption, not a
 * client-derived format guessed from the raw number.
 */
@JsonClass(generateAdapter = true)
data class NetworkMetricHistoryPoint(
    @Json(name = "date") val date: String?,
    @Json(name = "value") val value: Double?,
    @Json(name = "value_display") val valueDisplay: String?,
    @Json(name = "signal_color") val signalColor: String?
)
