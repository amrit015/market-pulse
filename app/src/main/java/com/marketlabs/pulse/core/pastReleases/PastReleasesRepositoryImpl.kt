package com.marketlabs.pulse.core.pastReleases

import android.util.Log
import com.marketlabs.pulse.network.store.pastReleases.RemotePastReleasesDataSource
import com.marketlabs.pulse.storage.model.pastReleases.PastReleases
import com.marketlabs.pulse.storage.store.pastReleases.LocalPastReleasesDataSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PastReleasesRepositoryImpl @Inject constructor(
    private val localDataSource: LocalPastReleasesDataSource,
    private val remoteDataSource: RemotePastReleasesDataSource
) : PastReleasesRepository {

    override fun getPastReleasesStream(): Flow<PastReleases?> = localDataSource.getLatestPastReleasesStream()

    /**
     * Refreshes past releases from the network. There is no cache expiration: this method is
     * driven strictly by the SyncManager (which detects the weekly `past_releases_updated` flag)
     * or explicit user pull-to-refresh actions.
     */
    override suspend fun refreshPastReleases(force: Boolean): Result<Unit> {
        return try {
            Log.d("PastReleases", "🌐 Fetching latest Past Releases from backend...")

            remoteDataSource.getLatestPastReleases().onSuccess { freshPastReleases ->
                localDataSource.savePastReleases(freshPastReleases)
            }.onFailure {
                throw it
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("PastReleases", "Failed to refresh past releases", e)
            Result.failure(e)
        }
    }

    override suspend fun getLastSyncedTimestamp(): Long? {
        return localDataSource.getLastSyncedTimestamp()
    }

    override suspend fun updateLastSyncedTimestamp(timestamp: Long) {
        localDataSource.updateLastSyncedTimestamp(timestamp)
    }
}
