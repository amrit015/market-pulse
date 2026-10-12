package com.marketlabs.pulse.ui.screens.summary

import com.marketlabs.pulse.storage.model.summary.MarketPulse
import com.marketlabs.pulse.utils.enums.ReportType
import org.junit.Assert.assertEquals
import org.junit.Test

class SummaryUiStateTest {

    private val days = listOf("2026-10-05", "2026-10-06", "2026-10-07", "2026-10-08", "2026-10-09", "2026-10-10", "2026-10-11")

    private fun available(dateId: String) =
        DayContent.Available(MarketPulse(dateId = dateId, lastSyncedTimestamp = 0L, lastUpdated = 0L))

    private fun state(content: Map<String, DayContent>) = SummaryUiState.Success(
        calendarDayIds = days,
        selectedDateId = days.last(),
        todayDateId = days.last(),
        contentByDateId = content,
        reportType = ReportType.DAILY_UPDATE
    )

    @Test
    fun latestReportDayIsOffsetZeroAndOlderDaysCountBack() {
        val success = state(days.associateWith { available(it) })

        assertEquals(0, success.dayOffsetOf("2026-10-11"))
        assertEquals(1, success.dayOffsetOf("2026-10-10"))
        assertEquals(6, success.dayOffsetOf("2026-10-05"))
    }

    @Test
    fun offsetsAnchorToTheLatestReportNotToTodayWhenTodayIsNotReady() {
        val content = days.dropLast(1).associateWith { available(it) } + (days.last() to DayContent.TodayNotReady)
        val success = state(content)

        assertEquals("2026-10-10", success.latestAvailableDateId)
        assertEquals(0, success.dayOffsetOf("2026-10-10"))
        assertEquals(2, success.dayOffsetOf("2026-10-08"))
        // Today has no report and sits ahead of the latest one: never negative.
        assertEquals(0, success.dayOffsetOf("2026-10-11"))
    }
}
