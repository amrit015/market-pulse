package com.marketlabs.pulse.storage.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.marketlabs.pulse.storage.database.converters.RiskConverters
import com.marketlabs.pulse.storage.model.marketRisk.MarketRiskFactor

@Entity(tableName = "market_tail_risks")
@TypeConverters(RiskConverters::class)
data class MarketRiskEntity(
    @PrimaryKey(autoGenerate = false)
    val date: String,
    val lastSyncedTimestamp: Long,
    val lastUpdated: Long? = null,
    val summary: String? = null,
    val risks: List<MarketRiskFactor>? = null,
    val sourceNarrative: String? = null
)