package com.marketlabs.pulse.storage.store.weeklyPlaybook

import com.marketlabs.pulse.storage.model.weeklyPlaybook.WeeklyPlaybook
import kotlinx.coroutines.flow.Flow

interface LocalWeeklyPlaybookDataSource {

    fun getLatestPlaybookStream(): Flow<WeeklyPlaybook?>
    suspend fun savePlaybook(playbook: WeeklyPlaybook)
}