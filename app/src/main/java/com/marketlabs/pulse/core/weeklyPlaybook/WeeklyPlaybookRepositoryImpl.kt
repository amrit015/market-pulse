package com.marketlabs.pulse.core.weeklyPlaybook

import android.util.Log
import com.marketlabs.pulse.network.store.weeklyPlaybook.RemoteWeeklyPlaybookDataSource
import com.marketlabs.pulse.storage.model.weeklyPlaybook.WeeklyPlaybook
import com.marketlabs.pulse.storage.store.weeklyPlaybook.LocalWeeklyPlaybookDataSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class WeeklyPlaybookRepositoryImpl @Inject constructor(
    private val localDataSource: LocalWeeklyPlaybookDataSource,
    private val remoteDataSource: RemoteWeeklyPlaybookDataSource
) : WeeklyPlaybookRepository {

    override fun getPlaybookStream(): Flow<WeeklyPlaybook?> = localDataSource.getLatestPlaybookStream()

    /**
     * Refreshes the playbook from the network.
     * Note: Cache expiration logic has been removed. This method is now strictly
     * driven by the SyncManager (which detects real-time backend changes) or
     * explicit user pull-to-refresh actions.
     */
    override suspend fun refreshPlaybook(force: Boolean): Result<Unit> {
        return try {
            Log.d("WeeklyPlaybook", "🌐 Fetching latest Playbook from backend...")

            remoteDataSource.getLatestPlaybook().onSuccess { freshPlaybook ->
                localDataSource.savePlaybook(freshPlaybook)
            }.onFailure {
                throw it
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("WeeklyPlaybook", "Failed to refresh playbook", e)
            Result.failure(e)
        }
    }

    /**
     * Retrieves the timestamp of the last successful sync from the local cache.
     * Null is returned if a sync has never occurred.
     */
    override suspend fun getLastSyncedTimestamp(): Long? {
        return localDataSource.getLastSyncedTimestamp()
    }

    /**
     * Updates the local cache with the latest sync timestamp.
     */
    override suspend fun updateLastSyncedTimestamp(timestamp: Long) {
        localDataSource.updateLastSyncedTimestamp(timestamp)
    }
}