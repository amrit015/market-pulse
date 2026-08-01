package com.marketlabs.pulse.core.sync

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.marketlabs.pulse.core.indicators.IndicatorsRepository
import com.marketlabs.pulse.core.marketRisk.MarketRiskRepository
import com.marketlabs.pulse.core.news.NewsRepository
import com.marketlabs.pulse.core.summary.SummaryRepository
import com.marketlabs.pulse.core.weeklyPlaybook.WeeklyPlaybookRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
    private val summaryRepository: SummaryRepository
) {
    private var listenerRegistration: ListenerRegistration? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    fun startListening() {
        if (listenerRegistration != null) return

        listenerRegistration = firestore.collection("system").document("sync_status")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("SyncManager", "Failed to listen to sync_status", error)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
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
                        // 2. INDICATORS (THREE PILLARS) SYNC
                        // ==========================================
                        val newVitalsTime = snapshot.getLong("macro_vitals_updated") ?: 0L
                        val newActionTime = snapshot.getLong("market_action_updated") ?: 0L
                        val newPhaseTime = snapshot.getLong("market_phase_updated") ?: 0L

                        val maxIndicatorTime = maxOf(newVitalsTime, newActionTime, newPhaseTime)
                        val localIndicatorTime = indicatorsRepository.getLastSyncedTimestamp() ?: 0L

                        if (maxIndicatorTime > localIndicatorTime) {
                            Log.d("SyncManager", "New Indicator Pillars detected! Fetching...")
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
                    }
                }
            }
    }

    fun stopListening() {
        listenerRegistration?.remove()
        listenerRegistration = null
    }
}