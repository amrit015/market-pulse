package com.marketlabs.pulse.network.store.posture

import com.marketlabs.pulse.storage.model.posture.DomainMarketPosture

interface RemoteMarketPostureDataSource {
    suspend fun getLatestPosture(): Result<DomainMarketPosture>
}