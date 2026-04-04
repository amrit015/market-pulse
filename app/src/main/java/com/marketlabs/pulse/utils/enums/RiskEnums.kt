package com.marketlabs.pulse.utils.enums

enum class RiskStatus {
    SAFE,
    STABLE,
    CAUTION,
    DANGER,
    UNKNOWN;

    companion object {
        // Safely parses the string, defaulting to UNKNOWN if there's no match
        fun fromString(value: String?): RiskStatus {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: UNKNOWN
        }
    }
}

enum class RiskTrend {
    ACCELERATING,
    COOLING,
    STABLE,
    UNKNOWN;

    companion object {
        fun fromString(value: String?): RiskTrend {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: UNKNOWN
        }
    }
}
