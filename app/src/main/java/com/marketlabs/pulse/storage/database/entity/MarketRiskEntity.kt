package com.marketlabs.pulse.storage.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.marketlabs.pulse.storage.database.converters.RiskConverters
import com.marketlabs.pulse.storage.model.marketRisk.MarketRiskFactor

// 💡 The `synthesis` block is stored as flat nullable columns (synthesisHeadline, synthesisDetail,
// ...), matching MarketPostureEntity's convention for the same narrative layer, even though this
// entity's own main content (`risks`) is stored as a JSON-blob column via RiskConverters rather
// than flat scalars -- the two are independent storage choices, and synthesis's own 5 fields are
// flat scalars like Posture's.
@Entity(tableName = "market_tail_risks")
@TypeConverters(RiskConverters::class)
data class MarketRiskEntity(
    @PrimaryKey(autoGenerate = false)
    val date: String,
    val lastSyncedTimestamp: Long,
    val lastUpdated: Long? = null,
    val risks: List<MarketRiskFactor>? = null,
    val sourceNarrative: String? = null,

    // Synthesis (Gemini narrative layer)
    val synthesisHeadline: String? = null,
    val synthesisDetail: String? = null,
    val synthesisGeneratedAt: Long? = null,
    val synthesisContentFlags: List<String>? = null,
    val synthesisState: String? = null
)