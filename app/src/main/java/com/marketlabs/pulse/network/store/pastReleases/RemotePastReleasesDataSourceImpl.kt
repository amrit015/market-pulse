package com.marketlabs.pulse.network.store.pastReleases

import android.util.Log
import com.marketlabs.pulse.network.api.PastReleasesApi
import com.marketlabs.pulse.storage.model.dashboard.mappers.toDomain
import com.marketlabs.pulse.storage.model.pastReleases.PastReleases
import javax.inject.Inject

class RemotePastReleasesDataSourceImpl @Inject constructor(
    private val api: PastReleasesApi
) : RemotePastReleasesDataSource {

    override suspend fun getLatestPastReleases(): Result<PastReleases> {
        return try {
            val response = api.getPastReleases()
            Result.success(response.toDomain())
        } catch (e: Exception) {
            Log.e("PastReleases", "Failed to fetch remote past releases", e)
            Result.failure(e)
        }
    }
}
