package com.marketlabs.pulse.network.store.indicators

import com.marketlabs.pulse.storage.model.indicators.MarketIndicators

interface RemoteIndicatorsDataSource {

    suspend fun getLatestIndicators(dateId: String): Result<MarketIndicators>
}