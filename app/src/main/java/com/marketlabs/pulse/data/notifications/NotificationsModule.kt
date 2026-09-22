package com.marketlabs.pulse.data.notifications

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** `@Provides`-in-`object` per this repo's DI convention — no `@Binds`. */
@Module
@InstallIn(SingletonComponent::class)
object NotificationsModule {

    @Provides
    @Singleton
    fun provideNotificationPreferencesRepository(
        impl: NotificationPreferencesRepositoryImpl
    ): NotificationPreferencesRepository = impl
}
