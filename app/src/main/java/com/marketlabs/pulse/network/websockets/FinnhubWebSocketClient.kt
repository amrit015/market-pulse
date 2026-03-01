package com.marketlabs.pulse.network.websockets

import android.util.Log
import com.marketlabs.pulse.network.model.dashboard.FinnhubSubscribeRequest
import com.marketlabs.pulse.network.model.dashboard.FinnhubTradeData
import com.marketlabs.pulse.network.model.dashboard.FinnhubTradeResponse
import com.marketlabs.pulse.utils.Constants
import com.squareup.moshi.Moshi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class FinnhubWebSocketClient @Inject constructor(
    @Named("FinnHubWebSocketClient") private val okHttpClient: OkHttpClient,
    private val moshi: Moshi
) : WebSocketListener() {

    private val wsUrl = Constants.FINNHUB_TOKEN_ENDPOINT

    private var webSocket: WebSocket? = null
    private val activeSymbols = mutableSetOf<String>()

    // The coroutine scope to emit parsed data to the Flow
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // 💡 The Flow that your Repository will listen to for live prices
    private val _livePrices = MutableSharedFlow<FinnhubTradeData>(extraBufferCapacity = 50)
    val livePrices = _livePrices.asSharedFlow()

    private val tradeResponseAdapter = moshi.adapter(FinnhubTradeResponse::class.java)
    private val subscribeRequestAdapter = moshi.adapter(FinnhubSubscribeRequest::class.java)

    fun connect() {
        if (webSocket != null) return // Already connected

        Log.d("FinnhubWS", "Connecting to WebSocket...")
        val request = Request.Builder().url(wsUrl).build()
        webSocket = okHttpClient.newWebSocket(request, this)
    }

    fun disconnect() {
        Log.d("FinnhubWS", "Disconnecting WebSocket...")
        webSocket?.close(1000, "User closed dashboard")
        webSocket = null
    }

    /**
     * Call this from your Repository to start tracking an asset (e.g., "BINANCE:BTCUSDT" or "AAPL")
     */
    fun subscribe(symbol: String) {
        activeSymbols.add(symbol)
        sendSubscription(symbol, "subscribe")
    }

    fun unsubscribe(symbol: String) {
        activeSymbols.remove(symbol)
        sendSubscription(symbol, "unsubscribe")
    }

    private fun sendSubscription(symbol: String, type: String) {
        val request = FinnhubSubscribeRequest(type = type, symbol = symbol)
        val json = subscribeRequestAdapter.toJson(request)
        webSocket?.send(json)
        Log.d("FinnhubWS", "Sent: $json")
    }

    // ==========================================
    // 🎧 OKHTTP WEBSOCKET LISTENER CALLBACKS
    // ==========================================

    override fun onOpen(webSocket: WebSocket, response: Response) {
        Log.d("FinnhubWS", "WebSocket Opened!")
        // If we reconnected, we need to resubscribe to the symbols we care about
        activeSymbols.forEach { symbol ->
            sendSubscription(symbol, "subscribe")
        }
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        try {
            // Parse the incoming JSON stream
            val response = tradeResponseAdapter.fromJson(text)

            if (response?.type == "trade" && response.data != null) {
                // Emit each individual trade down our Flow
                scope.launch {
                    response.data.forEach { trade ->
                        // 💡 NEW: Log the live price tick to Logcat
//                        Log.d("FinnhubWS", "Live Tick -> ${trade.symbol}: $${trade.price}")

                        _livePrices.emit(trade)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("FinnhubWS", "Failed to parse message: $text", e)
        }
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        Log.d("FinnhubWS", "WebSocket Closed: $reason")
        this.webSocket = null
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        Log.e("FinnhubWS", "WebSocket Failure", t)
        this.webSocket = null

        // Simple auto-reconnect logic if the network drops
        scope.launch {
            delay(5000) // Wait 5 seconds before trying again
            connect()
        }
    }
}