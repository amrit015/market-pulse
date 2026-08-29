package com.marketlabs.pulse.network.store.positioning

import com.marketlabs.pulse.storage.model.positioning.DomainMarketPositioning

interface RemoteMarketPositioningDataSource {
    suspend fun getLatestPositioning(): Result<DomainMarketPositioning>
}
