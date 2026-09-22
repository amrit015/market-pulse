package com.marketlabs.pulse.core.sync

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.marketlabs.pulse.core.indicators.IndicatorsRepository
import com.marketlabs.pulse.core.marketRisk.MarketRiskRepository
import com.marketlabs.pulse.core.news.NewsRepository
import com.marketlabs.pulse.core.positioning.MarketPositioningRepository
import com.marketlabs.pulse.core.posture.MarketPostureRepository
import com.marketlabs.pulse.core.stocks.StockAnalysisRepository
import com.marketlabs.pulse.core.summary.SummaryRepository
import com.marketlabs.pulse.core.weeklyPlaybook.WeeklyPlaybookRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncManager @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val playbookRepository: WeeklyPlaybookRepository,
    private val indicatorsRepository: IndicatorsRepository,
    private val newsRepository: NewsRepository,
    private val marketRiskRepository: MarketRiskRepository,
    private val summaryRepository: SummaryRepository,
    private val postureRepository: MarketPostureRepository,
    private val positioningRepository: MarketPositioningRepository,
    private val stockAnalysisRepository: StockAnalysisRepository
) {
    private var listenerRegistration: ListenerRegistration? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    // 💡 Unlike the 8 domains below (each push-triggers its own repository's refresh the moment its
    // flag advances), charts/history are per-symbol/per-metric and fetched on demand, not eagerly
    // for everything cached -- so `ChartsRepositoryImpl`/`MetricHistoryRepositoryImpl`/
    // `InsightsHistoryRepositoryImpl` instead pull the current value of the relevant flag here at
    // the moment they're asked to refresh, and compare it against their own per-item
    // `lastSyncedTimestamp`. A flag absent from the map (not yet fired since this client last
    // attached the listener, e.g. right after this flag was first deployed) is treated as "unknown,
    // not fresh" by every caller, never as timestamp 0 -- see the `mapNotNull` below.
    private val _chartSyncTimestamps = MutableStateFlow<Map<String, Long>>(emptyMap())
    val chartSyncTimestamps: StateFlow<Map<String, Long>> = _chartSyncTimestamps.asStateFlow()

    fun startListening() {
        if (listenerRegistration != null) return

        listenerRegistration = firestore.collection("system").document("sync_status")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("SyncManager", "Failed to listen to sync_status", error)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    _chartSyncTimestamps.value = CHART_SYNC_FLAG_KEYS.mapNotNull { key ->
                        snapshot.getLong(key)?.let { key to it }
                    }.toMap()

                    scope.launch {
                        // ==========================================
                        // 1. PLAYBOOK SYNC
                        // ==========================================
                        val newPlaybookTime = snapshot.getLong("weekly_playbook_updated") ?: 0L
                        // Check for the mid-week AI actuals update
                        val newPlaybookActualsTime = snapshot.getLong("weekly_playbook_actuals_updated") ?: 0L

                        // Take the latest of either the Sunday generation or Mid-Week update
                        val maxPlaybookTime = maxOf(newPlaybookTime, newPlaybookActualsTime)
                        val localPlaybookTime = playbookRepository.getLastSyncedTimestamp() ?: 0L

                        if (maxPlaybookTime > localPlaybookTime) {
                            Log.d("SyncManager", "New Playbook (or Actuals) detected! Fetching...")
                            playbookRepository.refreshPlaybook(force = true)
                            playbookRepository.updateLastSyncedTimestamp(maxPlaybookTime)
                        }

                        // ==========================================
                        // 2. INDICATORS (FOUR PILLARS + AI) SYNC
                        // ==========================================
                        // 💡 FIX: Updated to listen for the new Monolithic Engine & AI Synthesis
                        val masterIngestionTime = snapshot.getLong("master_ingestion_updated") ?: 0L
                        val aiSynthesisTime = snapshot.getLong("indicator_synthesis_updated") ?: 0L

                        // Take the latest of either the raw math ingestion or the AI Synthesis
                        val maxIndicatorTime = maxOf(masterIngestionTime, aiSynthesisTime)
                        val localIndicatorTime = indicatorsRepository.getLastSyncedTimestamp() ?: 0L

                        if (maxIndicatorTime > localIndicatorTime) {
                            Log.d("SyncManager", "New Indicators or AI Synthesis detected! Fetching...")
                            indicatorsRepository.refreshIndicators(force = true)
                            indicatorsRepository.updateLastSyncedTimestamp(maxIndicatorTime)
                        }

                        // ==========================================
                        // 3. NEWS SYNC
                        // ==========================================
                        val newNewsTime = snapshot.getLong("market_news_updated") ?: 0L
                        val localNewsTime = newsRepository.getLastSyncedTimestamp() ?: 0L

                        if (newNewsTime > localNewsTime) {
                            Log.d("SyncManager", "New Market News detected! Fetching...")
                            newsRepository.refreshNews(force = true)
                            newsRepository.updateLastSyncedTimestamp(newNewsTime)
                        }

                        // ==========================================
                        // 4. MARKET TAIL RISKS (AI) SYNC
                        // ==========================================
                        val newTailRisksTime = snapshot.getLong("market_risks_updated") ?: 0L
                        val localTailRisksTime = marketRiskRepository.getLastSyncedTimestampTailRisks() ?: 0L

                        if (newTailRisksTime > localTailRisksTime) {
                            Log.d("SyncManager", "New AI Tail Risks detected! Fetching...")
                            marketRiskRepository.refreshTailRisks(force = true)
                            marketRiskRepository.updateLastSyncedTimestampTailRisks(newTailRisksTime)
                        }

                        // ==========================================
                        // 5. MARKET PULSE (SUMMARY) SYNC
                        // ==========================================
                        val newPulseTime = snapshot.getLong("market_pulse_updated") ?: 0L
                        val localPulseTime = summaryRepository.getLastSyncedTimestamp() ?: 0L

                        if (newPulseTime > localPulseTime) {
                            Log.d("SyncManager", "New AI Market Pulse detected! Fetching...")
                            summaryRepository.refreshMarketSummary(force = true)
                            summaryRepository.updateLastSyncedTimestamp(newPulseTime)
                        }

                        // ==========================================
                        // 6. MARKET POSTURE SYNC (NEW)
                        // ==========================================
                        val newPostureTime = snapshot.getLong("market_posture_updated") ?: 0L
                        val localPostureTime = postureRepository.getLastSyncedTimestamp() ?: 0L

                        if (newPostureTime > localPostureTime) {
                            Log.d("SyncManager", "New Market Posture detected! Fetching...")
                            postureRepository.refreshPosture(force = true)
                            postureRepository.updateLastSyncedTimestamp(newPostureTime)
                        }

                        // ==========================================
                        // 7. MARKET POSITIONING SYNC (NEW) -- retail sentiment / COT / short interest
                        // ==========================================
                        val newPositioningTime = snapshot.getLong("market_positioning_updated") ?: 0L
                        val localPositioningTime = positioningRepository.getLastSyncedTimestamp() ?: 0L

                        if (newPositioningTime > localPositioningTime) {
                            Log.d("SyncManager", "New Market Positioning detected! Fetching...")
                            positioningRepository.refreshPositioning(force = true)
                            positioningRepository.updateLastSyncedTimestamp(newPositioningTime)
                        }

                        // ==========================================
                        // 8. STOCK ANALYSIS SYNC (previews only — detail is fetched on demand)
                        // ==========================================
                        // 💡 A single `stocks_updated` flag, fired once per run by the stock-analysis
                        // hub's completion check rather than by every individual worker, drives this
                        // sync. Detail documents aren't covered by any sync flag; they're fetched
                        // fresh whenever a symbol is opened (see StockAnalysisRepository.refreshDetail).
                        val newStocksTime = snapshot.getLong("stocks_updated") ?: 0L
                        val localStocksTime = stockAnalysisRepository.getLastSyncedTimestamp() ?: 0L

                        if (newStocksTime > localStocksTime) {
                            Log.d("SyncManager", "New Stock Analysis detected! Fetching...")
                            stockAnalysisRepository.refreshPreviews(force = true)
                            stockAnalysisRepository.updateLastSyncedTimestamp(newStocksTime)
                        }
                    }
                }
            }
    }

    fun stopListening() {
        listenerRegistration?.remove()
        listenerRegistration = null
    }

    private companion object {
        /** The 8 chart/history flags added alongside the 8 domain flags above -- pulled on demand
         *  by `ChartSyncGroup`-aware callers rather than push-triggering anything here. */
        val CHART_SYNC_FLAG_KEYS = listOf(
            "charts_stocks_updated",
            "charts_equity_sector_updated",
            "charts_sentiment_updated",
            "charts_futures_commodities_updated",
            "charts_crypto_updated",
            "indicator_charts_updated",
            "posture_charts_updated",
            "positioning_charts_updated"
        )
    }
}