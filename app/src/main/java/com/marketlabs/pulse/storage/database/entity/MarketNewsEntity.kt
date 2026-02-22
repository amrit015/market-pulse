package com.marketlabs.pulse.storage.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.marketlabs.pulse.storage.database.converters.NewsConverters
import com.marketlabs.pulse.storage.model.news.MarketNews
import com.marketlabs.pulse.storage.model.news.NewsArticle

@Entity(tableName = "market_news")
@TypeConverters(NewsConverters::class)
data class MarketNewsEntity(
    @PrimaryKey(autoGenerate = false)
    val id: String = "latest",
    val lastSyncedTimestamp: Long,
    val sourceCount: Int? = null,
    val stories: List<NewsArticle>? = null
)

// ============================================================================
// MAPPERS
// ============================================================================

fun MarketNews.toEntity(syncTime: Long = System.currentTimeMillis()): MarketNewsEntity {
    return MarketNewsEntity(
        id = "latest",
        lastSyncedTimestamp = syncTime,
        sourceCount = this.sourceCount,
        stories = this.stories
    )
}

fun MarketNewsEntity.toDomain(): MarketNews {
    return MarketNews(
        lastSyncedTimestamp = this.lastSyncedTimestamp,
        sourceCount = this.sourceCount,
        stories = this.stories
    )
}