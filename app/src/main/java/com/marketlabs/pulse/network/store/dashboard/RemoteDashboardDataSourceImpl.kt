package com.marketlabs.pulse.network.store.dashboard

import android.util.Log
import com.marketlabs.pulse.network.api.DashboardApi
import com.marketlabs.pulse.storage.database.entity.AssetOverviewEntity
import com.marketlabs.pulse.storage.database.entity.MarketStateEntity
import com.marketlabs.pulse.storage.model.dashboard.mappers.toEntity
import javax.inject.Inject

class RemoteDashboardDataSourceImpl @Inject constructor(
    private val api: DashboardApi
) : RemoteDashboardDataSource {

    override suspend fun fetchDashboardData(): Result<Pair<MarketStateEntity, List<AssetOverviewEntity>>> {
        return try {
            // 1. Fetch the data from your REST endpoint
            val response = api.getDashboardOverview()

            // 2. Map the Network models to Room Entities
            val marketStateEntity = response.marketState.toEntity()
            val assetsEntities = response.assets.map { it.toEntity() }

            // 3. Return the payload for the Repository to save to Room
            Result.success(Pair(marketStateEntity, assetsEntities))
        } catch (e: Exception) {
            Log.e("Dashboard", "Failed to fetch dashboard data from API", e)
            Result.failure(e)
        }
    }
}