package com.marketlabs.pulse.storage.model.dashboard.mappers

import com.marketlabs.pulse.network.model.dashboard.NetworkAssetOverview
import com.marketlabs.pulse.network.model.dashboard.NetworkMarketState
import com.marketlabs.pulse.storage.database.entity.AssetOverviewEntity
import com.marketlabs.pulse.storage.database.entity.MarketStateEntity
import com.marketlabs.pulse.storage.model.dashboard.AssetOverview
import com.marketlabs.pulse.storage.model.dashboard.MarketState
import com.marketlabs.pulse.storage.model.dashboard.enums.AssetType

// ==========================================
// 📱 ENTITY <-> DOMAIN (Room <-> UI)
// ==========================================

fun MarketStateEntity.toDomain(): MarketState {
    return MarketState(
        isEquityOpen = isEquityOpen,
        isFuturesOpen = isFuturesOpen,
        lastUpdated = lastUpdated
    )
}

fun MarketState.toEntity(): MarketStateEntity {
    return MarketStateEntity(
        id = 1,
        isEquityOpen = isEquityOpen,
        isFuturesOpen = isFuturesOpen,
        lastUpdated = lastUpdated
    )
}

fun AssetOverviewEntity.toDomain(): AssetOverview {
    return AssetOverview(
        symbol = symbol,
        name = name,
        description = description,
        type = AssetType.fromString(type),
        isInverted = isInverted,
        price = price,
        previousClose = previousClose,
        changePercent = changePercent,
        rsi = rsi,
        rsiStatus = rsiStatus,
        macdSignal = macdSignal,
        technicalStatus = technicalStatus,
        aiVerdict = aiVerdict,
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
        description = description,
        type = type.name,
        isInverted = isInverted,
        price = price,
        previousClose = previousClose,
        changePercent = changePercent,
        rsi = rsi,
        rsiStatus = rsiStatus,
        macdSignal = macdSignal,
        technicalStatus = technicalStatus,
        aiVerdict = aiVerdict,
        lastUpdated = lastUpdated,
        sma20 = sma20,
        sma50 = sma50,
        sma200 = sma200
    )
}

// ==========================================
// 🌐 NETWORK -> ENTITY (Retrofit -> Room)
// ==========================================

fun NetworkMarketState.toEntity(): MarketStateEntity {
    return MarketStateEntity(
        id = 1,
        isEquityOpen = this.isEquityOpen,
        isFuturesOpen = this.isFuturesOpen,
        lastUpdated = System.currentTimeMillis() // Stamp it locally on arrival
    )
}

fun NetworkAssetOverview.toEntity(): AssetOverviewEntity {
    return AssetOverviewEntity(
        symbol = this.symbol ?: "",
        name = this.name,
        description = this.description,
        type = this.type,
        isInverted = this.isInverted,
        price = this.price,
        previousClose = this.previousClose,
        changePercent = this.changePercent,
        rsi = this.rsi,
        rsiStatus = this.rsiStatus,
        macdSignal = this.macdSignal,
        technicalStatus = this.technicalStatus,
        aiVerdict = this.aiVerdict,
        lastUpdated = System.currentTimeMillis(), // Stamp it locally on arrival
        sma20 = this.sma20,
        sma50 = this.sma50,
        sma200 = this.sma200
    )
}