package com.marketlabs.pulse.core.indicators

import com.marketlabs.pulse.storage.model.indicators.MetricHistorySeries
import kotlinx.coroutines.flow.Flow

/**
 * On-demand, per-`metricId` -- gated on `system/sync_status`'s `indicator_charts_updated` flag
 * (see `MetricHistoryRepositoryImpl`), same principle as `ChartsRepository`. No range parameter
 * (unlike `ChartsRepository`) -- see `MetricHistorySeries`'s doc comment on why.
 */
interface MetricHistoryRepository {

    fun getHistoryStream(metricId: String): Flow<MetricHistorySeries?>
    suspend fun refreshHistory(metricId: String, limit: Int? = null): Result<Unit>
}
