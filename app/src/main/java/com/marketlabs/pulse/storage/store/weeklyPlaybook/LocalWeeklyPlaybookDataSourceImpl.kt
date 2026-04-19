package com.marketlabs.pulse.storage.store.weeklyPlaybook

import com.marketlabs.pulse.storage.database.dao.WeeklyPlaybookDao
import com.marketlabs.pulse.storage.model.dashboard.mappers.toDomain
import com.marketlabs.pulse.storage.model.dashboard.mappers.toEntity
import com.marketlabs.pulse.storage.model.weeklyPlaybook.WeeklyPlaybook
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LocalWeeklyPlaybookDataSourceImpl @Inject constructor(
    private val dao: WeeklyPlaybookDao
) : LocalWeeklyPlaybookDataSource {

    override fun getLatestPlaybookStream(): Flow<WeeklyPlaybook?> {
        return dao.getLatestPlaybook().map { it?.toDomain() }
    }

    override suspend fun savePlaybook(playbook: WeeklyPlaybook) {
        dao.insertPlaybook(playbook.toEntity())
    }

    /**
     * Retrieves the last synced timestamp. Returns null if a playbook has not been saved yet.
     */
    override suspend fun getLastSyncedTimestamp(): Long? {
        return dao.getLastSyncedTimestamp()
    }

    /**
     * Updates the last synced timestamp in the local database.
     */
    override suspend fun updateLastSyncedTimestamp(timestamp: Long) {
        dao.updateLastSyncedTimestamp(timestamp)
    }
}