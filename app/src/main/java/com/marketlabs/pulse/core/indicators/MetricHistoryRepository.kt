package com.marketlabs.pulse.core.indicators

import com.marketlabs.pulse.storage.model.indicators.MetricHistorySeries
import kotlinx.coroutines.flow.Flow

/**
 * On-demand, per-`metricId` -- not `SyncManager`-driven, matching `ChartsRepository`'s reasoning:
 * no sync flag exists for this domain, and refetching on the detail page's own focus is enough.
 * No range parameter (unlike `ChartsRepository`) -- see `MetricHistorySeries`'s doc comment on why.
 */
interface MetricHistoryRepository {

    fun getHistoryStream(metricId: String): Flow<MetricHistorySeries?>
    suspend fun refreshHistory(metricId: String, limit: Int? = null): Result<Unit>
}
