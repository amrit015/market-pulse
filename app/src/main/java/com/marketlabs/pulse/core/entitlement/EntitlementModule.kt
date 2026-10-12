package com.marketlabs.pulse.core.entitlement

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/** v1 has no paid tier, so every user is entitled to everything. */
@Singleton
class OpenEntitlementRepository @Inject constructor() : EntitlementRepository {
    override val isPro: StateFlow<Boolean> = MutableStateFlow(true).asStateFlow()
}

/** `@Provides`-in-`object` per this repo's DI convention — no `@Binds`. */
@Module
@InstallIn(SingletonComponent::class)
object EntitlementModule {

    @Provides
    @Singleton
    fun provideEntitlementRepository(impl: OpenEntitlementRepository): EntitlementRepository = impl
}
