package com.marketlabs.pulse.network.model.posture

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NetworkMarketPosture(
    @Json(name = "naaim_exposure") val naaimExposure: NetworkNaaimExposure? = null,
    @Json(name = "dark_pool_index") val darkPoolIndex: NetworkDarkPoolIndex? = null,
    @Json(name = "net_liquidity") val netLiquidity: NetworkNetLiquidity? = null,
    @Json(name = "timestamp") val timestamp: Long? = null
)

@JsonClass(generateAdapter = true)
data class NetworkNaaimExposure(
    @Json(name = "value") val value: Double? = null,
    @Json(name = "status") val status: String? = null,
    @Json(name = "description") val description: String? = null
)

@JsonClass(generateAdapter = true)
data class NetworkDarkPoolIndex(
    @Json(name = "value") val value: Double? = null,
    @Json(name = "date") val date: String? = null,
    @Json(name = "status") val status: String? = null,
    @Json(name = "description") val description: String? = null
)

@JsonClass(generateAdapter = true)
data class NetworkNetLiquidity(
    @Json(name = "value") val value: Double? = null,
    @Json(name = "status") val status: String? = null,
    @Json(name = "assets_t") val assetsT: Double? = null,
    @Json(name = "tga_t") val tgaT: Double? = null,
    @Json(name = "rrp_t") val rrpT: Double? = null,
    @Json(name = "date") val date: String? = null,
    @Json(name = "description") val description: String? = null
)