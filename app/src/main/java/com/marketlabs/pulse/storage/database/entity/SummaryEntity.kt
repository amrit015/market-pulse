package com.marketlabs.pulse.storage.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.marketlabs.pulse.storage.database.converters.SummaryConverters
import com.marketlabs.pulse.storage.model.summary.DominoEffect
import com.marketlabs.pulse.storage.model.summary.MacroItem
import com.marketlabs.pulse.storage.model.summary.MarketOutlook
import com.marketlabs.pulse.storage.model.summary.MarketPulse
import com.marketlabs.pulse.storage.model.summary.NewsItem
import com.marketlabs.pulse.storage.model.summary.Verdict
import com.marketlabs.pulse.storage.model.summary.enums.ReportType
import com.marketlabs.pulse.utils.toDateIdString

// ============================================================================
// 1. V3 ENTITY (Gemini 3.1 Pro - Main Content)
// ============================================================================
@Entity(tableName = "market_pulse")
@TypeConverters(SummaryConverters::class)
data class MarketPulseEntity(
    @PrimaryKey(autoGenerate = false)
    val dateId: String,
    val lastSyncedTimestamp: Long,
    val serverTimestamp: Long,
    val reportType: String,
    val verdict: Verdict? = null,
    val leadStories: List<NewsItem>? = null,
    val macroMix: List<MacroItem>? = null,
    val dominoEffect: DominoEffect? = null,
    val marketOutlook: MarketOutlook? = null
)

// ============================================================================
// 2. V2.5 ENTITY (Gemini 2.5 Pro - Banner Content)
// ============================================================================
@Entity(tableName = "daily_pulse")
@TypeConverters(SummaryConverters::class)
data class DailyPulseEntity(
    @PrimaryKey(autoGenerate = false)
    val dateId: String,
    val lastSyncedTimestamp: Long,
    val serverTimestamp: Long,
    val reportType: String,
    val verdict: Verdict? = null,
    val leadStories: List<NewsItem>? = null,
    val macroMix: List<MacroItem>? = null,
    val dominoEffect: DominoEffect? = null,
    val marketOutlook: MarketOutlook? = null
)

// ============================================================================
// MAPPERS: Domain -> Entity (Saving to DB)
// ============================================================================

fun MarketPulse.toMarketPulseEntity(
    currentSyncTime: Long = System.currentTimeMillis()
): MarketPulseEntity {
    return MarketPulseEntity(
        dateId = this.timestamp?.toDateIdString() ?: "",
        lastSyncedTimestamp = currentSyncTime,
        serverTimestamp = this.timestamp ?: 0L,
        reportType = this.reportType?.label ?: "",
        verdict = this.verdict,
        leadStories = this.leadStories,
        macroMix = this.macroMix,
        dominoEffect = this.dominoEffect,
        marketOutlook = this.marketOutlook
    )
}

fun MarketPulse.toDailyPulseEntity(
    currentSyncTime: Long = System.currentTimeMillis()
): DailyPulseEntity {
    return DailyPulseEntity(
        dateId = this.timestamp?.toDateIdString() ?: "",
        lastSyncedTimestamp = currentSyncTime,
        serverTimestamp = this.timestamp ?: 0L,
        reportType = this.reportType?.label ?: "",
        verdict = this.verdict,
        leadStories = this.leadStories,
        macroMix = this.macroMix,
        dominoEffect = this.dominoEffect,
        marketOutlook = this.marketOutlook
    )
}

// ============================================================================
// MAPPERS: Entity -> Domain (Reading from DB to UI)
// ============================================================================

fun MarketPulseEntity.toDomain(): MarketPulse {
    return MarketPulse(
        reportType = ReportType.from(this.reportType),
        timestamp = this.serverTimestamp,
        verdict = this.verdict,
        leadStories = this.leadStories,
        macroMix = this.macroMix,
        dominoEffect = this.dominoEffect,
        marketOutlook = this.marketOutlook
    )
}

fun DailyPulseEntity.toDomain(): MarketPulse {
    return MarketPulse(
        reportType = ReportType.from(this.reportType),
        timestamp = this.serverTimestamp,
        verdict = this.verdict,
        leadStories = this.leadStories,
        macroMix = this.macroMix,
        dominoEffect = this.dominoEffect,
        marketOutlook = this.marketOutlook
    )
}