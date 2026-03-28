package com.marketlabs.pulse.storage.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.marketlabs.pulse.storage.database.converters.RiskConverters
import com.marketlabs.pulse.storage.model.riskRadar.RiskGauges

@Entity(tableName = "market_risk")
@TypeConverters(RiskConverters::class)
data class RiskRadarEntity(
    @PrimaryKey(autoGenerate = false)
    val date: String,
    val lastSyncedTimestamp: Long,
    val lastUpdated: Long,
    val score: Int? = null,
    val previousScore: Int? = null,
    val trend: String? = null,
    val status: String? = null,
    val gauges: RiskGauges? = null
)