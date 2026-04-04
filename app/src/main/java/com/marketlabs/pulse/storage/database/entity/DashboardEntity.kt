package com.marketlabs.pulse.storage.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "market_state")
data class MarketStateEntity(
    @PrimaryKey val id: Int = 1,
    val isEquityOpen: Boolean?,
    val isFuturesOpen: Boolean?,
    val technicalSummary: String?, // 💡 NEW
    val technicalSummaryTimestamp: Long?, // 💡 NEW
    val lastUpdated: Long?
)

@Entity(tableName = "dashboard_assets")
data class AssetOverviewEntity(
    @PrimaryKey val symbol: String,
    val name: String?,
    val type: String?,
    val description: String?,
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