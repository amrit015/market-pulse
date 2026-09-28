package com.marketlabs.pulse.di

import com.marketlabs.pulse.core.pastReleases.PastReleasesRepository
import com.marketlabs.pulse.core.pastReleases.PastReleasesRepositoryImpl
import com.marketlabs.pulse.network.store.pastReleases.RemotePastReleasesDataSource
import com.marketlabs.pulse.network.store.pastReleases.RemotePastReleasesDataSourceImpl
import com.marketlabs.pulse.storage.store.pastReleases.LocalPastReleasesDataSource
import com.marketlabs.pulse.storage.store.pastReleases.LocalPastReleasesDataSourceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PastReleasesModule {

    @Provides
    @Singleton
    fun provideRemotePastReleasesDataSource(
        remoteDataSourceImpl: RemotePastReleasesDataSourceImpl
    ): RemotePastReleasesDataSource = remoteDataSourceImpl

    @Provides
    @Singleton
    fun provideLocalPastReleasesDataSource(
        localDataSourceImpl: LocalPastReleasesDataSourceImpl
    ): LocalPastReleasesDataSource = localDataSourceImpl

    @Provides
    @Singleton
    fun providePastReleasesRepository(
        pastReleasesRepositoryImpl: PastReleasesRepositoryImpl
    ): PastReleasesRepository = pastReleasesRepositoryImpl
}
