package com.marketlabs.pulse.di

import com.marketlabs.pulse.core.charts.ChartsRepository
import com.marketlabs.pulse.core.charts.ChartsRepositoryImpl
import com.marketlabs.pulse.network.store.charts.RemoteChartDataSource
import com.marketlabs.pulse.network.store.charts.RemoteChartDataSourceImpl
import com.marketlabs.pulse.storage.store.charts.LocalChartDataSource
import com.marketlabs.pulse.storage.store.charts.LocalChartDataSourceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Binds the charts domain's Remote/Local data sources and repository, matching `StockModule`'s 3-provider shape. */
@Module
@InstallIn(SingletonComponent::class)
object ChartsModule {

    @Provides
    @Singleton
    fun provideRemoteChartDataSource(
        remoteDataSourceImpl: RemoteChartDataSourceImpl
    ): RemoteChartDataSource = remoteDataSourceImpl

    @Provides
    @Singleton
    fun provideLocalChartDataSource(
        localDataSourceImpl: LocalChartDataSourceImpl
    ): LocalChartDataSource = localDataSourceImpl

    @Provides
    @Singleton
    fun provideChartsRepository(
        chartsRepositoryImpl: ChartsRepositoryImpl
    ): ChartsRepository = chartsRepositoryImpl
}
