package com.marketlabs.pulse.storage

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.marketlabs.pulse.Constants.MARKET_INDEX_TABLE

@Entity(tableName = MARKET_INDEX_TABLE)
data class MarketIndexEntity(
    @PrimaryKey val symbol: String,
    val trend: String,
    val rsi: Double,
    val signalColor: String,
    val currentPrice: Double,
    val percentChange: Double,
    val lastUpdated: Long
)
