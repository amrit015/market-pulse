package com.marketlabs.pulse.core.marketSummary

import com.marketlabs.pulse.storage.model.summary.MarketPulse
import kotlinx.coroutines.flow.Flow

interface MarketSummaryRepository {
    // 1. Passive Stream: Always returns whatever is in the DB (Fast, Offline-ready)
    fun getMarketSummaryStream(): Flow<MarketPulse?>

    // 2. Active Command: Fetches from API and updates DB
    // Returns Result so ViewModel can show errors (like "No Internet")
    suspend fun refreshMarketSummary(force: Boolean): Result<Unit>
}