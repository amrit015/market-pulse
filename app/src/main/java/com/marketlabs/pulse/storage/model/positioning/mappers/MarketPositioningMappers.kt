package com.marketlabs.pulse.storage.model.positioning.mappers

import com.marketlabs.pulse.network.model.positioning.NetworkFuturesContract
import com.marketlabs.pulse.network.model.positioning.NetworkInstitutionalPositioning
import com.marketlabs.pulse.network.model.positioning.NetworkLastObservation
import com.marketlabs.pulse.network.model.positioning.NetworkMarketPositioning
import com.marketlabs.pulse.network.model.positioning.NetworkRetailSentiment
import com.marketlabs.pulse.network.model.positioning.NetworkShortInterest
import com.marketlabs.pulse.network.model.positioning.NetworkShortInterestInstrument
import com.marketlabs.pulse.network.model.positioning.NetworkSynthesis
import com.marketlabs.pulse.storage.database.entity.MarketPositioningEntity
import com.marketlabs.pulse.storage.model.positioning.DomainFuturesContract
import com.marketlabs.pulse.storage.model.positioning.DomainInstitutionalPositioning
import com.marketlabs.pulse.storage.model.positioning.DomainLastObservation
import com.marketlabs.pulse.storage.model.positioning.DomainMarketPositioning
import com.marketlabs.pulse.storage.model.positioning.DomainPositioningSynthesis
import com.marketlabs.pulse.storage.model.positioning.DomainRetailSentiment
import com.marketlabs.pulse.storage.model.positioning.DomainShortInterest
import com.marketlabs.pulse.storage.model.positioning.DomainShortInterestInstrument
import com.marketlabs.pulse.utils.enums.DeltaDirection

fun NetworkMarketPositioning.toDomain(): DomainMarketPositioning {
    return DomainMarketPositioning(
        retailSentiment = retailSentiment?.toDomain(),
        institutionalPositioning = institutionalPositioning?.toDomain(),
        shortInterest = shortInterest?.toDomain(),
        synthesis = synthesis?.toDomain(),
        timestamp = timestamp
    )
}

fun NetworkLastObservation.toDomain(): DomainLastObservation {
    return DomainLastObservation(value = value, status = status, observedAt = observedAt)
}

fun NetworkSynthesis.toDomain(): DomainPositioningSynthesis {
    return DomainPositioningSynthesis(
        headline = headline,
        detail = detail,
        generatedAt = generatedAt,
        contentFlags = contentFlags ?: emptyList(),
        state = state
    )
}

fun NetworkRetailSentiment.toDomain(): DomainRetailSentiment {
    return DomainRetailSentiment(
        bullPct = bullPct,
        bearPct = bearPct,
        neutralPct = neutralPct,
        bullBearSpread = bullBearSpread,
        status = status,
        reportedDate = reportedDate,
        description = description,
        lastObservation = lastObservation?.toDomain(),
        delta = delta,
        deltaDirection = DeltaDirection.fromString(deltaDirection),
        fetchedAt = fetchedAt,
        staleSince = staleSince
    )
}

fun NetworkFuturesContract.toDomain(): DomainFuturesContract {
    return DomainFuturesContract(
        ncNetPctOi = ncNetPctOi,
        ncNetContracts = ncNetContracts,
        status = status,
        percentile = percentile,
        reportDate = reportDate,
        methodology = methodology,
        lastObservation = lastObservation?.toDomain(),
        delta = delta,
        deltaDirection = DeltaDirection.fromString(deltaDirection)
    )
}

fun NetworkInstitutionalPositioning.toDomain(): DomainInstitutionalPositioning {
    return DomainInstitutionalPositioning(
        es = es?.toDomain(),
        nq = nq?.toDomain(),
        rty = rty?.toDomain(),
        dia = dia?.toDomain(),
        description = description,
        fetchedAt = fetchedAt,
        staleSince = staleSince
    )
}

fun NetworkShortInterestInstrument.toDomain(): DomainShortInterestInstrument {
    return DomainShortInterestInstrument(
        shortShares = shortShares,
        daysToCover = daysToCover,
        momChangePct = momChangePct,
        settlementDate = settlementDate,
        status = status,
        lastObservation = lastObservation?.toDomain(),
        delta = delta,
        deltaDirection = DeltaDirection.fromString(deltaDirection)
    )
}

fun NetworkShortInterest.toDomain(): DomainShortInterest {
    return DomainShortInterest(
        spy = spy?.toDomain(),
        qqq = qqq?.toDomain(),
        iwm = iwm?.toDomain(),
        dia = dia?.toDomain(),
        rsp = rsp?.toDomain(),
        mags = mags?.toDomain(),
        description = description,
        fetchedAt = fetchedAt,
        staleSince = staleSince
    )
}

// ============================================================================
// Room mappers -- retailSentiment/institutionalPositioning/shortInterest are stored as one
// JSON-blob TEXT column each (via PositioningConverters), not flattened into scalar columns like
// MarketPostureEntity -- the nesting depth here (3 futures contracts x ~7 fields, 3 short-interest
// instruments x ~7 fields) would mean an unreasonable flat-column explosion for a brand-new table
// with no legacy row shape to preserve. Matches the Indicators/Stocks/Summary convention for
// nested domain objects.
// ============================================================================

fun NetworkMarketPositioning.toEntity(): MarketPositioningEntity {
    return MarketPositioningEntity(
        retailSentiment = retailSentiment?.toDomain(),
        institutionalPositioning = institutionalPositioning?.toDomain(),
        shortInterest = shortInterest?.toDomain(),
        synthesisHeadline = synthesis?.headline,
        synthesisDetail = synthesis?.detail,
        synthesisGeneratedAt = synthesis?.generatedAt,
        synthesisContentFlags = synthesis?.contentFlags,
        synthesisState = synthesis?.state,
        timestamp = timestamp
    )
}

fun MarketPositioningEntity.toDomain(): DomainMarketPositioning {
    return DomainMarketPositioning(
        retailSentiment = retailSentiment,
        institutionalPositioning = institutionalPositioning,
        shortInterest = shortInterest,
        // 💡 Keyed off `state`, not `headline` -- see MarketPostureMappers.kt's identical note.
        synthesis = synthesisState?.let {
            DomainPositioningSynthesis(
                headline = synthesisHeadline,
                detail = synthesisDetail,
                generatedAt = synthesisGeneratedAt,
                contentFlags = synthesisContentFlags ?: emptyList(),
                state = synthesisState
            )
        },
        timestamp = timestamp
    )
}

fun DomainMarketPositioning.toEntity(): MarketPositioningEntity {
    return MarketPositioningEntity(
        id = "market_positioning_id",
        retailSentiment = retailSentiment,
        institutionalPositioning = institutionalPositioning,
        shortInterest = shortInterest,
        synthesisHeadline = synthesis?.headline,
        synthesisDetail = synthesis?.detail,
        synthesisGeneratedAt = synthesis?.generatedAt,
        synthesisContentFlags = synthesis?.contentFlags,
        synthesisState = synthesis?.state,
        timestamp = timestamp
    )
}
