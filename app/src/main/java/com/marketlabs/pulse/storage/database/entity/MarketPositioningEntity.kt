package com.marketlabs.pulse.storage.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.marketlabs.pulse.storage.model.positioning.DomainInstitutionalPositioning
import com.marketlabs.pulse.storage.model.positioning.DomainRetailSentiment
import com.marketlabs.pulse.storage.model.positioning.DomainShortInterest

/**
 * Singleton row (hardcoded "market_positioning_id"), same pattern as MarketPostureEntity --
 * replace-on-conflict keeps this table holding only the latest snapshot.
 *
 * retailSentiment/institutionalPositioning/shortInterest are each one JSON-blob TEXT column
 * (PositioningConverters) rather than flattened scalar columns -- see MarketPositioningMappers.kt's
 * doc comment for why. `synthesisContentFlags` reuses the database-wide `List<String>?` converter
 * already registered by NewsConverters, same as MarketPostureEntity's identical column.
 */
@Entity(tableName = "market_positioning")
data class MarketPositioningEntity(
    @PrimaryKey
    val id: String = "market_positioning_id",

    val retailSentiment: DomainRetailSentiment? = null,
    val institutionalPositioning: DomainInstitutionalPositioning? = null,
    val shortInterest: DomainShortInterest? = null,

    val synthesisHeadline: String? = null,
    val synthesisDetail: String? = null,
    val synthesisGeneratedAt: Long? = null,
    val synthesisContentFlags: List<String>? = null,
    val synthesisState: String? = null,

    val timestamp: Long? = null
)
