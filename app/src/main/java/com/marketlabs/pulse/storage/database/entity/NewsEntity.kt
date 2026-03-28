package com.marketlabs.pulse.storage.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.marketlabs.pulse.storage.database.converters.NewsConverters
import com.marketlabs.pulse.storage.model.news.NewsArticle

@Entity(tableName = "market_news")
@TypeConverters(NewsConverters::class)
data class NewsEntity(
    @PrimaryKey(autoGenerate = false)
    val id: String = "latest",
    val lastSyncedTimestamp: Long,
    val lastUpdated: Long,
    val sourceCount: Int? = null,
    val stories: List<NewsArticle>? = null
)