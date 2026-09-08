package com.marketlabs.pulse.storage.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.marketlabs.pulse.storage.database.entity.MarketPulseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SummaryDao {

    // --- V3 (Gemini 3.x Pro - Main Content) ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMarketPulse(pulse: MarketPulseEntity)

    @Query("DELETE FROM market_pulse")
    suspend fun clearMarketPulse()

    // --- By dateId (today's live row, and calendar-strip past-day lookups) ---
    //
    // 💡 There used to be an `ORDER BY lastUpdated DESC LIMIT 1` "latest" query here, back when
    // this table only ever held one row total (today's, always overwritten). Once the calendar
    // strip started caching one row per day, "latest by lastUpdated" silently stopped meaning
    // "today's row" -- it returns whatever day's report was most recently generated, which is
    // yesterday's whenever today's hasn't posted yet (e.g. every morning before the afternoon
    // report run). Every caller that actually means "today" now passes today's own dateId
    // explicitly instead (SummaryRepositoryImpl computes it via getTodayDateString()).

    @Query("SELECT * FROM market_pulse WHERE dateId = :dateId LIMIT 1")
    fun getByDateId(dateId: String): Flow<MarketPulseEntity?>

    /**
     * When this dateId's row was last written to cache -- null if never cached at all. Used both
     * to decide whether a past date needs a Firestore refetch (core/summary/SummaryRepository.kt's
     * syncPastDate) and, for today's own dateId, as SyncManager's freshness bookkeeping (below) --
     * a lighter query than reading the full row for either purpose.
     */
    @Query("SELECT lastSyncedTimestamp FROM market_pulse WHERE dateId = :dateId LIMIT 1")
    suspend fun getLastSyncedTimestampForDate(dateId: String): Long?

    /**
     * Updates only the timestamp without overwriting the existing summary text. A no-op (matches
     * zero rows) if [dateId] hasn't been cached yet -- e.g. SyncManager calling this for today
     * right after a failed fetch, which is fine: the next sync tick will see no stored timestamp
     * for today and retry, rather than this silently stamping some unrelated day's row instead.
     */
    @Query("UPDATE market_pulse SET lastSyncedTimestamp = :timestamp WHERE dateId = :dateId")
    suspend fun updateLastSyncedTimestampForDate(dateId: String, timestamp: Long)
}