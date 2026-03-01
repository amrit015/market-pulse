package com.marketlabs.pulse.network.store.dashboard

import com.marketlabs.pulse.storage.database.entity.AssetOverviewEntity
import com.marketlabs.pulse.storage.database.entity.MarketStateEntity

interface RemoteDashboardDataSource {

    suspend fun fetchDashboardData(): Result<Pair<MarketStateEntity, List<AssetOverviewEntity>>>
}