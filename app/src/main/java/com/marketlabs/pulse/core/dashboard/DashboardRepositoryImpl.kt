package com.marketlabs.pulse.core.dashboard

import com.marketlabs.pulse.network.store.dashboard.RemoteDashboardDataSourceImpl
import com.marketlabs.pulse.network.websockets.FinnhubWebSocketClient
import com.marketlabs.pulse.storage.model.dashboard.AssetOverview
import com.marketlabs.pulse.storage.model.dashboard.MarketState
import com.marketlabs.pulse.storage.model.dashboard.mappers.toDomain
import com.marketlabs.pulse.storage.store.dashboard.LocalDashboardDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DashboardRepositoryImpl @Inject constructor(
    private val localDataSource: LocalDashboardDataSource,
    private val remoteDataSource: RemoteDashboardDataSourceImpl,
    private val webSocketClient: FinnhubWebSocketClient
) : DashboardRepository {
    private val scope = CoroutineScope(Dispatchers.IO)

    // A fast, in-memory map of Symbol -> Latest Live Price
    private val livePriceMap = MutableStateFlow<Map<String, Double>>(emptyMap())

    init {
        scope.launch {
            webSocketClient.livePrices.collect { trade ->
                val currentMap = livePriceMap.value.toMutableMap()

                // 💡 Translate Finnhub's broker tickers back to your app's database tickers
                val cleanSymbol = when (trade.symbol) {
                    "BINANCE:BTCUSDT" -> "BTC-USD"
                    "OANDA:XAU_USD" -> "GC=F"
                    "BINANCE:ETHUSDT" -> "ETH-USD"
                    "OANDA:XAG_USD" -> "SI=F"
                    else -> trade.symbol.substringAfter(":") // Failsafe to strip any other prefixes
                }

                currentMap[cleanSymbol] = trade.price
                livePriceMap.value = currentMap
            }
        }
    }

    // 1. Get the Market State (Open/Closed)
    override fun getMarketStateStream(): Flow<MarketState?> = localDataSource.getMarketStateStream()

    // 2. The Magic Merger: Room + WebSockets
    override fun getDashboardAssetsStream(): Flow<List<AssetOverview?>> {
        return combine(
            localDataSource.getDashboardAssetsStream(),
            livePriceMap
        ) { cachedAssets, livePrices ->
            cachedAssets.map { asset ->
                val currentPrice = livePrices[asset.symbol] ?: asset.price
                val prevClose = asset.previousClose

                val liveChangePercent =
                    if (currentPrice != null && prevClose != null && prevClose > 0.0) {
                        ((currentPrice - prevClose) / prevClose) * 100
                    } else {
                        asset.changePercent
                    }

                // Cleanly copy the Domain Model with the new live values
                asset.copy(
                    price = currentPrice,
                    changePercent = liveChangePercent
                )
            }
        }
    }

    // 3. Trigger a network refresh
    override suspend fun refreshDashboard(force: Boolean) {
        val localState = localDataSource.getMarketStateStream().firstOrNull()
        val isExpired = localState?.lastUpdated == null ||
                (System.currentTimeMillis() - localState.lastUpdated) > (15 * 60 * 1000)

        if (!isExpired && !force) {
            val activeSymbols =
                localDataSource.getDashboardAssetsStream().firstOrNull()?.map { it.symbol }
                    ?: emptyList()
            connectAndSubscribe(activeSymbols)
            return
        }

        remoteDataSource.fetchDashboardData().onSuccess { (marketStateEntity, assetEntities) ->
            // Map the remote entities down to domain models
            val marketState = marketStateEntity.toDomain()
            val assets = assetEntities.map { it.toDomain() }

            // Save fresh AI data to Room via the clean Local Data Source
            localDataSource.saveDashboardData(marketState, assets)

            // Connect WebSocket and Subscribe to the tracked assets
            connectAndSubscribe(assets.map { it.symbol })
        }
    }

    private fun connectAndSubscribe(symbols: List<String>) {
        webSocketClient.connect()

        symbols.forEach { symbol ->
            when {
                // 1. Map Crypto to Binance
                symbol == "BTC-USD" -> webSocketClient.subscribe("BINANCE:BTCUSDT")
                symbol == "ETH-USD" -> webSocketClient.subscribe("BINANCE:ETHUSDT") // 💡 NEW

                // 2. Map Metals to OANDA Forex Spot Prices
                symbol == "GC=F" -> webSocketClient.subscribe("OANDA:XAU_USD") // Gold
                symbol == "SI=F" -> webSocketClient.subscribe("OANDA:XAG_USD") // Silver

                // 3. Ignore assets Finnhub doesn't support
                symbol.contains("=") || symbol.contains("^") ||
                        symbol == "FEAR_GREED" || symbol == "PUT_CALL" -> {
                    // Do nothing. 15-min backend updates handle these.
                }

                // 4. Subscribe to standard Equities normally (SPY, QQQ, DIA, MAGS)
                else -> {
                    webSocketClient.subscribe(symbol)
                }
            }
        }
    }

    override fun closeWebSockets() {
        webSocketClient.disconnect()
        livePriceMap.value = emptyMap()
    }
}