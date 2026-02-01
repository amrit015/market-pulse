package com.marketlabs.pulse.network.store

import com.google.firebase.firestore.FirebaseFirestore
import com.marketlabs.pulse.network.api.FinnHubService
import com.marketlabs.pulse.network.model.finnhub.QuoteResponse
import com.marketlabs.pulse.network.model.firestore.AnalysisDto
import com.marketlabs.pulse.utils.Constants.COLLECTION_MARKET_DATA
import com.marketlabs.pulse.utils.Constants.DOC_ANALYSIS
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Handles all raw network calls (Firestore & REST API).
 * Returns nullable types to handle failures gracefully.
 */
class RemoteMarketDataSourceImpl @Inject constructor(
    private val finnHubService: FinnHubService,
    private val firestore: FirebaseFirestore,
) : RemoteMarketDataSource {

    /**
     * Fetches the "Master List" of analyzed stocks from Firestore.
     * Maps the raw Firestore Map<String, Any> into a clean List<AnalysisDto>.
     */
    override suspend fun getRemoteMarketAnalysis(): List<AnalysisDto> {
        return try {
            val snapshot = firestore.collection(COLLECTION_MARKET_DATA)
                .document(DOC_ANALYSIS)
                .get()
                .await()

            // Firestore stores tickers as a Map: "SPY": { ...data... }
            val tickersMap = snapshot.get("tickers") as? Map<String, Map<String, Any>> ?: emptyMap()

            tickersMap.values.map { item ->
                AnalysisDto(
                    symbol = item["symbol"] as? String ?: "",
                    rsi = (item["rsi"] as? Number)?.toDouble() ?: 0.0,
                    technicalStatus = item["technical_status"] as? String ?: "Neutral"
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Fetches the real-time price for a specific symbol.
     * Returns null if the API call fails (so we don't crash the whole batch).
     */
    override suspend fun getRemoteMarketIndexPrice(symbol: String): QuoteResponse? {
        return try {
            finnHubService.getQuote(symbol)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}