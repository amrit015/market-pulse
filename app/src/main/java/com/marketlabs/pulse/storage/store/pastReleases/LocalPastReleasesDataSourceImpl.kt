package com.marketlabs.pulse.storage.store.pastReleases

import com.marketlabs.pulse.storage.database.dao.PastReleasesDao
import com.marketlabs.pulse.storage.model.dashboard.mappers.toDomain
import com.marketlabs.pulse.storage.model.dashboard.mappers.toEntity
import com.marketlabs.pulse.storage.model.pastReleases.PastReleases
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LocalPastReleasesDataSourceImpl @Inject constructor(
    private val dao: PastReleasesDao
) : LocalPastReleasesDataSource {

    override fun getLatestPastReleasesStream(): Flow<PastReleases?> {
        return dao.getLatestPastReleases().map { it?.toDomain() }
    }

    override suspend fun savePastReleases(pastReleases: PastReleases) {
        dao.insertPastReleases(pastReleases.toEntity())
    }

    override suspend fun getLastSyncedTimestamp(): Long? {
        return dao.getLastSyncedTimestamp()
    }

    override suspend fun updateLastSyncedTimestamp(timestamp: Long) {
        dao.updateLastSyncedTimestamp(timestamp)
    }
}
