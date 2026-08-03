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
@Entity(tableName = "market_posture")
data class MarketPostureEntity(
    @PrimaryKey
    val id: String = "market_posture_id",

    // NAAIM Exposure
    val naaimValue: Double? = null,
    val naaimStatus: String? = null,
    val naaimDescription: String? = null,

    // Dark Pool Index (DIX)
    val dixValue: Double? = null,
    val dixDate: String? = null,
    val dixStatus: String? = null,
    val dixDescription: String? = null,

    // Net Liquidity
    val netLiqValue: Double? = null,
    val netLiqStatus: String? = null,
    val netLiqAssetsT: Double? = null,
    val netLiqTgaT: Double? = null,
    val netLiqRrpT: Double? = null,
    val netLiqDate: String? = null,
    val netLiqDescription: String? = null,

    val timestamp: Long? = null
)