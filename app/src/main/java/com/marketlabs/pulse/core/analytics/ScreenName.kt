package com.marketlabs.pulse.core.analytics

import java.util.Locale

/**
 * Every `screen_name` the app reports. A fixed set (not derived from the resolved route) keeps
 * names stable and keeps route arguments — URLs, glossary free text — out of analytics.
 *
 * [screenClass] groups related names (all Stock Detail tabs share one class).
 */
enum class ScreenName(val value: String, val screenClass: String) {
    OVERVIEW("overview", "MARKET_OVERVIEW"),
    ASSET_DETAIL("asset_detail", "ASSET_DETAIL"),
    INDICATORS("indicators", "MARKET_INDICATORS"),
    INDICATOR_HORIZONS("indicator_horizons", "INDICATOR_HORIZONS"),
    METRIC_DETAIL("metric_detail", "METRIC_DETAIL"),
    SUMMARY("summary", "MARKET_SUMMARY"),
    INSIGHTS("insights", "MARKET_INSIGHTS"),
    PAST_RELEASES("past_releases", "PAST_RELEASES"),
    GLOSSARY_DETAIL("glossary_detail", "GLOSSARY_DETAIL"),
    ANALYSIS("analysis", "MARKET_ANALYSIS"),
    STOCK_DETAIL("stock_detail", "STOCK_ANALYSIS_DETAIL"),
    DEEP_DIVE("deep_dive", "DEEP_DIVE_DETAIL"),
    RESOLVED_CALLS("resolved_calls", "RESOLVED_CALLS_LIST"),
    TECHNICAL_TIMELINE("technical_timeline", "TECHNICAL_TIMELINE_LIST"),
    NEWS("news", "MARKET_NEWS"),
    NEWS_WEBVIEW("news_webview", "WEBVIEW"),
    TUTORIALS_HUB("tutorials_hub", "TUTORIALS_HUB"),
    TUTORIAL_CONCEPT("tutorial_concept", "TUTORIALS_CONCEPT"),
    TUTORIAL_DECK("tutorial_deck", "TUTORIALS_DECK"),
    TUTORIAL_GLOSSARY("tutorial_glossary", "TUTORIALS_GLOSSARY"),
    TUTORIAL_GAUGE_ANATOMY("tutorial_gauge_anatomy", "TUTORIALS_GAUGE_ANATOMY"),
    TUTORIAL_DATA_LIMITATIONS("tutorial_data_limitations", "TUTORIALS_DATA_LIMITATIONS"),
    TUTORIAL_ARTICLE("tutorial_article", "TUTORIALS_ARTICLE"),
    ONBOARDING("onboarding", "ONBOARDING_CAROUSEL"),
    LEGAL_ACCEPTANCE("legal_acceptance", "LEGAL_ACCEPTANCE"),
    TERMS("terms", "TERMS_CONDITIONS"),
    PRIVACY_POLICY("privacy_policy", "PRIVACY_POLICY"),
    SETTINGS("settings", "SETTINGS"),
    SETTINGS_NOTIFICATIONS("settings_notifications", "SETTINGS_NOTIFICATIONS"),
    SETTINGS_DATA_SYNC("settings_data_sync", "SETTINGS_DATA_SYNC"),
    SETTINGS_ABOUT("settings_about", "SETTINGS_ABOUT"),
    SETTINGS_COMING_UP("settings_coming_up", "SETTINGS_COMING_UP"),
    SETTINGS_THEME_PICKER("settings_theme_picker", "SETTINGS_THEME_PICKER");
}

/**
 * The reported `screen_name` and `screen_class` for a screen, resolved once so tab names stay
 * `<base>_<tab>` everywhere.
 */
data class ScreenTarget(val name: String, val screenClass: String)

fun ScreenName.target(): ScreenTarget = ScreenTarget(value, screenClass)

/** `<base>_<tab>`, with the enum constant lower-snake-cased (`TACTICAL_MOMENTUM` -> `tactical_momentum`). */
fun ScreenName.forTab(tab: Enum<*>): ScreenTarget =
    ScreenTarget("${value}_${tab.name.lowercase(Locale.ROOT)}", screenClass)
