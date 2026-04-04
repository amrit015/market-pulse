package com.marketlabs.pulse.core.weeklyPlaybook

import com.marketlabs.pulse.storage.model.weeklyPlaybook.WeeklyPlaybook
import kotlinx.coroutines.flow.Flow

interface WeeklyPlaybookRepository {

    fun getPlaybookStream(): Flow<WeeklyPlaybook?>
    suspend fun refreshPlaybook(force: Boolean): Result<Unit>
}