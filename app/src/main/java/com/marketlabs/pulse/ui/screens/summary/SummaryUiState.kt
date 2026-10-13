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
     * @param selectedTabIndex Index into the Summary tabs (Pulse/Drivers/Outlook); survives day
     * changes, so picking another day keeps the reader on the tab they were reading.
     * @param expandItemsByDefault The persisted default for Market Movers / Macro Mix rows.
     * @param isPro Whether the user is entitled to the content a paid tier will lock.
     */
    data class Success(
        val calendarDayIds: List<String>,
        val selectedDateId: String,
        val todayDateId: String,
        val contentByDateId: Map<String, DayContent>,
        val reportType: ReportType,
        val selectedTabIndex: Int = 0,
        val expandItemsByDefault: Boolean = false,
        val isPro: Boolean = true
    ) : SummaryUiState {
        val isTodaySelected: Boolean get() = selectedDateId == todayDateId
        val selectedContent: DayContent get() = contentByDateId[selectedDateId] ?: DayContent.Loading

        /**
         * The most recent calendar day that actually has a report -- not necessarily today (today's
         * report usually posts in the afternoon). The Summary's live-only cards and its analytics
         * day offsets are both anchored to this day.
         */
        val latestAvailableDateId: String?
            get() = calendarDayIds.lastOrNull { contentByDateId[it] is DayContent.Available }

        /** Days behind the latest report ([dateId] = latest -> 0); never negative. */
        fun dayOffsetOf(dateId: String): Int {
            val latestIndex = calendarDayIds.indexOf(latestAvailableDateId ?: todayDateId)
            val index = calendarDayIds.indexOf(dateId)
            return if (index < 0 || latestIndex < 0) 0 else (latestIndex - index).coerceAtLeast(0)
        }
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

    /** A past date's first-ever sync failed and nothing is cached for it yet. */
    data class Error(val dateId: String, val message: String) : DayContent
}
