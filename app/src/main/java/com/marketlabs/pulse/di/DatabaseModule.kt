package com.marketlabs.pulse.di

import android.content.Context
import androidx.room.Room
import com.marketlabs.pulse.storage.database.AppDatabase
import com.marketlabs.pulse.storage.database.dao.ChartsDao
import com.marketlabs.pulse.storage.database.dao.MetricHistoryDao
import com.marketlabs.pulse.storage.database.dao.DashboardDao
import com.marketlabs.pulse.storage.database.dao.IndicatorsDao
import com.marketlabs.pulse.storage.database.dao.MarketPostureDao
import com.marketlabs.pulse.storage.database.dao.MarketRiskDao
import com.marketlabs.pulse.storage.database.dao.NewsDao
import com.marketlabs.pulse.storage.database.dao.StocksDao
import com.marketlabs.pulse.storage.database.dao.SummaryDao
import com.marketlabs.pulse.storage.database.dao.WeeklyPlaybookDao
import com.marketlabs.pulse.storage.database.migrations.DatabaseMigrations
import com.marketlabs.pulse.utils.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder<AppDatabase>(
            context,
            Constants.DATABASE_NAME
        )
            // Attach custom migrations
            .addMigrations(*DatabaseMigrations.ALL_MIGRATIONS)
            .build()
    }

    @Provides
    fun provideMarketSummaryDao(database: AppDatabase): SummaryDao {
        return database.marketSummaryDao()
    }

    @Provides
    @Singleton
    fun provideNewsDao(database: AppDatabase): NewsDao {
        return database.marketNewsDao()
    }

    @Provides
    @Singleton
    fun provideMarketRiskDao(database: AppDatabase): MarketRiskDao {
        return database.marketRiskDao()
    }

    @Provides
    @Singleton
    fun provideIndicatorsDao(database: AppDatabase): IndicatorsDao {
        return database.marketIndicatorsDao()
    }

    @Provides
    @Singleton
    fun provideDashboardDao(database: AppDatabase): DashboardDao {
        return database.dashboardDao()
    }

    @Provides
    @Singleton
    fun provideWeeklyPlaybookDao(database: AppDatabase): WeeklyPlaybookDao {
        return database.weeklyPlaybookDao()
    }

    @Provides
    fun provideMarketPostureDao(database: AppDatabase): MarketPostureDao {
        return database.marketPostureDao()
    }

    /** Provides the DAO for the stocks domain's `market_stocks` Room cache. */
    @Provides
    @Singleton
    fun provideStocksDao(database: AppDatabase): StocksDao {
        return database.stocksDao()
    }

    /** Provides the DAO for the `market_charts` table backing period charts. */
    @Provides
    @Singleton
    fun provideChartsDao(database: AppDatabase): ChartsDao {
        return database.chartsDao()
    }

    /** Provides the DAO for the `metric_history` table backing the indicator detail page's history chart. */
    @Provides
    @Singleton
    fun provideMetricHistoryDao(database: AppDatabase): MetricHistoryDao {
        return database.metricHistoryDao()
    }
}