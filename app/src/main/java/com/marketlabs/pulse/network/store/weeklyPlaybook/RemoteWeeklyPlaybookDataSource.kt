package com.marketlabs.pulse.network.store.weeklyPlaybook

import com.marketlabs.pulse.storage.model.weeklyPlaybook.WeeklyPlaybook

interface RemoteWeeklyPlaybookDataSource {

    suspend fun getLatestPlaybook(): Result<WeeklyPlaybook>
}