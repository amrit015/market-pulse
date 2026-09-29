package com.marketlabs.pulse.network.api

import com.marketlabs.pulse.network.model.pastReleases.NetworkPastReleasesResponse
import retrofit2.http.GET

interface PastReleasesApi {

    @GET("dashboard/past-releases")
    suspend fun getPastReleases(): NetworkPastReleasesResponse
}
