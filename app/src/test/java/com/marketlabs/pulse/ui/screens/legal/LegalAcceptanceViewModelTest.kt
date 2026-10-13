package com.marketlabs.pulse.ui.screens.legal

import com.marketlabs.pulse.core.analytics.AnalyticsEvent
import com.marketlabs.pulse.core.analytics.FakeAnalyticsTracker
import com.marketlabs.pulse.data.legal.LegalRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LegalAcceptanceViewModelTest {

    private class FakeLegalRepository : LegalRepository {
        override val acceptedVersion: Flow<Int> = flowOf(0)
        override suspend fun acceptCurrentVersion() = Unit
    }

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun acceptanceLogsCurrentLegalVersion() {
        val tracker = FakeAnalyticsTracker()

        LegalAcceptanceViewModel(FakeLegalRepository(), tracker).acceptCurrentVersion()

        val event = tracker.events.single() as AnalyticsEvent.LegalAccept
        assertEquals(LegalRepository.CURRENT_LEGAL_VERSION.toLong(), event.params["legal_version"])
    }
}
