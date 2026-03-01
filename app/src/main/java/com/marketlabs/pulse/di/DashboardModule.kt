package com.marketlabs.pulse.di

import com.marketlabs.pulse.core.dashboard.DashboardRepository
import com.marketlabs.pulse.core.dashboard.DashboardRepositoryImpl
import com.marketlabs.pulse.network.store.dashboard.RemoteDashboardDataSource
import com.marketlabs.pulse.network.store.dashboard.RemoteDashboardDataSourceImpl
import com.marketlabs.pulse.storage.store.dashboard.LocalDashboardDataSource
import com.marketlabs.pulse.storage.store.dashboard.LocalDashboardDataSourceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DashboardModule {

    @Provides
    @Singleton
    fun provideRemoteDashboardDataSource(
        remoteDataSourceImpl: RemoteDashboardDataSourceImpl
    ): RemoteDashboardDataSource = remoteDataSourceImpl

    @Provides
    @Singleton
    fun provideLocalDashboardDataSource(
        localDataSourceImpl: LocalDashboardDataSourceImpl
    ): LocalDashboardDataSource = localDataSourceImpl

    @Provides
    @Singleton
    fun provideDashboardRepository(
        dashboardRepositoryImpl: DashboardRepositoryImpl
    ): DashboardRepository = dashboardRepositoryImpl
}