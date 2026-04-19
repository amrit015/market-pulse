package com.marketlabs.pulse.storage.store.weeklyPlaybook

import com.marketlabs.pulse.storage.database.dao.WeeklyPlaybookDao
import com.marketlabs.pulse.storage.model.weeklyPlaybook.WeeklyPlaybook
import com.marketlabs.pulse.storage.model.dashboard.mappers.toDomain
import com.marketlabs.pulse.storage.model.dashboard.mappers.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LocalWeeklyPlaybookDataSourceImpl @Inject constructor(
    private val dao: WeeklyPlaybookDao
) : LocalWeeklyPlaybookDataSource {

    override fun getLatestPlaybookStream(): Flow<WeeklyPlaybook?> {
        return dao.getLatestPlaybook().map { it?.toDomain() }
    }

    override suspend fun savePlaybook(playbook: WeeklyPlaybook) {
        dao.insertPlaybook(playbook.toEntity())
    }
}