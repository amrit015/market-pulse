package com.marketlabs.pulse.core.marketIndex

interface MarketIndexRepository {

    suspend fun refreshMarketIndicesData()
}