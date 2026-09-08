package com.marketlabs.pulse.utils

import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Server uses the EST date/time
 */
val marketZone = ZoneId.of("America/New_York")

fun getTodayDateString(): String {
    return ZonedDateTime.now(marketZone)
        .format(DateTimeFormatter.ISO_LOCAL_DATE)
}

fun getYesterdayDateString(): String {
    return ZonedDateTime.now(marketZone)
        .minusDays(1)
        .format(DateTimeFormatter.ISO_LOCAL_DATE)
}

fun getMidnightTimestamp(): Long {
    return ZonedDateTime.now(marketZone)
        .toLocalDate()
        .atStartOfDay(marketZone)
        .toInstant()
        .toEpochMilli()
}

fun Long.toDateIdString(): String {
    if (this == 0L) return "1970-01-01" // Fallback

    return try {
        // 💡 `lastUpdated` (and `lastSyncedTimestamp`) are epoch MILLIS everywhere they're set --
        // backend's `timestamp` field is JS `Date.now()`, and this app's own
        // `System.currentTimeMillis()` calls match. This used to read `Instant.ofEpochSecond`,
        // which silently produced a garbage far-future dateId (millis misread as seconds) --
        // harmless while dateId was only ever used as a Room primary key and never queried by
        // value or displayed, but load-bearing now that the Summary calendar strip looks up
        // cached rows BY dateId (core/summary/SummaryRepository.kt's syncPastDate).
        Instant.ofEpochMilli(this)
            .atZone(ZoneId.of("America/New_York"))
            .format(DateTimeFormatter.ISO_LOCAL_DATE) // "2026-02-07"
    } catch (e: Exception) {
        "Unknown-Date"
    }
}

/**
 * The last [days] NY-calendar dateIds ("yyyy-MM-dd"), oldest first, ending with today's — the
 * Summary screen's calendar strip. Anchored to [marketZone], not the device's timezone, so the
 * strip's "Today" always matches the NY trading day the backend's reports are keyed by, even for
 * a viewer well ahead of NY time (e.g. it can still read yesterday's date on the strip while the
 * device's own calendar has already rolled over to the next day).
 */
fun getLastNDateIds(days: Int): List<String> {
    val today = ZonedDateTime.now(marketZone).toLocalDate()
    return (days - 1 downTo 0).map { offset ->
        today.minusDays(offset.toLong()).format(DateTimeFormatter.ISO_LOCAL_DATE)
    }
}

private val RELATIVE_DATE_FORMATTER = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.US)

/**
 * A `yyyy-MM-dd` dateId as "Today" / "Yesterday" / "September 5, 2026" -- the label the Summary
 * screen shows under its calendar strip. Deliberately the device's own local date (`LocalDate.now()`
 * with no explicit zone), NOT [marketZone] -- unlike the strip's own dateIds (`getLastNDateIds`,
 * which stay NY-anchored on purpose, since that's what the backend/cache actually key by), "Today"
 * here is a claim about the *viewer's* day, not the market's. A viewer far enough behind NY (e.g.
 * Los Angeles at 9:35pm, already past midnight in NY) would otherwise see NY's brand-new date
 * labeled "Today" on their screen before their own calendar has rolled over to it -- the pill still
 * appears (the strip itself doesn't change), but tapping it should read as its actual date, not
 * "Today", until the device's own clock agrees.
 */
fun String.toRelativeDayLabel(): String {
    return try {
        val date = java.time.LocalDate.parse(this, DateTimeFormatter.ISO_LOCAL_DATE)
        val localToday = java.time.LocalDate.now()
        when (date) {
            localToday -> "Today"
            localToday.minusDays(1) -> "Yesterday"
            else -> date.format(RELATIVE_DATE_FORMATTER)
        }
    } catch (e: Exception) {
        this
    }
}

private val DISPLAY_DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM d, yyyy")

/**
 * Reformats a backend `yyyy-MM-dd` date string (report_date/settlement_date/reported_date/date --
 * every raw date field Posture/Positioning show) into "Aug 27, 2026" for display. Falls back to
 * the raw string on a parse failure rather than throwing, same as `MetricDetailScreen`'s own
 * `formatReleaseDate` precedent for its release-date field.
 */
fun String.toDisplayDate(): String {
    return try {
        java.time.LocalDate.parse(this, DateTimeFormatter.ISO_LOCAL_DATE).format(DISPLAY_DATE_FORMATTER)
    } catch (e: Exception) {
        this
    }
}