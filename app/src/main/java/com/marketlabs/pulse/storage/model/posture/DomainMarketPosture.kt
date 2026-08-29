package com.marketlabs.pulse.storage.model.posture

import com.marketlabs.pulse.utils.enums.DeltaDirection

data class DomainMarketPosture(
    val naaimExposure: DomainNaaimExposure?,
    val darkPoolIndex: DomainDarkPoolIndex?,
    val netLiquidity: DomainNetLiquidity?,
    val synthesis: DomainPostureSynthesis?,
    val timestamp: Long?
)

data class DomainNaaimExposure(
    val value: Double?,
    val status: String?,
    val description: String?,
    val lastObservation: DomainLastObservation? = null,
    val delta: Double? = null,
    val deltaDirection: DeltaDirection = DeltaDirection.UNKNOWN,
    val fetchedAt: Long? = null,
    val staleSince: Long? = null
)

data class DomainDarkPoolIndex(
    val value: Double?,
    val date: String?,
    val status: String?,
    val description: String?,
    val lastObservation: DomainLastObservation? = null,
    val delta: Double? = null,
    val deltaDirection: DeltaDirection = DeltaDirection.UNKNOWN,
    val fetchedAt: Long? = null,
    val staleSince: Long? = null
)

data class DomainNetLiquidity(
    val value: Double?,
    val status: String?,
    val assetsT: Double?,
    val tgaT: Double?,
    val rrpT: Double?,
    val date: String?,
    val description: String?,
    val lastObservation: DomainLastObservation? = null,
    val delta: Double? = null,
    val deltaDirection: DeltaDirection = DeltaDirection.UNKNOWN,
    val fetchedAt: Long? = null,
    val staleSince: Long? = null
)

// 💡 Shared shape for the "what was this gauge reading before it last changed" envelope --
// see NetworkLastObservation's doc comment (network/model/posture/NetworkMarketPosture.kt) for
// why the network-layer version of this object's own fields are non-null even though the object
// as a whole is always optional.
data class DomainLastObservation(
    val value: Double,
    val status: String,
    val observedAt: Long
)

// 💡 Named DomainPostureSynthesis (not a bare "DomainSynthesis") because Positioning's own
// synthesis lives in its own package as DomainPositioningSynthesis -- same field shape, kept as
// two small duplicated classes rather than one shared type, matching this app's per-domain
// vertical-slicing convention (see this file's network-layer counterpart's own doc comment).
data class DomainPostureSynthesis(
    val headline: String?,
    val detail: String?,
    val generatedAt: Long?,
    val contentFlags: List<String>,
    val state: String?
)