package com.marketlabs.pulse.storage.model.posture

data class DomainMarketPosture(
    val naaimExposure: DomainNaaimExposure?,
    val darkPoolIndex: DomainDarkPoolIndex?,
    val netLiquidity: DomainNetLiquidity?,
    val timestamp: Long?
)

data class DomainNaaimExposure(
    val value: Double?,
    val status: String?,
    val description: String?
)

data class DomainDarkPoolIndex(
    val value: Double?,
    val date: String?,
    val status: String?,
    val description: String?
)

data class DomainNetLiquidity(
    val value: Double?,
    val status: String?,
    val assetsT: Double?,
    val tgaT: Double?,
    val rrpT: Double?,
    val date: String?,
    val description: String?
)