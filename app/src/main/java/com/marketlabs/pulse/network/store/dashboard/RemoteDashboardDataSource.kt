package com.marketlabs.pulse.network.store.dashboard

import com.marketlabs.pulse.storage.database.entity.AssetOverviewEntity
import com.marketlabs.pulse.storage.database.entity.MarketStateEntity
import kotlinx.coroutines.flow.Flow

interface RemoteDashboardDataSource {

    fun observeDashboardData(): Flow<Pair<MarketStateEntity, List<AssetOverviewEntity>>>
}