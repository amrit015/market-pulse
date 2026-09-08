package com.marketlabs.pulse.storage.store.summary

import com.marketlabs.pulse.core.summary.SummaryDateEntry
import com.marketlabs.pulse.storage.database.dao.SummaryDao
import com.marketlabs.pulse.storage.database.entity.MarketPulseEntity
import com.marketlabs.pulse.storage.model.summary.MarketPulse
import com.marketlabs.pulse.storage.model.summary.mappers.toDomain
import com.marketlabs.pulse.storage.model.summary.mappers.toMarketPulseEntity
import com.marketlabs.pulse.utils.getTodayDateString
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LocalSummaryDataSourceImpl @Inject constructor(
    private val dao: SummaryDao
) : LocalSummaryDataSource {

    // V3 Stream -- explicitly scoped to TODAY's own dateId (NY calendar, getTodayDateString()),
    // not just "whichever row was most recently cached". Now that market_pulse holds one row per
    // day (the calendar strip), the two used to be the same thing but no longer are: on a morning
    // before today's report has posted, "most recently cached" is yesterday's row, and this stream
    // is what the Summary screen's Today tab reads its (live, SyncManager-driven) content from --
    // it must come back null on a morning with no report yet, not silently serve yesterday's.
    override fun getLatestMarketPulse(): Flow<MarketPulse?> {
        return dao.getByDateId(getTodayDateString()).map { it?.toDomain() }
    }

    override suspend fun saveMarketPulse(pulse: MarketPulse) {
        dao.insertMarketPulse(pulse.toMarketPulseEntity())
    }

    override suspend fun clearAll() {
        dao.clearMarketPulse()
    }

    // ========================================================================
    // SYNC MANAGER TIMESTAMPS -- same today-scoping as getLatestMarketPulse() above, and for the
    // same reason: SyncManager's freshness check needs to know when TODAY's row was last synced,
    // not whichever day's row happens to have the highest lastUpdated.
    // ========================================================================

    override suspend fun getLastSyncedTimestamp(): Long? {
        return dao.getLastSyncedTimestampForDate(getTodayDateString())
    }

    override suspend fun updateLastSyncedTimestamp(timestamp: Long) {
        dao.updateLastSyncedTimestampForDate(getTodayDateString(), timestamp)
    }

    // ========================================================================
    // CALENDAR STRIP (past-day lookups by dateId)
    // ========================================================================

    override fun getMarketPulseForDate(dateId: String): Flow<SummaryDateEntry> {
        return dao.getByDateId(dateId).map { entity ->
            SummaryDateEntry(
                data = entity?.takeIf { it.hasReport }?.toDomain(),
                cached = entity != null
            )
        }
    }

    override suspend fun getLastSyncedTimestampForDate(dateId: String): Long? {
        return dao.getLastSyncedTimestampForDate(dateId)
    }

    override suspend fun saveTombstone(dateId: String, lastSyncedTimestamp: Long) {
        dao.insertMarketPulse(
            MarketPulseEntity(
                dateId = dateId,
                lastSyncedTimestamp = lastSyncedTimestamp,
                lastUpdated = 0L,
                reportType = "",
                hasReport = false
            )
        )
    }
}