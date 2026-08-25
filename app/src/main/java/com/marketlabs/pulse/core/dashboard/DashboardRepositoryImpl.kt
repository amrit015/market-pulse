package com.marketlabs.pulse.core.dashboard

import com.marketlabs.pulse.network.store.dashboard.RemoteDashboardDataSourceImpl
import com.marketlabs.pulse.storage.model.dashboard.AssetOverview
import com.marketlabs.pulse.storage.model.dashboard.MarketState
import com.marketlabs.pulse.storage.model.dashboard.mappers.toDomain
import com.marketlabs.pulse.storage.store.dashboard.LocalDashboardDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Live prices used to be a Finnhub WebSocket overlaid in-memory on top of the Firestore-cached
 * base list (see git history if you need the old shape) -- retired per the backend spec's Part D:
 * Finnhub only allows one connection per API key, so a single shared key never scaled past one
 * concurrent Overview-tab viewer. Live prices now come from the backend itself
 * (`refreshLiveDashboardPrices`, writing `price`/`previous_close`/`change_percent` to
 * `market_overview/{symbol}` every minute for the 23 symbols that used to be WS-covered,
 * everything else on the existing 15-minute cadence) straight through the Firestore listener
 * already wired up below -- no separate live-price merge step needed, Room already reflects
 * whatever Firestore has, and Firestore now gets written to more often.
 */
@Singleton
class DashboardRepositoryImpl @Inject constructor(
    private val localDataSource: LocalDashboardDataSource,
    private val remoteDataSource: RemoteDashboardDataSourceImpl
) : DashboardRepository {
    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        scope.launch {
            remoteDataSource.observeDashboardData().collect { (marketStateEntity, assetEntities) ->
                val marketState = marketStateEntity.toDomain()
                val assets = assetEntities.map { it.toDomain() }

                // Automatically save fresh Firestore data to Room -- getDashboardAssetsStream()
                // watches Room, so the UI updates instantly, including the now-per-minute price
                // writes for the live-priced symbol set.
                localDataSource.saveDashboardData(marketState, assets)
            }
        }
    }

    override fun getMarketStateStream(): Flow<MarketState?> = localDataSource.getMarketStateStream()

    override fun getDashboardAssetsStream(): Flow<List<AssetOverview?>> = localDataSource.getDashboardAssetsStream()

    /**
     * Nothing left to actually do -- the Firestore listener above runs for the app's lifetime
     * regardless of this call, so Room is already as fresh as it can be. Kept on the interface
     * (rather than removed) purely so `DashboardViewModel`'s existing pull-to-refresh /
     * first-paint-pre-warm wiring doesn't need to change shape for a genuinely no-op case.
     */
    override suspend fun refreshDashboard(force: Boolean) = Unit
}
