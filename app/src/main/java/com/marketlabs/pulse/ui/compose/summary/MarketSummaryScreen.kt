package com.marketlabs.pulse.ui.compose.summary

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.marketlabs.pulse.R
import com.marketlabs.pulse.storage.model.summary.DominoEffect
import com.marketlabs.pulse.storage.model.summary.MacroItem
import com.marketlabs.pulse.storage.model.summary.MarketPulse
import com.marketlabs.pulse.storage.model.summary.MarketOutlook
import com.marketlabs.pulse.storage.model.summary.NewsItem
import com.marketlabs.pulse.storage.model.summary.Verdict
import com.marketlabs.pulse.storage.model.summary.enums.ReportType
import com.marketlabs.pulse.storage.model.summary.enums.TradingCall
import com.marketlabs.pulse.ui.theme.* // 💡 ACTION: Imports your new semantic Verdict colors
import java.text.SimpleDateFormat
import java.util.*

/**
 * The main screen for displaying the Market Pulse report.
 *
 * This Composable handles the layout of the entire report using a [LazyColumn].
 * It implements a "Safe UI" pattern where sections are only rendered if the
 * corresponding data is non-null.
 *
 * @param data The [MarketPulse] domain object containing the full report.
 * All fields in this object are nullable.
 */
@Composable
fun MarketSummaryScreen(
    data: MarketPulse?,
    isLegacyVersion: Boolean,
    hasLegacyData: Boolean,
    onToggleVersion: () -> Unit,
    scaffoldPadding: PaddingValues
) {
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val paddingLarge = dimensionResource(id = R.dimen.padding_large)
    val paddingXLarge = dimensionResource(id = R.dimen.padding_xlarge)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(
            top = statusBarHeight + paddingLarge,
            bottom = scaffoldPadding.calculateBottomPadding() + paddingLarge,
            start = paddingLarge,
            end = paddingLarge
        ),
        verticalArrangement = Arrangement.spacedBy(paddingXLarge)
    ) {
        if (hasLegacyData) {
            item { VersionToggleBanner(isLegacyVersion, onToggleVersion) }
        }

        data?.let { validData ->

            val type = validData.reportType
            val timestamp = validData.timestamp
            if (type != null && timestamp != null) {
                item { HeaderSection(type, timestamp) }
            }

            val stories = validData.leadStories
            if (!stories.isNullOrEmpty()) {
                item {
                    SectionTitle(
                        title = stringResource(id = R.string.section_lead_stories),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                items(stories) { story -> NewsCard(story) }
            }

            val macros = validData.macroMix
            if (!macros.isNullOrEmpty()) {
                item { SectionTitle(stringResource(id = R.string.section_macro_mix), color = MaterialTheme.colorScheme.primary) }
                items(macros) { macro -> MacroCard(macro) }
            }

            validData.dominoEffect?.let { domino ->
                item { DominoCard(domino) }
            }

            validData.marketOutlook?.let { outlook ->
                if (!outlook.summary.isNullOrBlank()) item { OutlookCard(outlook) }
            }

            validData.verdict?.let { verdict ->
                item { VerdictCard(verdict) }
            }

            validData.verdict?.action?.let { action ->
                if (action.isNotBlank()) item { ActionFooter(action) }
            }
        }
    }
}

// ---------------------------------------------------------
// COMPONENT LIBRARY
// ---------------------------------------------------------

/**
 * Toggle between v3 and v2
 */
@Composable
fun VersionToggleBanner(isLegacyVersion: Boolean, onClick: () -> Unit) {
    val bgColor = if (isLegacyVersion) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.secondaryContainer
    val contentColor = if (isLegacyVersion) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSecondaryContainer

    Card(
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_card)),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = if (isLegacyVersion) R.string.banner_emoji_legacy else R.string.banner_emoji_test),
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_large)))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(id = if (isLegacyVersion) R.string.banner_viewing_legacy else R.string.banner_viewing_test),
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor.copy(alpha = 0.7f)
                )
                Text(
                    text = stringResource(id = if (isLegacyVersion) R.string.banner_switch_latest else R.string.banner_compare_legacy),
                    style = MaterialTheme.typography.titleMedium,
                    color = contentColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Header section - Market Summary
 */
@Composable
fun HeaderSection(type: ReportType, timestamp: Long) {
    Column {
        Text(
            text = type.label.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = stringResource(id = R.string.pulse_title),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        val date = Date(timestamp * 1000L)
        val format = SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault())

        Text(
            text = stringResource(id = R.string.pulse_updated_at, format.format(date)),
            style = MaterialTheme.typography.bodySmall,
            // 💡 ACTION: Replaced hardcoded Color.Gray with Theme's semantic variant text color
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Displays a single news item card.
 *
 * @param story The [NewsItem] to display. If the headline is missing, nothing is rendered.
 */
@Composable
fun NewsCard(story: NewsItem) {
    val headline = story.headline ?: return

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_card)),
        modifier = Modifier.fillMaxWidth().padding(bottom = dimensionResource(id = R.dimen.padding_medium))
    ) {
        Column(modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))) {
            Text(
                text = headline,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary
            )
            story.summary?.let { summary ->
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

/**
 * Displays a macro-economic factor with a tag (e.g., "Inflation", "Rates").
 *
 * @param item The [MacroItem] to display. Requires a headline to render.
 */
@Composable
fun MacroCard(item: MacroItem) {
    val headline = item.headline ?: return

    Card(
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_card)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth().padding(bottom = dimensionResource(id = R.dimen.padding_medium))
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(dimensionResource(id = R.dimen.border_medium))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
            )

            Column(modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))) {
                Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = headline,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.weight(1f)
                    )

                    item.tag?.let { tag ->
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_small)),
                            modifier = Modifier.padding(start = dimensionResource(id = R.dimen.padding_standard), top = dimensionResource(id = R.dimen.padding_tiny))
                        ) {
                            Text(
                                text = tag.label.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(
                                    horizontal = dimensionResource(id = R.dimen.corner_radius_chip),
                                    vertical = dimensionResource(id = R.dimen.padding_tiny)
                                )
                            )
                        }
                    }
                }

                item.summary?.let {
                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.border_thick)))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    }
}

