package com.marketlabs.pulse.ui.screens.summary.views

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.marketlabs.pulse.R
import com.marketlabs.pulse.storage.model.summary.DominoEffect
import com.marketlabs.pulse.storage.model.summary.MacroItem
import com.marketlabs.pulse.storage.model.summary.MarketOutlook
import com.marketlabs.pulse.storage.model.summary.MarketPulse
import com.marketlabs.pulse.storage.model.summary.NewsItem
import com.marketlabs.pulse.storage.model.summary.Verdict
import com.marketlabs.pulse.ui.components.bottomSheet.MarketGlossaryBottomSheet
import com.marketlabs.pulse.ui.theme.PulseStatusColors.BearishBg
import com.marketlabs.pulse.ui.theme.PulseStatusColors.BearishText
import com.marketlabs.pulse.ui.theme.PulseStatusColors.BullishBg
import com.marketlabs.pulse.ui.theme.PulseStatusColors.BullishText
import com.marketlabs.pulse.ui.theme.PulseStatusColors.NeutralBg
import com.marketlabs.pulse.ui.theme.PulseStatusColors.NeutralText
import com.marketlabs.pulse.utils.enums.ReportType
import com.marketlabs.pulse.utils.enums.TradingCall
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

    // 💡 NEW: State to track if the Verdict Glossary bottom sheet is open
    var showVerdictGlossary by remember { mutableStateOf(false) }

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
        verticalArrangement = Arrangement.spacedBy(paddingLarge)
    ) {
        if (hasLegacyData) {
            item { VersionToggleBanner(isLegacyVersion, onToggleVersion) }
        }

        data?.let { validData ->

            val type = validData.reportType
            val timestamp = validData.lastUpdated
            if (type != null) {
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
                items(stories) { story -> LeadStoryCard(story) }
            }

            val macros = validData.macroMix
            if (!macros.isNullOrEmpty()) {
                item {
                    SectionTitle(
                        stringResource(id = R.string.section_macro_mix),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                items(macros) { macro -> MacroCard(macro) }
            }

            item {
                SectionTitle(
                    title = stringResource(id = R.string.section_market_summary),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            validData.dominoEffect?.let { domino ->
                item { DominoCard(domino) }
            }

            validData.marketOutlook?.let { outlook ->
                if (!outlook.summary.isNullOrBlank()) item { OutlookCard(outlook) }
            }

            validData.verdict?.let { verdict ->
                // 💡 UPDATED: Pass the onClick trigger to open the sheet
                item {
                    VerdictCard(
                        verdict = verdict,
                        onClick = { showVerdictGlossary = true }
                    )
                }
            }

            validData.verdict?.action?.let { action ->
                if (action.isNotBlank()) item { ActionFooter(action) }
            }
        }
    }

    // 💡 NEW: Trigger the Glossary Bottom Sheet
    if (showVerdictGlossary && data?.verdict != null) {
        // Assuming VerdictGlossaryBottomSheet is in your common sheets package
        MarketGlossaryBottomSheet(
            currentRegime = data.verdict.regime?.label?.uppercase(),
            currentSetup = data.verdict.setup?.label?.uppercase(),
            currentCall = data.verdict.call?.label?.uppercase(),
            onDismiss = { showVerdictGlossary = false }
        )
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
    val bgColor =
        if (isLegacyVersion) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.secondaryContainer
    val contentColor =
        if (isLegacyVersion) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSecondaryContainer

    Card(
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_card)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
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
            text = type.label,
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        val date = Date(timestamp)
        val format = SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault())

        Text(
            text = stringResource(id = R.string.analyzed_at, format.format(date)),
            style = MaterialTheme.typography.bodySmall,
            // 💡 ACTION: Replaced hardcoded Color.Gray with Theme's semantic variant text color
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_micro))
        )
    }
}

/**
 * Displays a single news item card.
 *
 * @param story The [NewsItem] to display. If the headline is missing, nothing is rendered.
 */
