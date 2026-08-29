package com.marketlabs.pulse.storage.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 💡 THOUGHT PROCESS:
 * We use a single, hardcoded primary key ("market_posture_id") because this is a
 * singleton data object representing the current market state. By replacing it on
 * conflict, we ensure the local database only ever holds the latest snapshot.
 * All fields default to null to safely handle missing or partial backend updates.
 */
// 💡 2026-08-26 revamp: added last_observation/delta/delta_direction/fetched_at/stale_since to
// each of the three existing gauges, plus a new synthesis block -- kept as flat nullable columns
// (naaimLastObsValue, naaimDelta, ...) rather than restructuring this entity onto JSON-blob
// columns like Indicators/Stocks do for their own nested objects, since every field this entity
// already stores is flat scalars too and a straightforward `ADD COLUMN` migration (see
// MIGRATION_18_19) keeps that consistent instead of rebuilding the table. `contentFlags` is the
// one exception -- it's a `List<String>?`, which already has a registered database-wide
// TypeConverter via `NewsConverters.fromStringList`/`toStringList` (see StocksConverters.kt's own
// doc comment on why a second one here would fail KSP with "Multiple functions define the same
// conversion").
@Entity(tableName = "market_posture")
data class MarketPostureEntity(
    @PrimaryKey
    val id: String = "market_posture_id",

    // NAAIM Exposure
    val naaimValue: Double? = null,
    val naaimStatus: String? = null,
    val naaimDescription: String? = null,
    val naaimLastObsValue: Double? = null,
    val naaimLastObsStatus: String? = null,
    val naaimLastObsObservedAt: Long? = null,
    val naaimDelta: Double? = null,
    val naaimDeltaDirection: String? = null,
    val naaimFetchedAt: Long? = null,
    val naaimStaleSince: Long? = null,

    // Dark Pool Index (DIX)
    val dixValue: Double? = null,
    val dixDate: String? = null,
    val dixStatus: String? = null,
    val dixDescription: String? = null,
    val dixLastObsValue: Double? = null,
    val dixLastObsStatus: String? = null,
    val dixLastObsObservedAt: Long? = null,
    val dixDelta: Double? = null,
    val dixDeltaDirection: String? = null,
    val dixFetchedAt: Long? = null,
    val dixStaleSince: Long? = null,

    // Net Liquidity
    val netLiqValue: Double? = null,
    val netLiqStatus: String? = null,
    val netLiqAssetsT: Double? = null,
    val netLiqTgaT: Double? = null,
    val netLiqRrpT: Double? = null,
    val netLiqDate: String? = null,
    val netLiqDescription: String? = null,
    val netLiqLastObsValue: Double? = null,
    val netLiqLastObsStatus: String? = null,
    val netLiqLastObsObservedAt: Long? = null,
    val netLiqDelta: Double? = null,
    val netLiqDeltaDirection: String? = null,
    val netLiqFetchedAt: Long? = null,
    val netLiqStaleSince: Long? = null,

    // Synthesis (Gemini narrative layer)
    val synthesisHeadline: String? = null,
    val synthesisDetail: String? = null,
    val synthesisGeneratedAt: Long? = null,
    val synthesisContentFlags: List<String>? = null,
    val synthesisState: String? = null,

    val timestamp: Long? = null
)