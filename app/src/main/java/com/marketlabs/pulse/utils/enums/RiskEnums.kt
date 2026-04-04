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

// ==========================================
// ENUMS FOR MARKET RISKS
// ==========================================

enum class RiskImpactLevel(val label: String) {
    EXTREME("Extreme"),
    HIGH("High"),
    MEDIUM("Medium"),
    LOW("Low"),
    UNKNOWN("Unknown");

    companion object {
        fun fromString(value: String?): RiskImpactLevel {
            // Clean the AI's string so it's easy to compare
            val normalized = value?.trim()?.uppercase() ?: return UNKNOWN

            return when {
                normalized in listOf("EXTREME", "CRITICAL", "SEVERE") -> EXTREME
                normalized in listOf("HIGH", "ELEVATED", "MAJOR", "SIGNIFICANT") -> HIGH
                normalized in listOf("MEDIUM", "MODERATE", "NEUTRAL") -> MEDIUM
                normalized in listOf("LOW", "MINOR", "SAFE", "MINIMAL") -> LOW
                else -> UNKNOWN
            }
        }
    }
}
