package com.marketlabs.pulse.utils.enums

enum class SubcategoryEnums(val label: String) {
    INFLATION("Inflation"),
    LABOR("Labor"),
    GROWTH("Growth"),
    POLICY("Policy"),
    UNKNOWN("Unknown");

    companion object {
        fun fromString(value: String?): SubcategoryEnums? {
            if (value.isNullOrBlank()) return null
            return entries.find {
                it.name.equals(value, ignoreCase = true) || it.label.equals(value, ignoreCase = true)
            } ?: UNKNOWN
        }
    }
}