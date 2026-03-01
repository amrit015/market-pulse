package com.marketlabs.pulse.storage.model.dashboard.enums


/**
 * Defines the category of the asset to determine when it should be displayed.
 */
enum class AssetType {
    EQUITY,
    FUTURE,
    COMMODITY,
    CRYPTO,
    INDEX,
    SENTIMENT,
    UNKNOWN;

    companion object {
        fun fromString(value: String?): AssetType {
            return entries.find { it.name == value?.uppercase() } ?: UNKNOWN
        }
    }
}