package com.marketlabs.pulse.utils

object CachePolicy {
    private const val FIFTEEN_MINUTES_MS = 15 * 60 * 1000L
    private const val ONE_HOUR_MS = 60 * 60 * 1000L

    /**
     * Checks if the data is older than the current 15-minute clock block (e.g., :00, :15, :30, :45).
     * Used for fast-moving screens like News.
     */
    fun isQuarterHourExpired(dataTimestamp: Long, currentTime: Long = System.currentTimeMillis()): Boolean {
        // By dividing by the 15-min block size, we get a pure integer block ID since 1970.
        // If the data's block ID doesn't match the current time's block ID, it's expired!
        return (dataTimestamp / FIFTEEN_MINUTES_MS) != (currentTime / FIFTEEN_MINUTES_MS)
    }

    /**
     * Checks if the data is older than the current hourly clock block (e.g., 10:00, 11:00).
     * Used for heavy AI screens like Summary, Indicators, and Risk Radar.
     */
    fun isHourlyExpired(dataTimestamp: Long, currentTime: Long = System.currentTimeMillis()): Boolean {
        return (dataTimestamp / ONE_HOUR_MS) != (currentTime / ONE_HOUR_MS)
    }
}