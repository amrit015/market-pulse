package com.marketlabs.pulse.storage.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.marketlabs.pulse.storage.model.stocks.DomainDigestSection

@Entity(tableName = "market_state")
data class MarketStateEntity(
    @PrimaryKey val id: Int = 1,
    val isEquityOpen: Boolean?,
    val isFuturesOpen: Boolean?,
    // Flattened, same precedent as MarketPostureEntity/MarketPositioningEntity's own synthesis field.
    val synthesisHeadline: String?,
    val synthesisDetail: String?,
    val synthesisState: String?,
    // JSON-blob column reusing StocksConverters' existing List<DomainDigestSection> adapter.
    val dailyDigestSections: List<DomainDigestSection>?,
    val lastUpdated: Long?
)

@Entity(tableName = "dashboard_assets")
data class AssetOverviewEntity(
    @PrimaryKey val symbol: String,
    val name: String?,
    val type: String?,
    val isInverted: Boolean?,

    val price: Double?,
    val previousClose: Double?,
    val changePercent: Double?,

    val rsi: Double?,
    val rsiStatus: String?,
    val macdSignal: String?,
    val technicalStatus: String?,
    val lastUpdated: Long?,

    val sma20: Double?,
    val sma50: Double?,
    val sma200: Double?
)