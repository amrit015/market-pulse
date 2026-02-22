package com.marketlabs.pulse.storage.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.marketlabs.pulse.storage.database.converters.Converters
import com.marketlabs.pulse.storage.database.converters.MarketSummaryConverters
import com.marketlabs.pulse.storage.database.converters.NewsConverters
import com.marketlabs.pulse.storage.database.dao.MarketIndexDao
import com.marketlabs.pulse.storage.database.dao.MarketSummaryDao
import com.marketlabs.pulse.storage.database.dao.NewsDao
import com.marketlabs.pulse.storage.database.entity.DailyPulseEntity
import com.marketlabs.pulse.storage.database.entity.MarketIndexEntity
import com.marketlabs.pulse.storage.database.entity.MarketNewsEntity
import com.marketlabs.pulse.storage.database.entity.MarketPulseEntity

@Database(
    // 👇 Ensure both entities are registered here
    entities = [
        MarketIndexEntity::class,
        MarketPulseEntity::class,
        DailyPulseEntity::class,
        MarketNewsEntity::class
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(Converters::class, MarketSummaryConverters::class, NewsConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun marketIndexDao(): MarketIndexDao
    abstract fun marketSummaryDao(): MarketSummaryDao
    abstract fun marketNewsDao(): NewsDao

}