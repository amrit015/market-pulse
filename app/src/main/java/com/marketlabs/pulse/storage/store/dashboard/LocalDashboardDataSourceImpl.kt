package com.marketlabs.pulse.storage.store.dashboard

import com.marketlabs.pulse.storage.database.dao.DashboardDao
import com.marketlabs.pulse.storage.model.dashboard.AssetOverview
import com.marketlabs.pulse.storage.model.dashboard.MarketState
import com.marketlabs.pulse.storage.model.dashboard.mappers.toDomain
import com.marketlabs.pulse.storage.model.dashboard.mappers.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LocalDashboardDataSourceImpl @Inject constructor(
    private val dao: DashboardDao
) : LocalDashboardDataSource {

    override fun getMarketStateStream(): Flow<MarketState?> {
        return dao.getMarketState().map { it?.toDomain() }
    }

    override fun getDashboardAssetsStream(): Flow<List<AssetOverview>> {
        return dao.getDashboardAssets().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun saveDashboardData(state: MarketState, assets: List<AssetOverview>) {
        dao.updateDashboardData(
            state = state.toEntity(),
            assets = assets.map { it.toEntity() }
        )
    }
}