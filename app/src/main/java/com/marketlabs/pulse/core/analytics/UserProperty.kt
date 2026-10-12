package com.marketlabs.pulse.core.analytics

/**
 * Never a symbol or a list of symbols: that would make analytics a per-device holdings profile.
 * Favorites are reported as coarse buckets instead.
 */
enum class UserProperty(val key: String) {
    THEME_PRESET("theme_preset"),
    FAV_STOCKS_BUCKET("fav_stocks_bucket"),
    FAV_METRICS_BUCKET("fav_metrics_bucket"),
    PUSH_SUMMARY_ON("push_summary_on"),
    PUSH_STOCKS_ON("push_stocks_on"),
    LEGAL_VERSION("legal_version"),
    AD_FREE("ad_free");

    companion object {
        /** `0`, `1_3`, `4_10`, `10_plus` — bounded so the property never identifies a portfolio. */
        fun favoritesBucket(count: Int): String = when {
            count <= 0 -> "0"
            count <= 3 -> "1_3"
            count <= 10 -> "4_10"
            else -> "10_plus"
        }
    }
}
