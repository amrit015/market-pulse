package com.marketlabs.pulse.ui.screens.summary.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.marketlabs.pulse.R
import com.marketlabs.pulse.storage.model.summary.DominoEffect
import com.marketlabs.pulse.storage.model.summary.MacroItem
import com.marketlabs.pulse.storage.model.summary.MarketOutlook
import com.marketlabs.pulse.storage.model.summary.MarketPulse
import com.marketlabs.pulse.storage.model.summary.NewsItem
import com.marketlabs.pulse.storage.model.summary.Verdict
import com.marketlabs.pulse.ui.components.PulseCard
import com.marketlabs.pulse.ui.components.PulseCardStyle
import com.marketlabs.pulse.ui.components.bottomSheet.MarketGlossaryBottomSheet
import com.marketlabs.pulse.ui.components.widgets.SignalPill
import com.marketlabs.pulse.ui.theme.LocalPulseColors
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
    scaffoldPadding: PaddingValues
) {
    val paddingLarge = dimensionResource(id = R.dimen.padding_large)

    // 💡 NEW: State to track if the Verdict Glossary bottom sheet is open
    var showVerdictGlossary by remember { mutableStateOf(false) }

    // 💡 Top padding uses `scaffoldPadding`'s top component (the Scaffold's own measurement of
    // the top bar's real rendered height) instead of the raw status bar inset alone -- the raw
    // inset only accounts for the system status bar, not the app's own top bar sitting below it,
    // so content used to start underneath the top bar rather than below it.
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(
            top = scaffoldPadding.calculateTopPadding() + paddingLarge,
            bottom = scaffoldPadding.calculateBottomPadding() + paddingLarge,
            start = paddingLarge,
            end = paddingLarge
        ),
        verticalArrangement = Arrangement.spacedBy(paddingLarge)
    ) {

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
 * Header section - Market Summary
 */
@Composable
fun HeaderSection(type: ReportType, timestamp: Long) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Extract the exact text style you are using
            val textStyle = MaterialTheme.typography.headlineMedium

            // 2. Convert its font size (sp) into a Compose dimension (dp)
            val iconSize = with(LocalDensity.current) { textStyle.fontSize.toDp() }

            Icon(
                painter = painterResource(id = R.drawable.ic_engine_ai_sparkles),
                contentDescription = "Analysis Engine",
                tint = MaterialTheme.colorScheme.onBackground,
                // 3. Apply the calculated size here
                modifier = Modifier.size(iconSize)
            )

            Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_small)))

            Text(
                text = type.label,
                // 4. Use the exact same style reference here
                style = textStyle,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

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

    // 💡 DATA style -- was SYNTHESIS (an AI-summarized lead story, treated as AI content). Only
    // VerdictCard's own explicit verdict/call and Indicators' AI Executive Briefing keep the
    // darker SYNTHESIS background now; every other card, including this one, reads as the same
    // background every data-display card in the app uses. Replaces the old
    // `secondaryContainer.copy(alpha = 0.4f)` leftover from before this app had its own token
    // system.
    PulseCard(
        style = PulseCardStyle.DATA,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Text(
                modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large)),
                text = headline,
                // 💡 titleSmall (15sp, semi-bold), not titleMedium -- the consistent title tier
                // every curated/AI-content card title uses, separate from DATA-style cards
                // (Equities, VIX, Indicators), which keep their bold 17sp title.
                style = MaterialTheme.typography.titleSmall,
                // 💡 Card titles are always onSurface (dark-on-light/white-on-dark) across this
                // app -- same treatment as Equities/AI/News cards. Was `colorScheme.secondary`
                // (mapped to the muted onSurfaceMuted tone), which read as washed-out next to
                // those cards' bold titles.
                color = MaterialTheme.colorScheme.onSurface
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

    // 💡 DATA style -- see LeadStoryCard above.
    PulseCard(
        style = PulseCardStyle.DATA,
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
                        // 💡 Card titles are always onSurface -- see LeadStoryCard above.
                        color = MaterialTheme.colorScheme.onSurface,
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

    // 💡 DATA style -- see LeadStoryCard above.
    PulseCard(
        style = PulseCardStyle.DATA,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.section_domino_effect),
                    // 💡 titleSmall (15sp, semi-bold) -- see LeadStoryCard above. The separate
                    // `fontWeight = Bold` override is gone too, since titleSmall's own semi-bold
                    // is the target weight, not bold.
                    style = MaterialTheme.typography.titleSmall,
                    // 💡 Card titles are always onSurface -- see LeadStoryCard above.
                    color = MaterialTheme.colorScheme.onSurface
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
            // 💡 "TRIGGER"/"IMPACT"/"OUTLOOK" -- a label naming the text below it, not a date, so
            // it follows the same onSurface rule as every other card label. Was `colorScheme.
            // secondary` (mapped to the muted onSurfaceMuted tone).
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
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
    // 💡 DATA style -- was SYNTHESIS (an AI-written market outlook, treated as AI content) -- see
    // LeadStoryCard above for why only VerdictCard and the Executive Briefing keep that treatment
    // now. Replaces the old `secondaryContainer.copy(alpha = 0.4f)` fill and its matching
    // (same-color, essentially invisible) border -- both leftovers from before this app had its
    // own token system.
    PulseCard(
        style = PulseCardStyle.DATA,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Text(
                modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large)),
                text = stringResource(id = R.string.section_market_outlook),
                // 💡 titleSmall (15sp, semi-bold) -- see LeadStoryCard above.
                style = MaterialTheme.typography.titleSmall,
                // 💡 Card titles are always onSurface -- see LeadStoryCard above.
                color = MaterialTheme.colorScheme.onSurface
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

    val pulseColors = LocalPulseColors.current
    // 💡 The sector rotation heatmap is the only place in this app where a signal color is allowed
    // to own an entire card/tile background -- every other directional card, this one included,
    // uses a uniform accent-neutral fill instead. Direction still reads clearly from the regime
    // pill, the call headline, and the setup subtitle, all keyed off the same signal text/pill pair.
    val (pillColor, textColor) = when (call) {
        TradingCall.CONTRARIAN_BUY,
        TradingCall.ACCUMULATE -> pulseColors.signalBullishPill to pulseColors.signalBullishText

        TradingCall.CONTRARIAN_SELL,
        TradingCall.SELL_AVOID,
        TradingCall.HEDGE_PROTECT -> pulseColors.signalBearishPill to pulseColors.signalBearishText

        else -> pulseColors.signalNeutralPill to pulseColors.signalNeutralText
    }

    // 💡 SYNTHESIS style -- this is the AI Verdict (regime, call, setup), the same kind of
    // AI-interpreted content as the Technical Briefing and news cards, not a raw price reading.
    // Was `surfaceTinted` (the DATA style price cards use) with no border; moved here for the same
    // reason Technical Briefing wears the accent tint -- this card is an AI's synthesis of the
    // data, not the data itself.
    PulseCard(
        style = PulseCardStyle.SYNTHESIS,
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
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
                    SignalPill(text = regime.label, pillColor = pillColor, contentColor = textColor)

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
    // 💡 DATA style -- was SYNTHESIS (the AI report's closing action plan, treated as AI content)
    // -- see LeadStoryCard above for why only VerdictCard and the Executive Briefing keep that
    // treatment now. Replaces the old `secondaryContainer.copy(alpha = 0.4f)` fill and its bold
    // `primary`-colored border with the same hairline every other card in the app uses -- both
    // leftovers from before this app had its own token system.
    PulseCard(
        style = PulseCardStyle.DATA,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Text(
                text = stringResource(id = R.string.section_action_plan),
                // 💡 titleSmall (15sp, semi-bold) -- see LeadStoryCard above.
                style = MaterialTheme.typography.titleSmall,
                // 💡 Card titles are always onSurface -- see LeadStoryCard above.
                color = MaterialTheme.colorScheme.onSurface,
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