package com.marketlabs.pulse.storage.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.marketlabs.pulse.storage.database.converters.Converters
import com.marketlabs.pulse.storage.database.dao.MarketIndexDao
import com.marketlabs.pulse.storage.database.dao.MarketSummaryDao
import com.marketlabs.pulse.storage.database.entity.MarketIndexEntity
import com.marketlabs.pulse.storage.database.entity.MarketSummaryEntity

@Database(
    entities = [MarketIndexEntity::class, MarketSummaryEntity::class],
    version = 1,
    exportSchema = true // dumps schema to JSON for version control
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun marketIndexDao(): MarketIndexDao
    abstract fun marketSummaryDao() : MarketSummaryDao
}