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