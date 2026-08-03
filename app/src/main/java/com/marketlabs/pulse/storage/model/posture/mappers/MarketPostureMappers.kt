package com.marketlabs.pulse.storage.model.posture.mappers

import com.marketlabs.pulse.network.model.posture.NetworkDarkPoolIndex
import com.marketlabs.pulse.network.model.posture.NetworkMarketPosture
import com.marketlabs.pulse.network.model.posture.NetworkNaaimExposure
import com.marketlabs.pulse.network.model.posture.NetworkNetLiquidity
import com.marketlabs.pulse.storage.database.entity.MarketPostureEntity
import com.marketlabs.pulse.storage.model.posture.DomainDarkPoolIndex
import com.marketlabs.pulse.storage.model.posture.DomainMarketPosture
import com.marketlabs.pulse.storage.model.posture.DomainNaaimExposure
import com.marketlabs.pulse.storage.model.posture.DomainNetLiquidity

fun NetworkMarketPosture.toDomain(): DomainMarketPosture {
    return DomainMarketPosture(
        naaimExposure = naaimExposure?.toDomain(),
        darkPoolIndex = darkPoolIndex?.toDomain(),
        netLiquidity = netLiquidity?.toDomain(),
        timestamp = timestamp
    )
}

fun NetworkNaaimExposure.toDomain(): DomainNaaimExposure {
    return DomainNaaimExposure(
        value = value,
        status = status,
        description = description
    )
}

fun NetworkDarkPoolIndex.toDomain(): DomainDarkPoolIndex {
    return DomainDarkPoolIndex(
        value = value,
        date = date,
        status = status,
        description = description
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
        description = description
    )
}

fun NetworkMarketPosture.toEntity(): MarketPostureEntity {
    return MarketPostureEntity(
        naaimValue = naaimExposure?.value,
        naaimStatus = naaimExposure?.status,
        naaimDescription = naaimExposure?.description,

        dixValue = darkPoolIndex?.value,
        dixDate = darkPoolIndex?.date,
        dixStatus = darkPoolIndex?.status,
        dixDescription = darkPoolIndex?.description,

        netLiqValue = netLiquidity?.value,
        netLiqAssetsT = netLiquidity?.assetsT,
        netLiqTgaT = netLiquidity?.tgaT,
        netLiqRrpT = netLiquidity?.rrpT,
        netLiqDate = netLiquidity?.date,
        netLiqDescription = netLiquidity?.description,

        timestamp = timestamp
    )
}

fun MarketPostureEntity.toDomain(): DomainMarketPosture {
    return DomainMarketPosture(
        naaimExposure = DomainNaaimExposure(
            value = naaimValue,
            status = naaimStatus,
            description = naaimDescription
        ),
        darkPoolIndex = DomainDarkPoolIndex(
            value = dixValue,
            date = dixDate,
            status = dixStatus,
            description = dixDescription
        ),
        netLiquidity = DomainNetLiquidity(
            value = netLiqValue,
            status = netLiqStatus,
            assetsT = netLiqAssetsT,
            tgaT = netLiqTgaT,
            rrpT = netLiqRrpT,
            date = netLiqDate,
            description = netLiqDescription
        ),
        timestamp = timestamp
    )
}

fun DomainMarketPosture.toEntity(): MarketPostureEntity {
    return MarketPostureEntity(
        id = "market_posture_id",
        naaimValue = naaimExposure?.value,
        naaimStatus = naaimExposure?.status,
        naaimDescription = naaimExposure?.description,

        dixValue = darkPoolIndex?.value,
        dixDate = darkPoolIndex?.date,
        dixStatus = darkPoolIndex?.status,
        dixDescription = darkPoolIndex?.description,

        netLiqValue = netLiquidity?.value,
        netLiqStatus = netLiquidity?.status,
        netLiqAssetsT = netLiquidity?.assetsT,
        netLiqTgaT = netLiquidity?.tgaT,
        netLiqRrpT = netLiquidity?.rrpT,
        netLiqDate = netLiquidity?.date,
        netLiqDescription = netLiquidity?.description,

        timestamp = timestamp
    )
}