package com.marketlabs.pulse.storage.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.marketlabs.pulse.storage.database.converters.MarketSummaryConverters
import com.marketlabs.pulse.storage.model.summary.DominoEffect
import com.marketlabs.pulse.storage.model.summary.MacroItem
import com.marketlabs.pulse.storage.model.summary.MarketLookout
import com.marketlabs.pulse.storage.model.summary.MarketPulse
import com.marketlabs.pulse.storage.model.summary.NewsItem
import com.marketlabs.pulse.storage.model.summary.Verdict
import com.marketlabs.pulse.storage.model.summary.enums.MarketRegime
import com.marketlabs.pulse.storage.model.summary.enums.ReportType
import com.marketlabs.pulse.storage.model.summary.enums.TechnicalSetup
import com.marketlabs.pulse.storage.model.summary.enums.TradingCall
import com.marketlabs.pulse.utils.Constants
import com.marketlabs.pulse.utils.toDateIdString

@Entity(tableName = "market_pulse")
@TypeConverters(MarketSummaryConverters::class)
data class MarketSummaryEntity(
    @PrimaryKey(autoGenerate = false)
    val dateId: String, // "2026-02-04" (Using Date String as ID for easier lookup)
    val lastSyncedTimestamp: Long, // 💡 NEW: When did we last fetch this from network?
    val serverTimestamp: Long, // timestamp when the report was generated at the server side
    val reportType: String,
    val verdict: Verdict,
    val leadStories: List<NewsItem>,
    val macroMix: List<MacroItem>,
    val dominoEffect: DominoEffect,
    val marketLookout: MarketLookout
)

// Mapper Extension: Domain -> Entity
fun MarketPulse.toEntity(
    // Default to NOW, but allow overriding if needed
    currentSyncTime: Long = System.currentTimeMillis()
): MarketSummaryEntity {
    return MarketSummaryEntity(
        // Generate ID: Convert Server Timestamp -> "YYYY-MM-DD" (EST Time)
        dateId = this.timestamp?.toDateIdString() ?: "",
        // Cache Time: When did the App fetch this?
        lastSyncedTimestamp = currentSyncTime,
        // Server Time: When did the AI generate this?
        serverTimestamp = this.timestamp ?: 0,
        reportType = this.reportType?.label ?: "",
        verdict = this.verdict ?: Verdict(
            regime = MarketRegime.UNKNOWN,
            setup = TechnicalSetup.UNKNOWN,
            call = TradingCall.UNKNOWN,
            analysis = Constants.UNKNOWN,
            action = Constants.UNKNOWN
        ),
        leadStories = this.leadStories ?: emptyList(),
        macroMix = this.macroMix ?: emptyList(),
        dominoEffect = this.dominoEffect ?: DominoEffect(
            trigger = Constants.UNKNOWN,
            impact = Constants.UNKNOWN,
            outlook = Constants.UNKNOWN
        ),
        marketLookout = this.marketLookout ?: MarketLookout(
            outlook = Constants.UNKNOWN
        )
    )
}

// 2. Entity -> Domain (Reading from DB)
fun MarketSummaryEntity.toDomain(): MarketPulse {
    return MarketPulse(
        reportType = ReportType.from(this.reportType),
        timestamp = this.serverTimestamp, // Domain only cares about Server Time
        verdict = this.verdict,
        leadStories = this.leadStories,
        macroMix = this.macroMix,
        dominoEffect = this.dominoEffect,
        marketLookout = this.marketLookout
    )
}
