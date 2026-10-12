package com.marketlabs.pulse.ui.screens.summary.views

import androidx.annotation.StringRes
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateMapOf
import com.marketlabs.pulse.R
import com.marketlabs.pulse.core.analytics.AnalyticsEvent

/**
 * The three tabs every Summary day is split into. The enum name is lower-snake-cased into the
 * reported `screen_name` by `ScreenName.forTab` (`summary_pulse`, `summary_drivers`,
 * `summary_outlook`), so renaming a constant renames a shipped analytics value.
 */
enum class SummaryTab(@StringRes val labelRes: Int, val lockedBy: SummaryFeature? = null) {
    PULSE(R.string.summary_tab_pulse),
    DRIVERS(R.string.summary_tab_drivers),
    OUTLOOK(R.string.summary_tab_outlook, lockedBy = SummaryFeature.OUTLOOK_TAB)
}

/**
 * Everything on the Summary screen a paid tier will lock. Each interaction asks
 * [SummaryAccess.allows] before acting, so a future gate is one decision in one place.
 */
enum class SummaryFeature {
    MARKET_READ, SENTIMENT_SUMMARY, MOVERS_SUMMARY, MACRO_SUMMARY, EXPAND_SETTING, OUTLOOK_TAB
}

/** v1 entitles every user to every [SummaryFeature]; see `EntitlementRepository`. */
@Stable
class SummaryAccess(private val isPro: Boolean) {
    @Suppress("UNUSED_PARAMETER")
    fun allows(feature: SummaryFeature): Boolean = isPro
}

/**
 * A card part that is hidden until tapped. [isRow] sections (Movers, Macro Mix) are per-item rows
 * that follow the screen-level expanded/collapsed default; the others always start collapsed and
 * ignore it. [feature] is the lock that guards expanding it, or null when it is never locked.
 */
enum class SummarySection(
    val analytics: AnalyticsEvent.ExpandedSection,
    val isRow: Boolean,
    val feature: SummaryFeature?
) {
    MARKET_MOVERS(AnalyticsEvent.ExpandedSection.MARKET_MOVERS, isRow = true, SummaryFeature.MOVERS_SUMMARY),
    MACRO_MIX(AnalyticsEvent.ExpandedSection.MACRO_MIX, isRow = true, SummaryFeature.MACRO_SUMMARY),
    MARKET_SENTIMENT(AnalyticsEvent.ExpandedSection.MARKET_SENTIMENT, isRow = false, SummaryFeature.SENTIMENT_SUMMARY),
    MARKET_READ(AnalyticsEvent.ExpandedSection.MARKET_READ, isRow = false, SummaryFeature.MARKET_READ),
    WHAT_CHANGED(AnalyticsEvent.ExpandedSection.WHAT_CHANGED, isRow = false, feature = null);

    internal fun idFor(position: Int): String = "$name:$position"
}

/**
 * What the reader has expanded on the day currently on screen. Owned above the tab pager (so
 * switching tabs keeps it) and recreated whenever the day changes (so every day starts fresh).
 *
 * A row's state is an override of the screen-level default; [clearRowOverrides] drops them so every
 * row follows a newly chosen default again, while non-row sections are untouched.
 */
@Stable
class SummaryExpansionState {
    private val overrides = mutableStateMapOf<String, Boolean>()

    fun isExpanded(section: SummarySection, position: Int, rowDefault: Boolean): Boolean =
        overrides[section.idFor(position)] ?: (section.isRow && rowDefault)

    /** Flips the item and returns its new state. */
    fun toggle(section: SummarySection, position: Int, rowDefault: Boolean): Boolean {
        val next = !isExpanded(section, position, rowDefault)
        overrides[section.idFor(position)] = next
        return next
    }

    fun clearRowOverrides() {
        overrides.keys.filter { id -> SummarySection.entries.any { it.isRow && id.startsWith("${it.name}:") } }
            .forEach { overrides.remove(it) }
    }
}
