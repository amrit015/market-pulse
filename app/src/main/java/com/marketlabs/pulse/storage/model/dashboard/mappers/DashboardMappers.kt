package com.marketlabs.pulse.storage.model.dashboard.mappers

import com.marketlabs.pulse.network.model.dashboard.NetworkAssetOverview
import com.marketlabs.pulse.network.model.dashboard.NetworkDigestSection
import com.marketlabs.pulse.network.model.dashboard.NetworkMarketState
import com.marketlabs.pulse.network.model.dashboard.NetworkTechnicalSummary
import com.marketlabs.pulse.storage.database.entity.AssetOverviewEntity
import com.marketlabs.pulse.storage.database.entity.MarketStateEntity
import com.marketlabs.pulse.storage.model.dashboard.AssetOverview
import com.marketlabs.pulse.storage.model.dashboard.MarketState
import com.marketlabs.pulse.storage.model.stocks.DomainDigestSection
import com.marketlabs.pulse.utils.enums.AssetType

fun MarketStateEntity.toDomain(): MarketState {
    return MarketState(
        isEquityOpen = isEquityOpen,
        isFuturesOpen = isFuturesOpen,
        synthesisHeadline = synthesisHeadline,
        synthesisDetail = synthesisDetail,
        synthesisState = synthesisState,
        dailyDigestSections = dailyDigestSections,
        lastUpdated = lastUpdated
    )
}

fun MarketState.toEntity(): MarketStateEntity {
    return MarketStateEntity(
        id = 1,
        isEquityOpen = isEquityOpen,
        isFuturesOpen = isFuturesOpen,
        synthesisHeadline = synthesisHeadline,
        synthesisDetail = synthesisDetail,
        synthesisState = synthesisState,
        dailyDigestSections = dailyDigestSections,
        lastUpdated = lastUpdated
    )
}

fun NetworkDigestSection.toDomain(): DomainDigestSection {
    return DomainDigestSection(
        heading = this.heading,
        body = this.body,
        category = this.category
    )
}

/**
 * Merges the `market_overview/technical_summary` doc's fields onto an existing entity being built
 * from `market_state`/asset docs in the same Firestore snapshot -- see
 * `RemoteDashboardDataSourceImpl.observeDashboardData()`, same merge point `technicalSummary` used
 * to go through before this rewrite.
 */
fun MarketStateEntity.mergeTechnicalSummary(summary: NetworkTechnicalSummary?): MarketStateEntity {
    return this.copy(
        synthesisHeadline = summary?.synthesis?.headline,
        synthesisDetail = summary?.synthesis?.detail,
        synthesisState = summary?.state,
        dailyDigestSections = summary?.dailyDigest?.sections?.map { it.toDomain() }
    )
}

fun AssetOverviewEntity.toDomain(): AssetOverview {
    return AssetOverview(
        symbol = symbol,
        name = name,
        type = AssetType.fromString(type),
        isInverted = isInverted,
        price = price,
        previousClose = previousClose,
        changePercent = changePercent,
        rsi = rsi,
        rsiStatus = rsiStatus,
        macdSignal = macdSignal,
        technicalStatus = technicalStatus,
        lastUpdated = lastUpdated,
        sma20 = sma20,
        sma50 = sma50,
        sma200 = sma200
    )
}

fun AssetOverview.toEntity(): AssetOverviewEntity {
    return AssetOverviewEntity(
        symbol = symbol,
        name = name,
        type = type.name,
        isInverted = isInverted,
        price = price,
        previousClose = previousClose,
        changePercent = changePercent,
        rsi = rsi,
        rsiStatus = rsiStatus,
        macdSignal = macdSignal,
        technicalStatus = technicalStatus,
        lastUpdated = lastUpdated,
        sma20 = sma20,
        sma50 = sma50,
        sma200 = sma200
    )
}

/**
 * Base entity from the `market_state` doc alone -- `synthesisHeadline`/`synthesisDetail`/
 * `synthesisState`/`dailyDigestSections` are merged in afterward from the `technical_summary` doc
 * in the same Firestore snapshot, same two-step shape `technicalSummary` used before this rewrite
 * (see `mergeTechnicalSummary` below and `RemoteDashboardDataSourceImpl.observeDashboardData()`).
 */
fun NetworkMarketState.toEntity(): MarketStateEntity {
    return MarketStateEntity(
        id = 1,
        isEquityOpen = this.isEquityOpen,
        isFuturesOpen = this.isFuturesOpen,
        synthesisHeadline = null,
        synthesisDetail = null,
        synthesisState = null,
        dailyDigestSections = null,
        lastUpdated = System.currentTimeMillis()
    )
}

fun NetworkAssetOverview.toEntity(): AssetOverviewEntity {
    return AssetOverviewEntity(
        symbol = this.symbol,
        name = this.name,
        type = this.type,
        isInverted = this.isInverted,
        price = this.price,
        previousClose = this.previousClose,
        changePercent = this.changePercent,
        rsi = this.rsi,
        rsiStatus = this.rsiStatus,
        macdSignal = this.macdSignal,
        technicalStatus = this.technicalStatus,
        lastUpdated = System.currentTimeMillis(),
        sma20 = this.sma20,
        sma50 = this.sma50,
        sma200 = this.sma200
    )
}