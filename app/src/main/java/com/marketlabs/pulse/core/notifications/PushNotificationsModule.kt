package com.marketlabs.pulse.core.notifications

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** `@Provides`-in-`object` per this repo's DI convention — no `@Binds`. */
@Module
@InstallIn(SingletonComponent::class)
object PushNotificationsModule {

    @Provides
    @Singleton
    fun providePushTopicManager(impl: PushTopicManagerImpl): PushTopicManager = impl
}
