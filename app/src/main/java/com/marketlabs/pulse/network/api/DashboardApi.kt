package com.marketlabs.pulse.network.api

import com.marketlabs.pulse.network.model.dashboard.NetworkDashboardResponse
import retrofit2.http.GET

interface DashboardApi {
    // 💡 Replace this with the actual path you plan to build on your backend
    @GET("dashboard/overview")
    suspend fun getDashboardOverview(): NetworkDashboardResponse
}