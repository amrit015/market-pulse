package com.marketlabs.pulse.storage.store.pastReleases

import com.marketlabs.pulse.storage.model.pastReleases.PastReleases
import kotlinx.coroutines.flow.Flow

interface LocalPastReleasesDataSource {

    fun getLatestPastReleasesStream(): Flow<PastReleases?>
    suspend fun savePastReleases(pastReleases: PastReleases)
    suspend fun getLastSyncedTimestamp(): Long?
    suspend fun updateLastSyncedTimestamp(timestamp: Long)
}
