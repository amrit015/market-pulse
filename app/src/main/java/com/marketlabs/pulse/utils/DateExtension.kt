package com.marketlabs.pulse.utils

import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

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
        Instant.ofEpochSecond(this)
            .atZone(ZoneId.of("America/New_York"))
            .format(DateTimeFormatter.ISO_LOCAL_DATE) // "2026-02-07"
    } catch (e: Exception) {
        "Unknown-Date"
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