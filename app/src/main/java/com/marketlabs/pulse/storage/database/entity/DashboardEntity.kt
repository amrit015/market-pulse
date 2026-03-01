package com.marketlabs.pulse.storage.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "market_state")
data class MarketStateEntity(
    @PrimaryKey val id: Int = 1, // Only ever one row, non-nullable
    val isEquityOpen: Boolean?,
    val isFuturesOpen: Boolean?,
    val lastUpdated: Long?
)

@Entity(tableName = "dashboard_assets")
data class AssetOverviewEntity(
    @PrimaryKey val symbol: String, // Symbol must be non-nullable as the primary key
    val name: String?,
    val type: String?, // Maps to AssetType Enum
    val description: String?,
    val isInverted: Boolean?,

    val price: Double?,
    val previousClose: Double?,
    val changePercent: Double?,

    val rsi: Double?,
    val rsiStatus: String?,
    val macdSignal: String?,
    val technicalStatus: String?,
    val aiVerdict: String?,
    val lastUpdated: Long?,

    val sma20: Double?,
    val sma50: Double?,
    val sma200: Double?
)