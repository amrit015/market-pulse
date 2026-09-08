package com.marketlabs.pulse.ui.screens.summary

import com.marketlabs.pulse.storage.model.summary.MarketPulse
import com.marketlabs.pulse.utils.enums.ReportType

sealed interface SummaryUiState {
    data object Loading : SummaryUiState

    /**
     * @param calendarDayIds The 7 NY dateIds behind the calendar strip, oldest first, last =
     * [todayDateId].
     * @param contentByDateId One entry per [calendarDayIds] entry, always fully populated -- the
     * calendar-strip pager reads each page's own content directly from here rather than only
     * knowing about whichever single day is "selected", so a page already shows its cached content
     * (or a loading/empty state) the instant it's swiped into view.
     * @param reportType Daily vs. Weekend for [selectedDateId] -- whatever day is actually being
     * shown, including today -- derived purely from that dateId's day of the week (NY calendar),
     * not from any report's own `reportType` field (which may not exist yet; today's report is
     * often generated in the afternoon). Drives the global top bar.
     */
    data class Success(
        val calendarDayIds: List<String>,
        val selectedDateId: String,
        val todayDateId: String,
        val contentByDateId: Map<String, DayContent>,
        val reportType: ReportType
    ) : SummaryUiState {
        val isTodaySelected: Boolean get() = selectedDateId == todayDateId
        val selectedContent: DayContent get() = contentByDateId[selectedDateId] ?: DayContent.Loading
    }

    data class Error(val message: String) : SummaryUiState
}

/** What to render for one of [SummaryUiState.Success.calendarDayIds]. */
sealed interface DayContent {
    data class Available(val data: MarketPulse) : DayContent

    /** A past date, confirmed (tombstoned) to have no report -- weekend/holiday/pre-history. */
    data class NotAvailable(val dateId: String) : DayContent

    /** Today selected explicitly, but no report has posted yet. */
    data object TodayNotReady : DayContent

    /** A past date whose first-ever sync is still in flight. */
    data object Loading : DayContent
}
