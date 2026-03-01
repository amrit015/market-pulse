package com.marketlabs.pulse.storage.store.dashboard

import com.marketlabs.pulse.storage.model.dashboard.AssetOverview
import com.marketlabs.pulse.storage.model.dashboard.MarketState
import kotlinx.coroutines.flow.Flow

interface LocalDashboardDataSource {
    fun getMarketStateStream(): Flow<MarketState?>
    fun getDashboardAssetsStream(): Flow<List<AssetOverview>>
    suspend fun saveDashboardData(state: MarketState, assets: List<AssetOverview>)
}