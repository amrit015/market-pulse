package com.marketlabs.pulse.storage.model.posture.mappers

import com.marketlabs.pulse.network.model.posture.NetworkDarkPoolIndex
import com.marketlabs.pulse.network.model.posture.NetworkLastObservation
import com.marketlabs.pulse.network.model.posture.NetworkMarketPosture
import com.marketlabs.pulse.network.model.posture.NetworkNaaimExposure
import com.marketlabs.pulse.network.model.posture.NetworkNetLiquidity
import com.marketlabs.pulse.network.model.posture.NetworkSynthesis
import com.marketlabs.pulse.storage.database.entity.MarketPostureEntity
import com.marketlabs.pulse.storage.model.posture.DomainDarkPoolIndex
import com.marketlabs.pulse.storage.model.posture.DomainLastObservation
import com.marketlabs.pulse.storage.model.posture.DomainMarketPosture
import com.marketlabs.pulse.storage.model.posture.DomainNaaimExposure
import com.marketlabs.pulse.storage.model.posture.DomainNetLiquidity
import com.marketlabs.pulse.storage.model.posture.DomainPostureSynthesis
import com.marketlabs.pulse.utils.enums.DeltaDirection

fun NetworkMarketPosture.toDomain(): DomainMarketPosture {
    return DomainMarketPosture(
        naaimExposure = naaimExposure?.toDomain(),
        darkPoolIndex = darkPoolIndex?.toDomain(),
        netLiquidity = netLiquidity?.toDomain(),
        synthesis = synthesis?.toDomain(),
        timestamp = timestamp
    )
}

fun NetworkLastObservation.toDomain(): DomainLastObservation {
    return DomainLastObservation(
        value = value,
        status = status,
        observedAt = observedAt
    )
}

fun NetworkSynthesis.toDomain(): DomainPostureSynthesis {
    return DomainPostureSynthesis(
        headline = headline,
        detail = detail,
        generatedAt = generatedAt,
        contentFlags = contentFlags ?: emptyList(),
        state = state
    )
}

fun NetworkNaaimExposure.toDomain(): DomainNaaimExposure {
    return DomainNaaimExposure(
        value = value,
        status = status,
        description = description,
        lastObservation = lastObservation?.toDomain(),
        delta = delta,
        deltaDirection = DeltaDirection.fromString(deltaDirection),
        fetchedAt = fetchedAt,
        staleSince = staleSince
    )
}

fun NetworkDarkPoolIndex.toDomain(): DomainDarkPoolIndex {
    return DomainDarkPoolIndex(
        value = value,
        date = date,
        status = status,
        description = description,
        lastObservation = lastObservation?.toDomain(),
        delta = delta,
        deltaDirection = DeltaDirection.fromString(deltaDirection),
        fetchedAt = fetchedAt,
        staleSince = staleSince
    )
}

fun NetworkNetLiquidity.toDomain(): DomainNetLiquidity {
    return DomainNetLiquidity(
        value = value,
        status = status,
        assetsT = assetsT,
        tgaT = tgaT,
        rrpT = rrpT,
        date = date,
        description = description,
        lastObservation = lastObservation?.toDomain(),
        delta = delta,
        deltaDirection = DeltaDirection.fromString(deltaDirection),
        fetchedAt = fetchedAt,
        staleSince = staleSince
    )
}

fun NetworkMarketPosture.toEntity(): MarketPostureEntity {
    return MarketPostureEntity(
        naaimValue = naaimExposure?.value,
        naaimStatus = naaimExposure?.status,
        naaimDescription = naaimExposure?.description,
        naaimLastObsValue = naaimExposure?.lastObservation?.value,
        naaimLastObsStatus = naaimExposure?.lastObservation?.status,
        naaimLastObsObservedAt = naaimExposure?.lastObservation?.observedAt,
        naaimDelta = naaimExposure?.delta,
        naaimDeltaDirection = naaimExposure?.deltaDirection,
        naaimFetchedAt = naaimExposure?.fetchedAt,
        naaimStaleSince = naaimExposure?.staleSince,

        dixValue = darkPoolIndex?.value,
        dixDate = darkPoolIndex?.date,
        dixStatus = darkPoolIndex?.status,
        dixDescription = darkPoolIndex?.description,
        dixLastObsValue = darkPoolIndex?.lastObservation?.value,
        dixLastObsStatus = darkPoolIndex?.lastObservation?.status,
        dixLastObsObservedAt = darkPoolIndex?.lastObservation?.observedAt,
        dixDelta = darkPoolIndex?.delta,
        dixDeltaDirection = darkPoolIndex?.deltaDirection,
        dixFetchedAt = darkPoolIndex?.fetchedAt,
        dixStaleSince = darkPoolIndex?.staleSince,

        netLiqValue = netLiquidity?.value,
        netLiqStatus = netLiquidity?.status,
        netLiqAssetsT = netLiquidity?.assetsT,
        netLiqTgaT = netLiquidity?.tgaT,
        netLiqRrpT = netLiquidity?.rrpT,
        netLiqDate = netLiquidity?.date,
        netLiqDescription = netLiquidity?.description,
        netLiqLastObsValue = netLiquidity?.lastObservation?.value,
        netLiqLastObsStatus = netLiquidity?.lastObservation?.status,
        netLiqLastObsObservedAt = netLiquidity?.lastObservation?.observedAt,
        netLiqDelta = netLiquidity?.delta,
        netLiqDeltaDirection = netLiquidity?.deltaDirection,
        netLiqFetchedAt = netLiquidity?.fetchedAt,
        netLiqStaleSince = netLiquidity?.staleSince,

        synthesisHeadline = synthesis?.headline,
        synthesisDetail = synthesis?.detail,
        synthesisGeneratedAt = synthesis?.generatedAt,
        synthesisContentFlags = synthesis?.contentFlags,
        synthesisState = synthesis?.state,

        timestamp = timestamp
    )
}

