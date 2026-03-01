package com.marketlabs.pulse.storage.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.marketlabs.pulse.storage.database.converters.IndicatorsConverters
import com.marketlabs.pulse.storage.database.converters.NewsConverters
import com.marketlabs.pulse.storage.database.converters.RiskConverters
import com.marketlabs.pulse.storage.database.converters.SummaryConverters
import com.marketlabs.pulse.storage.database.dao.DashboardDao
import com.marketlabs.pulse.storage.database.dao.IndicatorsDao
import com.marketlabs.pulse.storage.database.dao.MarketIndexDao
import com.marketlabs.pulse.storage.database.dao.NewsDao
import com.marketlabs.pulse.storage.database.dao.RiskRadarDao
import com.marketlabs.pulse.storage.database.dao.SummaryDao
import com.marketlabs.pulse.storage.database.entity.AssetOverviewEntity
import com.marketlabs.pulse.storage.database.entity.DailyPulseEntity
import com.marketlabs.pulse.storage.database.entity.IndicatorsEntity
import com.marketlabs.pulse.storage.database.entity.MarketIndexEntity
import com.marketlabs.pulse.storage.database.entity.MarketPulseEntity
import com.marketlabs.pulse.storage.database.entity.MarketStateEntity
import com.marketlabs.pulse.storage.database.entity.NewsEntity
import com.marketlabs.pulse.storage.database.entity.RiskRadarEntity

@Database(
    entities = [
        MarketIndexEntity::class,
        MarketPulseEntity::class,
        DailyPulseEntity::class,
        NewsEntity::class,
        RiskRadarEntity::class,
        IndicatorsEntity::class,
        MarketStateEntity::class,
        AssetOverviewEntity::class
    ],
    version = 5,
    exportSchema = true
)
@TypeConverters(
    RiskConverters::class,
    SummaryConverters::class,
    NewsConverters::class,
    IndicatorsConverters::class
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun marketIndexDao(): MarketIndexDao
    abstract fun marketSummaryDao(): SummaryDao
    abstract fun marketNewsDao(): NewsDao
    abstract fun riskRadarDao(): RiskRadarDao
    abstract fun indicatorsDao(): IndicatorsDao
    abstract fun dashboardDao(): DashboardDao
}