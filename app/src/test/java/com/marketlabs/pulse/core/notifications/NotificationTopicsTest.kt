package com.marketlabs.pulse.core.notifications

import com.marketlabs.pulse.data.notifications.NotificationPreference
import org.junit.Assert.assertEquals
import org.junit.Test

class NotificationTopicsTest {

    @Test
    fun releaseSubscribesToProductionTopicsOnly() {
        assertEquals(
            listOf("daily_summary"),
            NotificationTopics.topicsFor(NotificationPreference.DAILY_SUMMARY, includeTestTopics = false)
        )
        assertEquals(
            listOf("stock_analysis"),
            NotificationTopics.topicsFor(NotificationPreference.STOCK_ANALYSIS, includeTestTopics = false)
        )
    }

    @Test
    fun debugAddsTestTopics() {
        assertEquals(
            listOf("daily_summary", "daily_summary_test"),
            NotificationTopics.topicsFor(NotificationPreference.DAILY_SUMMARY, includeTestTopics = true)
        )
        assertEquals(
            listOf("stock_analysis", "stock_analysis_test"),
            NotificationTopics.topicsFor(NotificationPreference.STOCK_ANALYSIS, includeTestTopics = true)
        )
    }

    @Test
    fun channelIdsMatchBackendContract() {
        assertEquals("daily_summary", NotificationChannels.DAILY_SUMMARY)
        assertEquals("stock_updates", NotificationChannels.STOCK_UPDATES)
    }
}