fun MarketPostureEntity.toDomain(): DomainMarketPosture {
    return DomainMarketPosture(
        naaimExposure = DomainNaaimExposure(
            value = naaimValue,
            status = naaimStatus,
            description = naaimDescription,
            lastObservation = naaimLastObsValue?.let {
                DomainLastObservation(value = it, status = naaimLastObsStatus ?: "", observedAt = naaimLastObsObservedAt ?: 0L)
            },
            delta = naaimDelta,
            deltaDirection = DeltaDirection.fromString(naaimDeltaDirection),
            fetchedAt = naaimFetchedAt,
            staleSince = naaimStaleSince
        ),
        darkPoolIndex = DomainDarkPoolIndex(
            value = dixValue,
            date = dixDate,
            status = dixStatus,
            description = dixDescription,
            lastObservation = dixLastObsValue?.let {
                DomainLastObservation(value = it, status = dixLastObsStatus ?: "", observedAt = dixLastObsObservedAt ?: 0L)
            },
            delta = dixDelta,
            deltaDirection = DeltaDirection.fromString(dixDeltaDirection),
            fetchedAt = dixFetchedAt,
            staleSince = dixStaleSince
        ),
        netLiquidity = DomainNetLiquidity(
            value = netLiqValue,
            status = netLiqStatus,
            assetsT = netLiqAssetsT,
            tgaT = netLiqTgaT,
            rrpT = netLiqRrpT,
            date = netLiqDate,
            description = netLiqDescription,
            lastObservation = netLiqLastObsValue?.let {
                DomainLastObservation(value = it, status = netLiqLastObsStatus ?: "", observedAt = netLiqLastObsObservedAt ?: 0L)
            },
            delta = netLiqDelta,
            deltaDirection = DeltaDirection.fromString(netLiqDeltaDirection),
            fetchedAt = netLiqFetchedAt,
            staleSince = netLiqStaleSince
        ),
        // 💡 Keyed off `state`, not `headline` -- the "unavailable" first-run case (see
        // NetworkSynthesis's doc comment) has a real synthesis object with a null headline/detail
        // but a non-null `state`, and the hero card's "Preview unavailable state" treatment needs
        // that distinction preserved rather than collapsing it into "no synthesis at all."
        synthesis = synthesisState?.let {
            DomainPostureSynthesis(
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

fun DomainMarketPosture.toEntity(): MarketPostureEntity {
    return MarketPostureEntity(
        id = "market_posture_id",
        naaimValue = naaimExposure?.value,
        naaimStatus = naaimExposure?.status,
        naaimDescription = naaimExposure?.description,
        naaimLastObsValue = naaimExposure?.lastObservation?.value,
        naaimLastObsStatus = naaimExposure?.lastObservation?.status,
        naaimLastObsObservedAt = naaimExposure?.lastObservation?.observedAt,
        naaimDelta = naaimExposure?.delta,
        naaimDeltaDirection = naaimExposure?.deltaDirection?.name,
        naaimFetchedAt = naaimExposure?.fetchedAt,
        naaimStaleSince = naaimExposure?.staleSince,

        dixValue = darkPoolIndex?.value,
        dixDate = darkPoolIndex?.date,
        dixStatus = darkPoolIndex?.status,
        dixDescription = darkPoolIndex?.description,
        dixLastObsValue = darkPoolIndex?.lastObservation?.value,
        dixLastObsStatus = darkPoolIndex?.lastObservation?.status,
        dixLastObsObservedAt = darkPoolIndex?.lastObservation?.observedAt,
        dixDelta = darkPoolIndex?.delta,
        dixDeltaDirection = darkPoolIndex?.deltaDirection?.name,
        dixFetchedAt = darkPoolIndex?.fetchedAt,
        dixStaleSince = darkPoolIndex?.staleSince,

        netLiqValue = netLiquidity?.value,
        netLiqStatus = netLiquidity?.status,
        netLiqAssetsT = netLiquidity?.assetsT,
        netLiqTgaT = netLiquidity?.tgaT,
        netLiqRrpT = netLiquidity?.rrpT,
        netLiqDate = netLiquidity?.date,
        netLiqDescription = netLiquidity?.description,
        netLiqLastObsValue = netLiquidity?.lastObservation?.value,
        netLiqLastObsStatus = netLiquidity?.lastObservation?.status,
        netLiqLastObsObservedAt = netLiquidity?.lastObservation?.observedAt,
        netLiqDelta = netLiquidity?.delta,
        netLiqDeltaDirection = netLiquidity?.deltaDirection?.name,
        netLiqFetchedAt = netLiquidity?.fetchedAt,
        netLiqStaleSince = netLiquidity?.staleSince,

        synthesisHeadline = synthesis?.headline,
        synthesisDetail = synthesis?.detail,
        synthesisGeneratedAt = synthesis?.generatedAt,
        synthesisContentFlags = synthesis?.contentFlags,
        synthesisState = synthesis?.state,

        timestamp = timestamp
    )
}