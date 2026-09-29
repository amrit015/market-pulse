package com.marketlabs.pulse.core.pastReleases

import com.marketlabs.pulse.storage.model.pastReleases.PastReleases
import kotlinx.coroutines.flow.Flow

interface PastReleasesRepository {

    fun getPastReleasesStream(): Flow<PastReleases?>
    suspend fun refreshPastReleases(force: Boolean): Result<Unit>
    suspend fun getLastSyncedTimestamp(): Long?
    suspend fun updateLastSyncedTimestamp(timestamp: Long)
}
