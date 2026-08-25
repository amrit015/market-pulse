package com.marketlabs.pulse.di

import com.marketlabs.pulse.core.indicators.MetricHistoryRepository
import com.marketlabs.pulse.core.indicators.MetricHistoryRepositoryImpl
import com.marketlabs.pulse.network.store.indicators.RemoteMetricHistoryDataSource
import com.marketlabs.pulse.network.store.indicators.RemoteMetricHistoryDataSourceImpl
import com.marketlabs.pulse.storage.store.indicators.LocalMetricHistoryDataSource
import com.marketlabs.pulse.storage.store.indicators.LocalMetricHistoryDataSourceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Binds the metric-history domain's Remote/Local data sources and repository, matching `ChartsModule`'s 3-provider shape. */
@Module
@InstallIn(SingletonComponent::class)
object MetricHistoryModule {

    @Provides
    @Singleton
    fun provideRemoteMetricHistoryDataSource(
        remoteDataSourceImpl: RemoteMetricHistoryDataSourceImpl
    ): RemoteMetricHistoryDataSource = remoteDataSourceImpl

    @Provides
    @Singleton
    fun provideLocalMetricHistoryDataSource(
        localDataSourceImpl: LocalMetricHistoryDataSourceImpl
    ): LocalMetricHistoryDataSource = localDataSourceImpl

    @Provides
    @Singleton
    fun provideMetricHistoryRepository(
        metricHistoryRepositoryImpl: MetricHistoryRepositoryImpl
    ): MetricHistoryRepository = metricHistoryRepositoryImpl
}
