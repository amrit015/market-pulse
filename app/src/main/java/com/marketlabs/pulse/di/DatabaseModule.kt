package com.marketlabs.pulse.di

import android.content.Context
import androidx.room.Room
import com.marketlabs.pulse.storage.database.AppDatabase
import com.marketlabs.pulse.storage.database.dao.IndicatorsDao
import com.marketlabs.pulse.storage.database.dao.MarketIndexDao
import com.marketlabs.pulse.storage.database.dao.NewsDao
import com.marketlabs.pulse.storage.database.dao.RiskRadarDao
import com.marketlabs.pulse.storage.database.dao.SummaryDao
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
    fun provideMarketIndexDao(database: AppDatabase): MarketIndexDao {
        return database.marketIndexDao()
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
    fun provideRiskRadarDao(database: AppDatabase): RiskRadarDao {
        return database.riskRadarDao()
    }

    @Provides
    @Singleton
    fun provideIndicatorsDao(database: AppDatabase): IndicatorsDao {
        return database.indicatorsDao()
    }
}