package com.marketlabs.pulse.storage.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.marketlabs.pulse.storage.model.MarketTrend
import com.marketlabs.pulse.utils.Constants

/**
 * Local storage model representing a single Market Index.
 * Acts as the Single Source of Truth for the UI.
 */
@Entity(tableName = Constants.MARKET_INDEX_TABLE)
data class MarketIndexEntity(
    @PrimaryKey val symbol: String,
    val trend: MarketTrend,
    val rsi: Double,
    val currentPrice: Double,
    val percentChange: Double,
    val lastUpdated: Long
)