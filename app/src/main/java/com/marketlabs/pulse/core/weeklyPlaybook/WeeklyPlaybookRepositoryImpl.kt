package com.marketlabs.pulse.core.weeklyPlaybook

import android.util.Log
import com.marketlabs.pulse.network.store.weeklyPlaybook.RemoteWeeklyPlaybookDataSource
import com.marketlabs.pulse.storage.model.weeklyPlaybook.WeeklyPlaybook
import com.marketlabs.pulse.storage.store.weeklyPlaybook.LocalWeeklyPlaybookDataSource
import com.marketlabs.pulse.utils.CachePolicy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class WeeklyPlaybookRepositoryImpl @Inject constructor(
    private val localDataSource: LocalWeeklyPlaybookDataSource,
    private val remoteDataSource: RemoteWeeklyPlaybookDataSource
) : WeeklyPlaybookRepository {

    override fun getPlaybookStream(): Flow<WeeklyPlaybook?> = localDataSource.getLatestPlaybookStream()

    override suspend fun refreshPlaybook(force: Boolean): Result<Unit> {
        return try {
            val localData = localDataSource.getLatestPlaybookStream().firstOrNull()
            val currentTime = System.currentTimeMillis()

            val shouldFetch = when {
                force -> true
                localData?.lastSyncedTimestamp == null -> true
                else -> CachePolicy.isHourlyExpired(localData.lastSyncedTimestamp, currentTime)
            }

            if (!shouldFetch) {
                Log.d("WeeklyPlaybook", "✅ Playbook cache is fresh. Skipping network.")
                return Result.success(Unit)
            }

            Log.d("WeeklyPlaybook", "🌐 Fetching latest Playbook from backend...")

            remoteDataSource.getLatestPlaybook().onSuccess { freshPlaybook ->
                localDataSource.savePlaybook(freshPlaybook)
            }.onFailure {
                throw it
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("WeeklyPlaybook", "Failed to refresh playbook", e)
            Result.failure(e)
        }
    }
}