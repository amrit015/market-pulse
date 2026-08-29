package com.marketlabs.pulse.core.positioning

import android.util.Log
import com.marketlabs.pulse.network.store.positioning.RemoteMarketPositioningDataSource
import com.marketlabs.pulse.storage.model.positioning.DomainMarketPositioning
import com.marketlabs.pulse.storage.store.positioning.LocalMarketPositioningDataSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MarketPositioningRepositoryImpl @Inject constructor(
    private val localDataSource: LocalMarketPositioningDataSource,
    private val remoteDataSource: RemoteMarketPositioningDataSource
) : MarketPositioningRepository {

    override fun getPositioningStream(): Flow<DomainMarketPositioning?> = localDataSource.getLatestPositioningStream()

    override suspend fun refreshPositioning(force: Boolean): Result<Unit> {
        return try {
            Log.d("MarketPositioning", "🌐 Fetching latest Positioning from backend...")

            remoteDataSource.getLatestPositioning().onSuccess { freshPositioning ->
                localDataSource.savePositioning(freshPositioning)
            }.onFailure {
                throw it
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("MarketPositioning", "Failed to refresh positioning", e)
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
