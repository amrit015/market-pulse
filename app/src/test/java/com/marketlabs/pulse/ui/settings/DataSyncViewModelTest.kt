package com.marketlabs.pulse.ui.settings

import com.marketlabs.pulse.core.analytics.FakeAnalyticsTracker
import com.marketlabs.pulse.data.privacy.AnalyticsConsentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DataSyncViewModelTest {

    private class FakeConsentRepository : AnalyticsConsentRepository {
        val state = MutableStateFlow(true)
        override val enabled = state
        override suspend fun setEnabled(enabled: Boolean) {
            state.value = enabled
        }
    }

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun turningSharingOffPersistsAndStopsCollection() = runTest {
        val repository = FakeConsentRepository()
        val tracker = FakeAnalyticsTracker()
        val viewModel = DataSyncViewModel(repository, tracker)

        viewModel.setAnalyticsEnabled(false)

        assertEquals(false, repository.enabled.first())
        assertEquals(listOf(false), tracker.collectionCalls)
        assertTrue("no event for the change itself", tracker.events.isEmpty())
    }
}