@Composable
fun LeadStoryCard(story: NewsItem) {
    val headline = story.headline ?: return

    Card(
        // 💡 CHANGED: Matches Market Outlook background
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_card)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Text(
                modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large)),
                text = headline,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                thickness = dimensionResource(id = R.dimen.border_thin)
            )

            story.summary?.let { summary ->
                Text(
                    modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large)),
                    text = summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
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
        // 💡 CHANGED: Matches Market Outlook background
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(dimensionResource(id = R.dimen.border_medium))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
            )

            Column {
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(dimensionResource(id = R.dimen.padding_large))
                ) {
                    Text(
                        text = headline,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.weight(1f)
                    )

                    item.tag?.let { tag ->
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_chip))
                        ) {
                            Text(
                                text = tag.label.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(
                                    horizontal = dimensionResource(id = R.dimen.padding_medium),
                                    vertical = dimensionResource(id = R.dimen.padding_tiny)
                                )
                            )
                        }
                    }
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                    thickness = dimensionResource(id = R.dimen.border_thin)
                )

                item.summary?.let {
                    Text(
                        modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large)),
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
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
        // 💡 CHANGED: Matches Market Outlook background
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_card)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.section_domino_effect),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold
                )
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                thickness = dimensionResource(id = R.dimen.border_thin)
            )

            Column(modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))) {
                DominoTimelineStep(
                    title = stringResource(id = R.string.label_trigger).uppercase(),
                    text = trigger,
                    isLast = false
                )
                DominoTimelineStep(
                    title = stringResource(id = R.string.label_impact).uppercase(),
                    text = impact,
                    isLast = false
                )
                DominoTimelineStep(
                    title = stringResource(id = R.string.label_outlook).uppercase(),
                    text = outlook,
                    isLast = true
                )
            }
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
            modifier = Modifier
                .width(dimensionResource(id = R.dimen.timeline_node_width))
                .fillMaxHeight()
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
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(
                alpha = 0.4f
            )
        ),
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_card)),
        border = BorderStroke(
            dimensionResource(id = R.dimen.border_thin),
            MaterialTheme.colorScheme.secondaryContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            // 💡 ACTION: Market Outlook Title upgraded to titleMedium for higher visibility
            Text(
                modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large)),
                text = stringResource(id = R.string.section_market_outlook),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                thickness = dimensionResource(id = R.dimen.border_thin)
            )

            Text(
                modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large)),
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
fun VerdictCard(verdict: Verdict, onClick: () -> Unit) {
    val call = verdict.call ?: return
    val regime = verdict.regime ?: return
    val setup = verdict.setup ?: return

    val (bgColor, textColor) = when (call) {
        TradingCall.CONTRARIAN_BUY,
        TradingCall.ACCUMULATE -> Pair(BullishBg, BullishText)

        TradingCall.CONTRARIAN_SELL,
        TradingCall.SELL_AVOID,
        TradingCall.HEDGE_PROTECT -> Pair(BearishBg, BearishText)

        else -> Pair(NeutralBg, NeutralText)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_card)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() } // 💡 NEW: Made the card clickable
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimensionResource(id = R.dimen.padding_large)),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        color = textColor.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_chip))
                    ) {
                        Text(
                            text = regime.label,
                            color = textColor,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(
                                horizontal = dimensionResource(id = R.dimen.padding_medium),
                                vertical = dimensionResource(id = R.dimen.padding_tiny)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))

                    Text(
                        text = call.label,
                        style = MaterialTheme.typography.headlineSmall,
                        color = textColor
                    )

                    Text(
                        text = setup.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = textColor.copy(alpha = 0.8f)
                    )
                }

                // 💡 FIXED: Arrow is now properly aligned and themed to match the card text
                Icon(
                    painter = painterResource(id = R.drawable.ic_chevron_forward),
                    contentDescription = "View Glossary",
                    tint = textColor,
                    modifier = Modifier.size(dimensionResource(id = R.dimen.padding_large))
                )
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                thickness = dimensionResource(id = R.dimen.border_thin)
            )

            Text(
                modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large)),
                text = verdict.analysis ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
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
        // 💡 CHANGED: Matches Market Outlook background
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_card)),
        border = BorderStroke(
            dimensionResource(id = R.dimen.border_thin),
            MaterialTheme.colorScheme.primary
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Text(
                text = stringResource(id = R.string.section_action_plan),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))
            )
            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                thickness = dimensionResource(id = R.dimen.border_thin)
            )

            Text(
                modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large)),
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
fun SectionTitle(title: String, color: Color = MaterialTheme.colorScheme.onSurface) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        color = color,
        modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_medium))
    )
}