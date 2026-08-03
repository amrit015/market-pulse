package com.marketlabs.pulse.storage.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.marketlabs.pulse.storage.database.converters.SummaryConverters
import com.marketlabs.pulse.storage.model.summary.DominoEffect
import com.marketlabs.pulse.storage.model.summary.MacroItem
import com.marketlabs.pulse.storage.model.summary.MarketOutlook
import com.marketlabs.pulse.storage.model.summary.NewsItem
import com.marketlabs.pulse.storage.model.summary.Verdict

// ============================================================================
//  V3 ENTITY (Gemini 3.x Pro - Main Content)
// ============================================================================
@Entity(tableName = "market_pulse")
@TypeConverters(SummaryConverters::class)
data class MarketPulseEntity(
    @PrimaryKey(autoGenerate = false)
    val dateId: String,
    val lastSyncedTimestamp: Long,
    val lastUpdated: Long,
    val reportType: String,
    val verdict: Verdict? = null,
    val leadStories: List<NewsItem>? = null,
    val macroMix: List<MacroItem>? = null,
    val dominoEffect: DominoEffect? = null,
    val marketOutlook: MarketOutlook? = null
)