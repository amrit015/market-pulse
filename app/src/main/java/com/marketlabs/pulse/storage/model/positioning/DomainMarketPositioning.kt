package com.marketlabs.pulse.storage.model.positioning

import com.marketlabs.pulse.utils.enums.DeltaDirection

data class DomainMarketPositioning(
    val retailSentiment: DomainRetailSentiment?,
    val institutionalPositioning: DomainInstitutionalPositioning?,
    val shortInterest: DomainShortInterest?,
    val synthesis: DomainPositioningSynthesis?,
    val timestamp: Long?
)

data class DomainRetailSentiment(
    val bullPct: Double?,
    val bearPct: Double?,
    val neutralPct: Double?,
    val bullBearSpread: Double?,
    val status: String?,
    val reportedDate: String?,
    val description: String?,
    val lastObservation: DomainLastObservation? = null,
    val delta: Double? = null,
    val deltaDirection: DeltaDirection = DeltaDirection.UNKNOWN,
    val fetchedAt: Long? = null,
    val staleSince: Long? = null
)

data class DomainInstitutionalPositioning(
    val es: DomainFuturesContract?,
    val nq: DomainFuturesContract?,
    val rty: DomainFuturesContract?,
    val dia: DomainFuturesContract?,
    val description: String?,
    val fetchedAt: Long?,
    val staleSince: Long?
)

// 💡 `methodology` (2026-08-27): "legacy_non_commercial" (es/nq/rty) vs. "tff_leveraged_funds"
// (dia) -- see NetworkFuturesContract's identical doc comment for why dia isn't directly
// comparable to the other three.
data class DomainFuturesContract(
    val ncNetPctOi: Double,
    val ncNetContracts: Long,
    val status: String,
    val percentile: Int,
    val reportDate: String,
    val methodology: String,
    val lastObservation: DomainLastObservation? = null,
    val delta: Double? = null,
    val deltaDirection: DeltaDirection = DeltaDirection.UNKNOWN
)

data class DomainShortInterest(
    val spy: DomainShortInterestInstrument?,
    val qqq: DomainShortInterestInstrument?,
    val iwm: DomainShortInterestInstrument?,
    val dia: DomainShortInterestInstrument?,
    val rsp: DomainShortInterestInstrument?,
    val mags: DomainShortInterestInstrument?,
    val description: String?,
    val fetchedAt: Long?,
    val staleSince: Long?
)

data class DomainShortInterestInstrument(
    val shortShares: Long,
    val daysToCover: Double,
    val momChangePct: Double,
    val settlementDate: String,
    val status: String,
    val lastObservation: DomainLastObservation? = null,
    val delta: Double? = null,
    val deltaDirection: DeltaDirection = DeltaDirection.UNKNOWN
)

// 💡 Duplicated from storage/model/posture/DomainMarketPosture.kt -- see that file's identical
// class for the vertical-slicing rationale.
data class DomainLastObservation(
    val value: Double,
    val status: String,
    val observedAt: Long
)

// 💡 Named DomainPositioningSynthesis, mirroring Posture's DomainPostureSynthesis -- same field
// shape, kept as two small duplicated classes rather than one shared type.
data class DomainPositioningSynthesis(
    val headline: String?,
    val detail: String?,
    val generatedAt: Long?,
    val contentFlags: List<String>,
    val state: String?
)