/**
 * Displays the "Domino Effect" card, showing a causal relationship.
 *
 * Layout:
 * - Trigger: The event that started it.
 * - Impact: The result of that event.
 *
 * @param domino The [DominoEffect] object. Requires both 'trigger' and 'impact' to render.
 */
@Composable
fun DominoCard(domino: DominoEffect) {
    val trigger = domino.trigger ?: return
    val impact = domino.impact ?: return
    val outlook = domino.outlook ?: return

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_card)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(id = R.string.emoji_domino),
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_medium)))
                Text(
                    text = stringResource(id = R.string.section_domino_effect),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_large)))

            DominoTimelineStep(title = stringResource(id = R.string.label_trigger).uppercase(), text = trigger, isLast = false)
            DominoTimelineStep(title = stringResource(id = R.string.label_impact).uppercase(), text = impact, isLast = false)
            DominoTimelineStep(title = stringResource(id = R.string.label_outlook).uppercase(), text = outlook, isLast = true)
        }
    }
}

/**
 * Helper row for the DominoCard to align labels and values.
 */
@Composable
private fun DominoTimelineStep(title: String, text: String, isLast: Boolean) {
    Row(modifier = Modifier.height(IntrinsicSize.Min)) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(dimensionResource(id = R.dimen.timeline_node_width)).fillMaxHeight()
        ) {
            Box(
                modifier = Modifier
                    .size(dimensionResource(id = R.dimen.timeline_dot_size))
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(dimensionResource(id = R.dimen.padding_tiny))
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                )
            }
        }

        Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_standard)))

        Column(modifier = Modifier.padding(bottom = if (isLast) 0.dp else dimensionResource(id = R.dimen.padding_xlarge))) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Market Outlook card
 */
@Composable
fun OutlookCard(outlook: MarketOutlook) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_card)),
        border = BorderStroke(dimensionResource(id = R.dimen.border_thin), MaterialTheme.colorScheme.secondaryContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))) {
            // 💡 ACTION: Market Outlook Title upgraded to titleMedium for higher visibility
            Text(
                text = stringResource(id = R.string.section_market_outlook),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))
            Text(
                text = outlook.summary ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * The primary dashboard card showing the Market Regime, Trading Call, and Technical Setup.
 *
 * Visual Logic:
 * - Changes background color based on the [TradingCall] (Green for Buy, Red for Sell, Orange for Neutral).
 * - If essential fields (Regime, Setup, Call) are missing, this composable renders nothing.
 *
 * @param verdict The [Verdict] object containing the analysis.
 */
@Composable
fun VerdictCard(verdict: Verdict) {
    val call = verdict.call ?: return
    val regime = verdict.regime ?: return
    val setup = verdict.setup ?: return

    val (bgColor, textColor) = when (call) {
        TradingCall.CONTRARIAN_BUY,
        TradingCall.ACCUMULATE -> Pair(VerdictBuyBackground, VerdictBuyText)
        TradingCall.CONTRARIAN_SELL,
        TradingCall.SELL_AVOID,
        TradingCall.HEDGE_PROTECT -> Pair(VerdictSellBackground, VerdictSellText)
        else -> Pair(VerdictNeutralBackground, VerdictNeutralText)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_card)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))) {
            Surface(
                color = textColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_small)),
                modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.padding_medium))
            ) {
                Text(
                    text = regime.label,
                    color = textColor,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(
                        horizontal = dimensionResource(id = R.dimen.corner_radius_chip),
                        vertical = dimensionResource(id = R.dimen.padding_tiny)
                    )
                )
            }

            Text(
                text = call.label,
                style = MaterialTheme.typography.headlineSmall,
                color = textColor
            )

            Text(
                text = setup.label,
                style = MaterialTheme.typography.labelMedium,
                color = textColor.copy(alpha = 0.8f),
                modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.padding_standard))
            )

            HorizontalDivider(color = textColor.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_standard)))

            Text(
                text = verdict.analysis ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = textColor
            )
        }
    }
}

/**
 * A prominent footer card showing the actionable advice for the user.
 *
 * @param action The action string to display.
 */
@Composable
fun ActionFooter(action: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_card)),
        border = BorderStroke(dimensionResource(id = R.dimen.border_thin), MaterialTheme.colorScheme.primary),
        elevation = CardDefaults.cardElevation(defaultElevation = dimensionResource(id = R.dimen.elevation_small)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_small))
                ) {
                    Text(
                        text = stringResource(id = R.string.emoji_action),
                        modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_small)),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_large)))
                Text(
                    text = stringResource(id = R.string.section_action_plan),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_standard)))

            Text(
                text = action,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * A standardized section title for the list.
 */
@Composable
fun SectionTitle(title: String, color: Color = MaterialTheme.colorScheme.onBackground) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        color = color,
        modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_medium))
    )
}