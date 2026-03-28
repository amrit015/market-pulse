package com.marketlabs.pulse.storage.model.news.mappers

import com.marketlabs.pulse.storage.database.entity.NewsEntity
import com.marketlabs.pulse.storage.model.news.MarketNews

// ============================================================================
// MAPPERS
// ============================================================================

fun MarketNews.toEntity(syncTime: Long = System.currentTimeMillis()): NewsEntity {
    return NewsEntity(
        id = "latest",
        lastSyncedTimestamp = syncTime,
        lastUpdated = this.lastUpdated,
        sourceCount = this.sourceCount,
        stories = this.stories?.sortedByDescending { it.timestamp }
    )
}

fun NewsEntity.toDomain(): MarketNews {
    return MarketNews(
        lastSyncedTimestamp = this.lastSyncedTimestamp,
        lastUpdated = this.lastUpdated,
        sourceCount = this.sourceCount,
        stories = this.stories?.sortedByDescending { it.timestamp }
    )
}