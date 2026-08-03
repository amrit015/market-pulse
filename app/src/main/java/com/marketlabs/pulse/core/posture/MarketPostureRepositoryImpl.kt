package com.marketlabs.pulse.core.posture

import android.util.Log
import com.marketlabs.pulse.network.store.posture.RemoteMarketPostureDataSource
import com.marketlabs.pulse.storage.model.posture.DomainMarketPosture
import com.marketlabs.pulse.storage.store.posture.LocalMarketPostureDataSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MarketPostureRepositoryImpl @Inject constructor(
    private val localDataSource: LocalMarketPostureDataSource,
    private val remoteDataSource: RemoteMarketPostureDataSource
) : MarketPostureRepository {

    override fun getPostureStream(): Flow<DomainMarketPosture?> = localDataSource.getLatestPostureStream()

    override suspend fun refreshPosture(force: Boolean): Result<Unit> {
        return try {
            Log.d("MarketPosture", "🌐 Fetching latest Posture from backend...")

            remoteDataSource.getLatestPosture().onSuccess { freshPosture ->
                localDataSource.savePosture(freshPosture)
            }.onFailure {
                throw it
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("MarketPosture", "Failed to refresh posture", e)
            Result.failure(e)
        }
    }

    override suspend fun getLastSyncedTimestamp(): Long? {
        return localDataSource.getLastSyncedTimestamp()
    }

    override suspend fun updateLastSyncedTimestamp(timestamp: Long) {
        localDataSource.updateLastSyncedTimestamp(timestamp)
    }
}