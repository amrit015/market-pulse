package com.marketlabs.pulse.storage.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.marketlabs.pulse.storage.database.converters.IndicatorsConverters
import com.marketlabs.pulse.storage.model.indicators.DomainMacroVitals
import com.marketlabs.pulse.storage.model.indicators.DomainMarketAction
import com.marketlabs.pulse.storage.model.indicators.DomainMarketPhase

@Entity(tableName = "market_indicators")
@TypeConverters(IndicatorsConverters::class)
data class IndicatorsEntity(
    @PrimaryKey(autoGenerate = false) val dateId: String,
    val lastSyncedTimestamp: Long,

    // The Three Pillars (Stored as JSON Strings via TypeConverters)
    val marketPhase: DomainMarketPhase? = null,
    val macroVitals: DomainMacroVitals? = null,
    val marketAction: DomainMarketAction? = null
)