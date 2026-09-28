package com.marketlabs.pulse.network.store.pastReleases

import com.marketlabs.pulse.storage.model.pastReleases.PastReleases

interface RemotePastReleasesDataSource {

    suspend fun getLatestPastReleases(): Result<PastReleases>
}
