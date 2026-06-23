package com.marketlabs.pulse.storage.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.marketlabs.pulse.storage.database.converters.IndicatorsConverters
import com.marketlabs.pulse.storage.model.indicators.DomainAiSynthesis
import com.marketlabs.pulse.storage.model.indicators.DomainIndicatorPillar

@Entity(tableName = "market_indicators")
@TypeConverters(IndicatorsConverters::class)
data class IndicatorsEntity(
    @PrimaryKey(autoGenerate = false) val dateId: String,
    val lastSyncedTimestamp: Long,

    // The Four Pillars + AI (Stored as JSON Strings via TypeConverters)
    val aiSynthesis: DomainAiSynthesis? = null,

    // Notice how all four quantitative pillars cleanly reuse the exact same unified model!
    val tacticalMomentum: DomainIndicatorPillar? = null,
    val systemicRisk: DomainIndicatorPillar? = null,
    val valuation: DomainIndicatorPillar? = null,
    val macroVitals: DomainIndicatorPillar? = null
)