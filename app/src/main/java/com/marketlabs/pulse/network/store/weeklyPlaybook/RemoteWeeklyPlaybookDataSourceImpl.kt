package com.marketlabs.pulse.network.store.weeklyPlaybook

import android.util.Log
import com.marketlabs.pulse.network.api.WeeklyPlaybookApi
import com.marketlabs.pulse.storage.model.dashboard.mappers.toDomain
import com.marketlabs.pulse.storage.model.weeklyPlaybook.WeeklyPlaybook
import javax.inject.Inject

class RemoteWeeklyPlaybookDataSourceImpl @Inject constructor(
    private val api: WeeklyPlaybookApi
) : RemoteWeeklyPlaybookDataSource {

    override suspend fun getLatestPlaybook(): Result<WeeklyPlaybook> {
        return try {
            val response = api.getWeeklyPlaybook()
            Result.success(response.toDomain())
        } catch (e: Exception) {
            Log.e("WeeklyPlaybook", "Failed to fetch remote playbook", e)
            Result.failure(e)
        }
    }
}