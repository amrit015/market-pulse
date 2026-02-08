package com.marketlabs.pulse.network.store.marketIndex

import com.marketlabs.pulse.network.model.finnhub.QuoteResponse
import com.marketlabs.pulse.network.model.firestore.AnalysisDto

interface RemoteMarketDataSource {

    suspend fun getRemoteMarketAnalysis(): List<AnalysisDto>

    suspend fun getRemoteMarketIndexPrice(symbol: String) : QuoteResponse?
}