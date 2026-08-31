package com.marketlabs.pulse.storage.database.entity

import androidx.room.Entity
import androidx.room.TypeConverters
import com.marketlabs.pulse.storage.database.converters.ChartsConverters
import com.marketlabs.pulse.storage.model.charts.ChartPoint

/**
 * One row per `(symbol, rangeKey)` pair — switching the range picker is a different cached row,
 * not a client-side slice of one full series (see `ChartModels.kt`'s doc comment on why). `points`
 * is a JSON-blob column via `ChartsConverters`, same convention as every other nested/list column
 * in this codebase. No TTL — shown until an explicit re-fetch (screen focus / pull-to-refresh),
 * same as every other domain (`docs/architecture/data-flow.md`); no `SyncManager` flag exists for this domain.
 */
@Entity(tableName = "market_charts", primaryKeys = ["symbol", "rangeKey"])
@TypeConverters(ChartsConverters::class)
data class ChartEntity(
    val symbol: String,
    val rangeKey: String,
    val lastSyncedTimestamp: Long,

    val name: String? = null,
    val type: String? = null,
    val points: List<ChartPoint>? = null
)
