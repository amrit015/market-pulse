package com.marketlabs.pulse.ui.screens.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marketlabs.pulse.core.summary.SummaryRepository
import com.marketlabs.pulse.core.sync.SyncManager
import com.marketlabs.pulse.utils.enums.ReportType
import com.marketlabs.pulse.utils.getLastNDateIds
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class SummaryViewModel @Inject constructor(
    private val repository: SummaryRepository,
    private val syncManager: SyncManager
) : ViewModel() {

    // NY-anchored (see utils/DateExtension.kt) -- computed once per ViewModel instance rather than
    // per-recomposition, same as the rest of this screen's state.
    private val calendarDayIds: List<String> = getLastNDateIds(7)
    private val todayDateId: String = calendarDayIds.last()
    private val yesterdayDateId: String = calendarDayIds[calendarDayIds.size - 2]
    private val pastDateIds: List<String> = calendarDayIds.dropLast(1)

    private val _selectedDateId = MutableStateFlow(todayDateId)
    val selectedDateId: StateFlow<String> = _selectedDateId.asStateFlow()

    // Set once the user (or the today-empty fallback below) has moved off the initial default --
    // guards the fallback so it only ever fires once per session, not on every emission where
    // today happens to still be null.
    private var hasAutoFallenBack = false

    private val todayStream = repository.getMarketPulseStream()

    // One local-only (no network) reactive read per past day, combined into a dateId -> entry map
    // -- lets every page of the calendar-strip pager show whatever's already cached the instant
    // it's swiped into view, not just the single day the ViewModel currently calls "selected".
    // Network fetches stay lazy: see the settledPage-driven selectDate() calls from
    // MarketSummaryScreen and the syncPastDate trigger below, neither of which touch every date at
    // once.
    private val pastEntriesByDateId = combine(
        pastDateIds.map { dateId -> repository.getMarketPulseForDate(dateId).map { dateId to it } }
    ) { pairs -> pairs.toMap() }

    init {
        // Confirmed decision: today has no report yet -> land on yesterday silently (no banner);
        // the "not generated yet" message only shows if the user actually swipes/taps onto the
        // Today page themselves. Runs once -- if today's data shows up later in the session, we
        // don't yank the user back to it if they're mid-read on a past day.
        viewModelScope.launch {
            todayStream.collect { data ->
                if (!hasAutoFallenBack && _selectedDateId.value == todayDateId && data == null) {
                    hasAutoFallenBack = true
                    _selectedDateId.value = yesterdayDateId
                }
            }
        }

        // Applies the past-date caching rule whenever selection moves to a past date -- covers
        // both explicit taps/swipes (selectDate, called from the pager's settledPage effect) and
        // the auto-fallback above. Cheap to re-run on repeat selection of an already-locked-in
        // date: syncPastDate's own check is one local DB read before it decides whether a network
        // call is even needed.
        viewModelScope.launch {
            _selectedDateId.collect { dateId ->
                if (dateId != todayDateId) {
                    repository.syncPastDate(dateId)
                }
            }
        }
    }

    val summaryUiState: StateFlow<SummaryUiState> = combine(
        _selectedDateId, todayStream, pastEntriesByDateId
    ) { selectedDateId, todayData, pastEntries ->
        val contentByDateId = buildMap {
            put(todayDateId, if (todayData != null) DayContent.Available(todayData) else DayContent.TodayNotReady)
            pastDateIds.forEach { dateId ->
                val entry = pastEntries[dateId]
                put(
                    dateId,
                    when {
                        entry == null || !entry.cached -> DayContent.Loading
                        entry.data != null -> DayContent.Available(entry.data)
                        else -> DayContent.NotAvailable(dateId)
                    }
                )
            }
        }

        SummaryUiState.Success(
            calendarDayIds = calendarDayIds,
            selectedDateId = selectedDateId,
            todayDateId = todayDateId,
            contentByDateId = contentByDateId,
            // 💡 The SELECTED day's weekday, not always today's -- e.g. Saturday/Sunday should
            // read "Weekend Update" whenever that's the day actually on screen, today included.
            // The earlier root-cause bug (Today's tab silently rendering a stale previous day's
            // content, see SummaryRepositoryImpl/LocalSummaryDataSourceImpl's today-scoped
            // queries) was what made this misleading before -- with that fixed, whatever's
            // rendered for `selectedDateId` and this label always agree.
            reportType = selectedDateId.toWeekdayReportType()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SummaryUiState.Loading
    )

    private val _errorEvents: Channel<String> = Channel()
    val errorEvents = _errorEvents.receiveAsFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    /**
     * Wakes up the global listener when the Summary tab is visible.
     */
    fun onStart() {
        syncManager.startListening()
    }

    /**
     * Puts the listener to sleep when the app is backgrounded.
     */
    fun onStop() {
        syncManager.stopListening()
    }

    /** Calendar strip pill tap, or the pager settling on a new page after a swipe. */
    fun selectDate(dateId: String) {
        hasAutoFallenBack = true // a manual pick overrides the auto-fallback either way
        _selectedDateId.value = dateId
    }

    /**
     * Retained for manual Pull-To-Refresh. Only meaningful for today -- a past date's report never
     * changes again once cached (see SummaryRepository.syncPastDate's doc comment), so pulling to
     * refresh while viewing one is a no-op rather than a way to bypass the caching rule and rack up
     * extra Firestore reads.
     */
    fun refreshData(force: Boolean = true) = viewModelScope.launch {
        if (_selectedDateId.value != todayDateId) return@launch

        _isRefreshing.value = true
        val result = repository.refreshMarketSummary(force)
        result.onFailure { error ->
            _errorEvents.send(error.message ?: "Connection failed")
        }
        _isRefreshing.value = false
    }
}

/**
 * Daily vs. Weekend, purely from a `yyyy-MM-dd` dateId's own day of the week (NY calendar) --
 * deterministic and always knowable, unlike the backend's own `reportType` field on a report,
 * which doesn't exist until that day's report has actually generated (usually not until the
 * afternoon for today). Used for the global top bar label so it reads correctly even before/
 * without a report for the selected day (e.g. Monday morning, before today's report has posted,
 * still correctly reads "Daily Update" rather than falling back to Sunday's "Weekend Update").
 */
private fun String.toWeekdayReportType(): ReportType {
    return try {
        val dayOfWeek = LocalDate.parse(this).dayOfWeek
        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            ReportType.WEEKEND_UPDATE
        } else {
            ReportType.DAILY_UPDATE
        }
    } catch (e: Exception) {
        ReportType.UNKNOWN
    }
}
