package com.marketlabs.pulse.network.api

import com.marketlabs.pulse.network.model.weeklyPlaybook.NetworkWeeklyPlaybook
import retrofit2.http.GET

interface WeeklyPlaybookApi {

    @GET("dashboard/playbook")
    suspend fun getWeeklyPlaybook(): NetworkWeeklyPlaybook
}