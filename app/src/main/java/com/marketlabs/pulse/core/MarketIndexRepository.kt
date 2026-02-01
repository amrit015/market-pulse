package com.marketlabs.pulse.core

interface MarketIndexRepository {

    suspend fun refreshMarketIndicesData()
}