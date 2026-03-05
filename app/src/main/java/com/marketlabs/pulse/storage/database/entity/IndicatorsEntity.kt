package com.marketlabs.pulse.storage.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.marketlabs.pulse.storage.database.converters.IndicatorsConverters
import com.marketlabs.pulse.storage.model.indicators.PhaseDetails
import com.marketlabs.pulse.storage.model.indicators.PhaseSummary

@Entity(tableName = "market_indicators")
@TypeConverters(IndicatorsConverters::class)
data class IndicatorsEntity(
    @PrimaryKey(autoGenerate = false) val dateId: String,
    val lastSyncedTimestamp: Long,
    val lastUpdated: Long,
    val summary: PhaseSummary? = null,
    val trendPhase: PhaseDetails? = null,
    val healthPhase: PhaseDetails? = null,
    val riskPhase: PhaseDetails? = null
)