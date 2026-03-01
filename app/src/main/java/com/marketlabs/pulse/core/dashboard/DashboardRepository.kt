package com.marketlabs.pulse.core.dashboard

import com.marketlabs.pulse.storage.model.dashboard.AssetOverview
import com.marketlabs.pulse.storage.model.dashboard.MarketState
import kotlinx.coroutines.flow.Flow

interface DashboardRepository {

    fun getMarketStateStream(): Flow<MarketState?>

    fun getDashboardAssetsStream(): Flow<List<AssetOverview?>>

    suspend fun refreshDashboard(force: Boolean = false)

    fun closeWebSockets()
}