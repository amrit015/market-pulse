package com.marketlabs.pulse.core.analytics

/**
 * Every analytics event the app sends. Names are the contract with Firebase reporting and A/B
 * goals, so a shipped name must never be renamed; the ones marked "goal" are used as experiment
 * goals.
 *
 * Param values are `String` or `Long` only, with no free text, URLs, or anything the user typed.
 * Firebase limits: names `[a-z_]` up to 40 chars, string values up to 100 chars, up to 25 params,
 * and no `firebase_`/`google_`/`ga_` prefixes.
 */
sealed class AnalyticsEvent(val name: String, val params: Map<String, Any>) {

    /** Goal event. Fires when a detail route opens; [source] is passed by the tapping caller. */
    class ContentOpen(contentType: ContentType, itemId: String, source: Source) : AnalyticsEvent(
        "content_open",
        mapOf("content_type" to contentType.value, "item_id" to itemId, "source" to source.value)
    )

    class SheetOpen(sheet: Sheet, screenName: String, itemId: String? = null) : AnalyticsEvent(
        "sheet_open",
        buildParams("sheet" to sheet.value, "screen_name" to screenName, "item_id" to itemId)
    )

    /** [key] is an article key or a mechanism key, whichever the screen is keyed by. */
    class TutorialBegin(key: TutorialKey) : AnalyticsEvent("tutorial_begin", key.toParams())

    /** Goal event. */
    class TutorialComplete(key: TutorialKey) : AnalyticsEvent("tutorial_complete", key.toParams())

    /** Goal event. [pushKey] stays absent (not empty) for older pushes without `data.key`. */
    class PushOpen(
        pushType: String?,
        pushKey: String?,
        targetScreen: String,
        launch: Launch
    ) : AnalyticsEvent(
        "push_open",
        buildParams(
            "push_type" to pushType,
            "push_key" to pushKey,
            "target_screen" to targetScreen,
            "launch" to launch.value
        )
    )

    /** [publisher] is the URL host only, never the URL. */
    class NewsOutbound(publisher: String, position: Long) : AnalyticsEvent(
        "news_outbound",
        mapOf("publisher" to publisher, "position" to position)
    )

    class ChartRangeChange(range: String, screenName: String) : AnalyticsEvent(
        "chart_range_change",
        mapOf("range" to range, "screen_name" to screenName)
    )

    class PullToRefresh(screenName: String) : AnalyticsEvent(
        "pull_to_refresh",
        mapOf("screen_name" to screenName)
    )

    class DataStateShown(state: DataState, screenName: String, domain: String) : AnalyticsEvent(
        "data_state_shown",
        mapOf("state" to state.value, "screen_name" to screenName, "domain" to domain)
    )

    class FavoriteToggle(kind: FavoriteKind, itemId: String, on: Boolean) : AnalyticsEvent(
        "favorite_toggle",
        mapOf("kind" to kind.value, "item_id" to itemId, "on" to if (on) 1L else 0L)
    )

    class ThemeChange(preset: String) : AnalyticsEvent("theme_change", mapOf("preset" to preset))

    class OnboardingStep(step: Long, action: OnboardingAction) : AnalyticsEvent(
        "onboarding_step",
        mapOf("step" to step, "action" to action.value)
    )

    class LegalAccept(legalVersion: Long) : AnalyticsEvent(
        "legal_accept",
        mapOf("legal_version" to legalVersion)
    )

    enum class ContentType(val value: String) {
        STOCK("stock"), ASSET("asset"), METRIC("metric"), NEWS_STORY("news_story"),
        TUTORIAL("tutorial"), GLOSSARY_TERM("glossary_term"), DEEP_DIVE("deep_dive"),
        PAST_RELEASE("past_release")
    }

    enum class Source(val value: String) {
        LIST("list"), FAVORITES("favorites"), PUSH("push"), CROSS_LINK("cross_link"),
        TUTORIAL("tutorial")
    }

    enum class Sheet(val value: String) {
        DRIVERS_INFO("drivers_info"), MARKET("market"), MARKET_READ("market_read"),
        RISK("risk"), SCREEN_GUIDE("screen_guide"), STOCK_GLOSSARY("stock_glossary")
    }

    enum class Launch(val value: String) { COLD("cold"), WARM("warm") }

    enum class DataState(val value: String) { ERROR("error"), EMPTY("empty") }

    enum class FavoriteKind(val value: String) { STOCK("stock"), METRIC("metric") }

    enum class OnboardingAction(val value: String) {
        NEXT("next"), SKIP("skip"), DONE("done")
    }

    sealed class TutorialKey {
        abstract fun toParams(): Map<String, Any>

        class Article(val articleKey: String) : TutorialKey() {
            override fun toParams() = mapOf<String, Any>("article_key" to articleKey)
        }

        class Deck(val mechanism: String) : TutorialKey() {
            override fun toParams() = mapOf<String, Any>("mechanism" to mechanism)
        }
    }

    companion object {
        /** Drops null entries, so an absent optional param is absent in the event, not empty. */
        internal fun buildParams(vararg pairs: Pair<String, String?>): Map<String, Any> =
            pairs.mapNotNull { (k, v) -> v?.let { k to it } }.toMap()
    }
}
