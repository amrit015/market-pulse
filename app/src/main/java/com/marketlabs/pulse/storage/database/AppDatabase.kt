package com.marketlabs.pulse.storage.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.marketlabs.pulse.storage.database.converters.IndicatorsConverters
import com.marketlabs.pulse.storage.database.converters.NewsConverters
import com.marketlabs.pulse.storage.database.converters.RiskConverters
import com.marketlabs.pulse.storage.database.converters.StocksConverters
import com.marketlabs.pulse.storage.database.converters.SummaryConverters
import com.marketlabs.pulse.storage.database.converters.WeeklyPlaybookConverters
import com.marketlabs.pulse.storage.database.dao.DashboardDao
import com.marketlabs.pulse.storage.database.dao.IndicatorsDao
import com.marketlabs.pulse.storage.database.dao.MarketPostureDao
import com.marketlabs.pulse.storage.database.dao.MarketRiskDao
import com.marketlabs.pulse.storage.database.dao.NewsDao
import com.marketlabs.pulse.storage.database.dao.StocksDao
import com.marketlabs.pulse.storage.database.dao.SummaryDao
import com.marketlabs.pulse.storage.database.dao.WeeklyPlaybookDao
import com.marketlabs.pulse.storage.database.entity.AssetOverviewEntity
import com.marketlabs.pulse.storage.database.entity.IndicatorsEntity
import com.marketlabs.pulse.storage.database.entity.MarketPostureEntity
import com.marketlabs.pulse.storage.database.entity.MarketPulseEntity
import com.marketlabs.pulse.storage.database.entity.MarketRiskEntity
import com.marketlabs.pulse.storage.database.entity.MarketStateEntity
import com.marketlabs.pulse.storage.database.entity.NewsEntity
import com.marketlabs.pulse.storage.database.entity.StockDetailEntity
import com.marketlabs.pulse.storage.database.entity.StockPreviewEntity
import com.marketlabs.pulse.storage.database.entity.WeeklyPlaybookEntity

// Includes registration of the stocks (market_stock_previews / market_stock_details) cache,
// added with Claude Code assistance.
@Database(
    entities = [
        MarketPulseEntity::class,
        NewsEntity::class,
        IndicatorsEntity::class,
        MarketStateEntity::class,
        AssetOverviewEntity::class,
        MarketRiskEntity::class,
        WeeklyPlaybookEntity::class,
        MarketPostureEntity::class,
        StockPreviewEntity::class,
        StockDetailEntity::class
    ],
    version = 15,
    exportSchema = true
)
@TypeConverters(
    RiskConverters::class,
    SummaryConverters::class,
    NewsConverters::class,
    IndicatorsConverters::class,
    WeeklyPlaybookConverters::class,
    StocksConverters::class
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun marketSummaryDao(): SummaryDao
    abstract fun marketNewsDao(): NewsDao
    abstract fun marketRiskDao(): MarketRiskDao
    abstract fun marketIndicatorsDao(): IndicatorsDao
    abstract fun dashboardDao(): DashboardDao
    abstract fun weeklyPlaybookDao(): WeeklyPlaybookDao
    abstract fun marketPostureDao(): MarketPostureDao

    /** DAO for the `market_stock_previews` / `market_stock_details` tables backing the stock analysis cache. */
    abstract fun stocksDao(): StocksDao
}