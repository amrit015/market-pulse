package com.marketlabs.pulse.storage.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.marketlabs.pulse.storage.database.converters.SummaryConverters
import com.marketlabs.pulse.storage.model.summary.DominoEffect
import com.marketlabs.pulse.storage.model.summary.MacroItem
import com.marketlabs.pulse.storage.model.summary.MarketDriver
import com.marketlabs.pulse.storage.model.summary.MarketPosition
import com.marketlabs.pulse.storage.model.summary.MarketVerdict
import com.marketlabs.pulse.storage.model.summary.NewsItem
import com.marketlabs.pulse.storage.model.summary.RiskItem
import com.marketlabs.pulse.storage.model.summary.WatchItem

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
    val verdict: MarketVerdict? = null,
    val drivers: List<MarketDriver>? = null,
    val position: MarketPosition? = null,
    val leadStories: List<NewsItem>? = null,
    val macroMix: List<MacroItem>? = null,
    val dominoEffect: DominoEffect? = null,
    val watch: List<WatchItem>? = null,
    val risks: List<RiskItem>? = null,
    val whatChanged: String? = null
)