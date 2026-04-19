package com.marketlabs.pulse.di

import com.marketlabs.pulse.core.weeklyPlaybook.WeeklyPlaybookRepository
import com.marketlabs.pulse.core.weeklyPlaybook.WeeklyPlaybookRepositoryImpl
import com.marketlabs.pulse.network.store.weeklyPlaybook.RemoteWeeklyPlaybookDataSource
import com.marketlabs.pulse.network.store.weeklyPlaybook.RemoteWeeklyPlaybookDataSourceImpl
import com.marketlabs.pulse.storage.store.weeklyPlaybook.LocalWeeklyPlaybookDataSource
import com.marketlabs.pulse.storage.store.weeklyPlaybook.LocalWeeklyPlaybookDataSourceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WeeklyPlaybookModule {

    @Provides
    @Singleton
    fun provideRemoteWeeklyPlaybookDataSource(
        remoteDataSourceImpl: RemoteWeeklyPlaybookDataSourceImpl
    ): RemoteWeeklyPlaybookDataSource = remoteDataSourceImpl

    @Provides
    @Singleton
    fun provideLocalWeeklyPlaybookDataSource(
        localDataSourceImpl: LocalWeeklyPlaybookDataSourceImpl
    ): LocalWeeklyPlaybookDataSource = localDataSourceImpl

    @Provides
    @Singleton
    fun provideWeeklyPlaybookRepository(
        weeklyPlaybookRepositoryImpl: WeeklyPlaybookRepositoryImpl
    ): WeeklyPlaybookRepository = weeklyPlaybookRepositoryImpl
}