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
        // 1. Listen to Finnhub WebSockets for live equity ticks
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

        // 💡 2. NEW: Listen to Firebase Real-Time Dashboard Updates!
        scope.launch {
            remoteDataSource.observeDashboardData().collect { (marketStateEntity, assetEntities) ->
                val marketState = marketStateEntity.toDomain()
                val assets = assetEntities.map { it.toDomain() }

                // Automatically save fresh Firebase data to Room.
                // Because getDashboardAssetsStream() watches Room, the UI updates instantly!
                localDataSource.saveDashboardData(marketState, assets)

                // Ensure Finnhub is subscribed to the active symbols
                connectAndSubscribe(assets.map { it.symbol })
            }
        }
    }

    // 1. Get the Market State (Open/Closed)
    override fun getMarketStateStream(): Flow<MarketState?> = localDataSource.getMarketStateStream()

    // 2. The Magic Merger: Room (Firebase updates) + WebSockets (Finnhub live ticks)
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
        // 💡 Because Firebase is continuously streaming data into Room, we no longer need a manual network fetch here!
        // We just double-check that our Finnhub socket is successfully connected to the latest symbols.
        val activeSymbols = localDataSource.getDashboardAssetsStream().firstOrNull()?.map { it.symbol } ?: emptyList()
        connectAndSubscribe(activeSymbols)
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
                    // Do nothing. Firebase Real-Time Listener automatically handles these now!
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