package com.marketlabs.pulse.ui.screens.summary.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.marketlabs.pulse.R
import com.marketlabs.pulse.storage.model.summary.DominoEffect
import com.marketlabs.pulse.storage.model.summary.MacroItem
import com.marketlabs.pulse.storage.model.summary.MarketDriver
import com.marketlabs.pulse.storage.model.summary.MarketPosition
import com.marketlabs.pulse.storage.model.summary.MarketPulse
import com.marketlabs.pulse.storage.model.summary.MarketVerdict
import com.marketlabs.pulse.storage.model.summary.NewsItem
import com.marketlabs.pulse.storage.model.summary.Positioning
import com.marketlabs.pulse.storage.model.summary.RiskItem
import com.marketlabs.pulse.storage.model.summary.Valuation
import com.marketlabs.pulse.storage.model.summary.WatchItem
import com.marketlabs.pulse.ui.components.PulseCard
import com.marketlabs.pulse.ui.components.PulseCardStyle
import com.marketlabs.pulse.ui.components.bottomSheet.DriversInfoBottomSheet
import com.marketlabs.pulse.ui.components.bottomSheet.MarketGlossaryBottomSheet
import com.marketlabs.pulse.ui.components.widgets.SignalPill
import com.marketlabs.pulse.ui.theme.LocalPulseColors
import com.marketlabs.pulse.ui.theme.MarketPulseTheme
import com.marketlabs.pulse.ui.theme.pillColor
import com.marketlabs.pulse.ui.theme.textColor
import com.marketlabs.pulse.utils.enums.Conviction
import com.marketlabs.pulse.utils.enums.DriverImpact
import com.marketlabs.pulse.utils.enums.IndicatorCategory
import com.marketlabs.pulse.utils.enums.MarketRegime
import com.marketlabs.pulse.utils.enums.ReportType
import com.marketlabs.pulse.utils.enums.RiskImpactLevel
import com.marketlabs.pulse.utils.enums.SignalColor
import com.marketlabs.pulse.utils.enums.SignalDirection
import com.marketlabs.pulse.utils.enums.TechnicalSetup
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
    scaffoldPadding: PaddingValues,
    onNavigateToIndicators: () -> Unit
) {
    val paddingLarge = dimensionResource(id = R.dimen.padding_large)

    // 💡 Which glossary sheet (if any) is open, and for which term -- regime/direction, setup,
    // and cycle zone each have their own tap target now (a chevron on that one chip) instead of
    // one card-wide tap opening a combined sheet, so this needs to track *which* term was tapped,
    // not just open/closed.
    var glossaryTarget by remember { mutableStateOf<GlossaryTarget?>(null) }
    var showDriversInfo by remember { mutableStateOf(false) }

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

            // 💡 market_pulse v2 hierarchy (2026-08-17 backend revamp): signal -> drivers ->
            // position -> lead stories -> macro mix -> domino -> watch & risks -> the read.
            // Signal (top) and The Read (bottom) both bind to the same MarketVerdict --
            // signalLine at top, verdict_text (analysis) at bottom -- since the backend
            // consolidated what used to be a separate signal + the_read split into one object.
            validData.verdict?.let { verdict ->
                item {
                    SignalSection(
                        verdict = verdict,
                        onRegimeClick = { glossaryTarget = GlossaryTarget.REGIME })
                }
            }

            val drivers = validData.drivers
            if (!drivers.isNullOrEmpty()) {
                item {
                    DriversSectionHeader(
                        onClick = onNavigateToIndicators,
                        onInfoClick = { showDriversInfo = true }
                    )
                }
                item { DriversSection(drivers) }
            }

            validData.position?.let { position ->
                item {
                    SectionTitle(
                        title = stringResource(id = R.string.section_market_position),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                item {
                    MarketPositionSection(
                        position = position,
                        setup = validData.verdict?.setup,
                        whatChanged = validData.whatChanged,
                        onSetupClick = { glossaryTarget = GlossaryTarget.SETUP },
                        onCycleZoneClick = { glossaryTarget = GlossaryTarget.CYCLE_ZONE }
                    )
                }
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

            validData.dominoEffect?.let { domino ->
                item {
                    SectionTitle(
                        title = stringResource(id = R.string.section_domino_effect),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                item { DominoCard(domino) }
            }

            val watch = validData.watch
            if (!watch.isNullOrEmpty()) {
                item {
                    SectionTitle(
                        title = stringResource(id = R.string.section_watch),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                item { WatchSection(watch) }
            }

            val risks = validData.risks
            if (!risks.isNullOrEmpty()) {
                item {
                    SectionTitle(
                        title = stringResource(id = R.string.section_risks),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                item { RisksSection(risks) }
            }

            validData.verdict?.let { verdict ->
                item {
                    SectionTitle(
                        title = stringResource(id = R.string.section_the_read),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                item { TheReadSection(verdict) }
            }
            item {
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_standard)))
            }
        }
    }

    // 💡 One sheet per tapped chip, not one combined sheet for the whole card -- `call` no longer
    // exists on MarketVerdict (the backend removed it project-wide) so there's nothing left that
    // needs a "verdict-wide" glossary. Regime's sheet also carries direction (never its own text,
    // only this chip's tint) since they're the two things that one chip communicates together.
    when (glossaryTarget) {
        GlossaryTarget.REGIME -> {
            MarketGlossaryBottomSheet(
                currentRegime = data?.verdict?.regime?.label?.uppercase(),
                currentDirection = data?.verdict?.direction?.label?.uppercase(),
                onDismiss = { glossaryTarget = null }
            )
        }

        GlossaryTarget.SETUP -> {
            MarketGlossaryBottomSheet(
                currentSetup = data?.verdict?.setup?.label?.uppercase(),
                onDismiss = { glossaryTarget = null }
            )
        }

        GlossaryTarget.CYCLE_ZONE -> {
            MarketGlossaryBottomSheet(
                currentCycleZone = data?.position?.cycleZone,
                onDismiss = { glossaryTarget = null }
            )
        }

        null -> {}
    }

    if (showDriversInfo) {
        DriversInfoBottomSheet(onDismiss = { showDriversInfo = false })
    }
}

private enum class GlossaryTarget { REGIME, SETUP, CYCLE_ZONE }

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
                contentDescription = stringResource(id = R.string.summary_analysis_engine_content_description),
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
 * The top-of-screen Signal card and the closing "The Read" card both read from the same
 * [MarketVerdict] (the backend folded the old signal + the_read split into one object). This one
 * renders the glanceable top flash: `regime` as the one headline chip, tinted and arrow-marked by
 * `direction` (RISK_ON/RISK_OFF/MIXED) rather than `direction` getting its own redundant text
 * pill -- the color/arrow *is* the direction read, not a second word saying the same thing.
 * `signal_line` is the largest prose on the card (the flash itself); `conviction` +
 * `convictionReason` render together, always inline, never behind a tap -- this is the field that
 * makes the verdict earned confidence instead of a black-box "trust me," and seeing e.g. "2 of 4"
 * signals aligned is what makes the mixed drivers underneath read as a contested call rather than
 * a contradiction. `setup` moved to the Market Position card. The card itself is no longer a tap
 * target -- only the regime chip is (trailing chevron), opening the regime+direction glossary,
 * since direction is only ever expressed as this chip's tint, never its own text.
 */
@Composable
fun SignalSection(verdict: MarketVerdict, onRegimeClick: () -> Unit) {
    val directionColor = verdict.direction.toSignalColor()
    // 💡 Same ▲/▼/▪ glyph convention DriversSection/ScoreGauge already use.
    val directionGlyph = when (verdict.direction) {
        SignalDirection.RISK_ON -> "▲"
        SignalDirection.RISK_OFF -> "▼"
        SignalDirection.MIXED -> "▪"
        SignalDirection.UNKNOWN, null -> null
    }

    PulseCard(
        style = PulseCardStyle.SYNTHESIS,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Column(modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))) {
                Text(
                    text = stringResource(id = R.string.section_signal),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )

                verdict.regime?.let {
                    SignalPill(
                        text = it.label,
                        pillColor = directionColor.pillColor,
                        contentColor = directionColor.textColor,
                        leadingIcon = directionGlyph?.let { glyph ->
                            {
                                Text(
                                    text = glyph,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = directionColor.textColor
                                )
                            }
                        },
                        trailingIcon = { GlossaryChevron(tint = directionColor.textColor) },
                        onClick = onRegimeClick,
                        modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_small))
                    )
                }

                // 💡 The flash -- largest prose on the card, no callout box around it; a headline
                // doesn't need to be quoted. bodyMedium, not titleSmall/bold -- a full ~170-char
                // sentence read as too heavy at the bolder title weight.
                verdict.signalLine?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_medium))
                    )
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                thickness = dimensionResource(id = R.dimen.border_thin)
            )

            Column(modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))) {
                if (verdict.conviction != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(id = R.string.label_conviction),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_medium)))
                        // 💡 .textColor, not .pillColor -- pillColor is tuned to be a soft background a
                        // bolder text color sits on top of, so it read as a near-invisible fill when
                        // used as the filled color of a bar itself. .textColor is the more saturated
                        // half of the pair, tuned to already read clearly as a foreground color.
                        ConvictionMeter(
                            conviction = verdict.conviction,
                            filledColor = directionColor.textColor
                        )
                    }

                    val reason = verdict.convictionReason?.stripRegimeToken()
                    Text(
                        text = buildAnnotatedString {
                            withStyle(
                                SpanStyle(
                                    fontWeight = FontWeight.Bold,
                                    color = directionColor.textColor
                                )
                            ) {
                                append(verdict.conviction.name)
                            }
                            if (!reason.isNullOrBlank()) {
                                append(" · ")
                                append(reason)
                            }
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_tiny))
                    )
                }
            }
        }
    }
}

// verdict.convictionReason is code-generated (pulseSignal.ts's deriveConviction():
// "${agree} of 4 signals aligned ${regimeDoc.regime}...") -- regimeDoc.regime is system/
// market_regime's own internal snake_case token ("risk_on"/"risk_off"/"neutral"). Stripped here
// rather than shown or prettified: the regime word is redundant with the chip directly above this
// text, and "2 of 4 signals aligned" reads as a complete thought on its own without it.
private fun String.stripRegimeToken(): String = this
    .replace(Regex("\\s*(risk_on|risk_off|neutral)\\s*", RegexOption.IGNORE_CASE), " ")
    .replace(Regex("\\s+([.,)])"), "$1")
    .replace(Regex("\\s{2,}"), " ")
    .trim()

private fun SignalDirection?.toSignalColor(): SignalColor = when (this) {
    SignalDirection.RISK_ON -> SignalColor.GREEN
    SignalDirection.RISK_OFF -> SignalColor.RED
    SignalDirection.MIXED -> SignalColor.YELLOW
    SignalDirection.UNKNOWN, null -> SignalColor.UNKNOWN
}

// 💡 The backend doesn't send a color for `setup` the way it does for driver direction or
// market_position's signal_color -- setup is read contrarian throughout this app (see
// MarketGlossary.kt's definitions: EXHAUSTED_OVERSOLD/OVERSOLD read as buying opportunities,
// OVERBOUGHT/BLOW_OFF_TOP as danger), so the color has to be derived client-side from that same
// contrarian reading, not a literal "up = green" mapping. `regime` no longer gets an equivalent
// mapping here -- its chip is tinted by `direction` (the code-derived aggregate read) instead of
// a second, client-guessed color for the same card.
private fun TechnicalSetup?.toSignalColor(): SignalColor = when (this) {
    TechnicalSetup.EXHAUSTED_OVERSOLD, TechnicalSetup.OVERSOLD -> SignalColor.GREEN
    TechnicalSetup.NEUTRAL_MEAN -> SignalColor.YELLOW
    TechnicalSetup.OVERBOUGHT, TechnicalSetup.BLOW_OFF_TOP, TechnicalSetup.BEARISH_DIVERGENCE -> SignalColor.RED
    TechnicalSetup.UNKNOWN, null -> SignalColor.UNKNOWN
}

// cycle_zone is a free-text field (system/market_regime's risk_on/risk_off/neutral, relabeled --
// see marketPulseComposer.ts's deriveCycleZone), always one of exactly these three phrases or
// null, matched by substring rather than exact equality so a future label tweak (e.g. added
// punctuation) degrades to UNKNOWN instead of breaking silently.
private fun String?.cycleZoneToSignalColor(): SignalColor = when {
    this == null -> SignalColor.UNKNOWN
    this.contains("EXPANSION", ignoreCase = true) -> SignalColor.GREEN
    this.contains("CONTRACTION", ignoreCase = true) -> SignalColor.RED
    this.contains("TRANSITION", ignoreCase = true) -> SignalColor.YELLOW
    else -> SignalColor.UNKNOWN
}

/**
 * A 3-segment bar, filled left-to-right by [conviction] (LOW = 1, MODERATE = 2, HIGH = 3) --
 * a compact at-a-glance strength read next to the "CONVICTION" label, rather than relying on the
 * word alone to carry how strong a LOW vs. HIGH read is.
 */
@Composable
private fun ConvictionMeter(conviction: Conviction, filledColor: Color) {
    val filledSegments = when (conviction) {
        Conviction.LOW -> 1
        Conviction.MODERATE -> 2
        Conviction.HIGH -> 3
        Conviction.UNKNOWN -> 0
    }
    val emptyColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
    Row(horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_tiny))) {
        repeat(3) { index ->
            Box(
                modifier = Modifier
                    .width(dimensionResource(id = R.dimen.padding_xlarge))
                    .height(dimensionResource(id = R.dimen.padding_small))
                    .background(
                        color = if (index < filledSegments) filledColor else emptyColor,
                        shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_chip))
                    )
            )
        }
    }
}

/**
 * Section header for Drivers -- the title row navigates to the Indicators tab on tap (using the
 * same tab-preserving `popUpTo`/`launchSingleTop`/`restoreState` pattern the bottom nav bar
 * itself uses -- see `MainActivity.kt`'s `FloatingBottomNav` `onItemClick`), trailing chevron.
 * Drivers are indicator names (Retail Sales, Crude Oil, ...); Indicators is where their full
 * detail actually lives.
 *
 * The info icon is its own separate tap target (opens [com.marketlabs.pulse.ui.components.
 * bottomSheet.DriversInfoBottomSheet]), not part of the row's navigate-to-Indicators click --
 * a nested `clickable` consumes its own taps before they reach the row's, same as
 * `DataCardTitleWithInfo`'s icon does inside its own row elsewhere in the app. Exists because
 * `drivers[].direction` now means "this driver's net effect on equities," not "the underlying
 * indicator's own reading" (backend change, 2026-08-18) -- a distinction a color/arrow alone
 * can't communicate.
 */
@Composable
private fun DriversSectionHeader(onClick: () -> Unit, onInfoClick: () -> Unit) {
    // 💡 The clickable is on this outer Row now, not just the title+info-icon inner group -- it
    // used to stop short of the chevron, so the one part of the row visually signaling "tap here"
    // was the one part that wasn't actually tappable. The info icon still carves out its own
    // nested tap target inside (a nested `clickable` consumes its own taps before they reach this
    // outer one, same as everywhere else in this file that pairs the two).
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(top = dimensionResource(id = R.dimen.padding_medium)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = R.string.section_drivers),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_small)))
            Icon(
                painter = painterResource(id = R.drawable.ic_info),
                contentDescription = stringResource(id = R.string.drivers_info_content_description),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(dimensionResource(id = R.dimen.icon_size_small))
                    .clickable(onClick = onInfoClick)
            )
        }
        Icon(
            painter = painterResource(id = R.drawable.ic_chevron_forward),
            contentDescription = stringResource(id = R.string.drivers_navigate_content_description),
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(dimensionResource(id = R.dimen.padding_large))
        )
    }
}

/**
 * The drivers[] chip row -- same `FlowRow` + `SignalPill` pattern `ConditionChipRow.kt` (the
 * stock domain's condition-chip row) uses, one step cleaner here since `MarketDriver.direction`
 * is already a real `SignalColor` from the mapper (never a raw string needing a hand-rolled
 * `when`). Sorted HIGH-impact first, same "most-important-first" sort `WatchList.kt` uses for its
 * own urgency field.
 *
 * 💡 No ▲/▼/▪ glyph on the pill (there was one briefly) -- an arrow reads as "the data itself
 * went up/down," which stopped being true once `direction` became the model's reconciled
 * effect-on-equities call rather than a mechanical copy of the indicator's own reading (backend
 * change, 2026-08-18; see [DriversInfoBottomSheet]). The color alone still carries that signal;
 * the glyph was adding a second, now-misleading claim on top of it.
 */
@Composable
fun DriversSection(drivers: List<MarketDriver>) {
    val sorted = drivers.sortedBy { if (it.impact == DriverImpact.HIGH) 0 else 1 }

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_small)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_small))
    ) {
        sorted.forEach { driver ->
            SignalPill(
                text = driver.label ?: "",
                pillColor = driver.direction.pillColor,
                contentColor = driver.direction.textColor
            )
        }
    }
}

/**
 * market_position -- a Reader-side join, already merged into the same response as everything
 * else (not a second network call). Every sub-block can be null on cold start, so each one
 * renders independently. `signal_text`/`signal_color` are pre-classified backend-side; rendered
 * directly, no client threshold logic.
 */
@Composable
fun MarketPositionSection(
    position: MarketPosition,
    setup: TechnicalSetup?,
    whatChanged: String?,
    onSetupClick: () -> Unit,
    onCycleZoneClick: () -> Unit
) {
    PulseCard(style = PulseCardStyle.DATA, modifier = Modifier.fillMaxWidth()) {
        Column {
            Column(modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))) {
                // 💡 setup lands here now, not on the Signal card or The Read -- it's a technical
                // read on where price sits (oversold/overbought/...), which fits this card's own
                // subject better than the verdict cards' regime/conviction focus. Both chips are
                // their own tap target (trailing chevron), each opening a glossary scoped to just
                // that term -- same pattern the Signal card's regime chip uses.
                if (position.cycleZone != null || setup != null) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_small)),
                        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_small))
                    ) {
                        position.cycleZone?.let {
                            val color = it.cycleZoneToSignalColor()
                            SignalPill(
                                text = it,
                                pillColor = color.pillColor,
                                contentColor = color.textColor,
                                trailingIcon = { GlossaryChevron(tint = color.textColor) },
                                onClick = onCycleZoneClick
                            )
                        }
                        setup?.let {
                            val color = it.toSignalColor()
                            SignalPill(
                                text = it.label,
                                pillColor = color.pillColor,
                                contentColor = color.textColor,
                                trailingIcon = { GlossaryChevron(tint = color.textColor) },
                                onClick = onSetupClick
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_standard)))
                }

                position.positioning?.let { positioning ->
                    PositioningGauge(
                        rangePosition = positioning.rangePosition,
                        windowLabel = positioning.windowLabel,
                        signalText = positioning.signalText,
                        signalColor = positioning.signalColor
                    )

                    val windowLabel =
                        positioning.windowLabel
                            ?: stringResource(id = R.string.label_window_fallback)
                    val pctText = listOfNotNull(
                        positioning.pctFrom52wHigh?.let {
                            stringResource(
                                id = R.string.label_pct_from_high,
                                it,
                                windowLabel
                            )
                        },
                        positioning.pctFrom52wLow?.let {
                            stringResource(
                                id = R.string.label_pct_from_low,
                                it,
                                windowLabel
                            )
                        }
                    ).joinToString(",\n")
                    if (pctText.isNotBlank()) {
                        Text(
                            text = pctText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_medium))
                        )
                    }
                }

            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                thickness = dimensionResource(id = R.dimen.border_thin)
            )

            Column(modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))) {

                // 💡 horizons (SHORT/MEDIUM/LONG risk_level + key_driver) deliberately not rendered
                // here -- staying in the domain model/cache, just not surfaced on this screen; it's
                // landing on the Indicators tab instead once that work starts.

                val footerLines = listOfNotNull(
                    position.valuation?.pePercentile5y?.let { "${stringResource(id = R.string.label_valuation)} $it" },
                    whatChanged?.let { "${stringResource(id = R.string.label_what_changed)} $it" }
                )
                footerLines.forEach {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_small))
                    )
                }
            }
        }
    }
}

/**
 * The watch[] list -- `DATA` style cards (same as [LeadStoryCard]/[MacroCard]), timeframe using
 * the same [TagPill] [MacroCard]'s tag does.
 */
@Composable
fun WatchSection(watch: List<WatchItem>) {
    Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_medium))) {
        watch.forEach { item ->
            PulseCard(style = PulseCardStyle.DATA, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))) {
                    // 💡 CenterVertically, not Top -- SignalPill/TagPill's own internal padding
                    // sits their text lower than a plain Text's top edge, so top-aligning the row
                    // made the title and the pill look visually offset from each other.
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.label ?: "",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        item.timeframe?.let {
                            TagPill(text = it)
                        }
                    }
                    item.why?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_small))
                        )
                    }
                }
            }
        }
    }
}

/**
 * The risks[] list -- `DATA` style cards, severity colored through the same [RiskImpactLevel]
 * color extension [HorizonChip] uses (`ui/theme/SignalColorExtensions.kt`), not a hand-rolled
 * `when` here.
 */
@Composable
fun RisksSection(risks: List<RiskItem>) {
    Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_medium))) {
        risks.forEach { item ->
            PulseCard(style = PulseCardStyle.DATA, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))) {
                    // 💡 CenterVertically, not Top -- see WatchSection's identical fix above.
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.risk ?: "",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        item.severity?.let {
                            SignalPill(
                                text = it.label.uppercase(),
                                pillColor = it.pillColor,
                                contentColor = it.textColor
                            )
                        }
                    }
                    item.note?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_small))
                        )
                    }
                }
            }
        }
    }
}

/**
 * The closing "The Read" card -- [MarketVerdict.analysis] is the verdict_text field,
 * [MarketVerdict.posture] is the renamed-from-action field. posture is descriptive market-stance
 * text, never an instruction -- rendered as a small labeled observation inside this same
 * SYNTHESIS card, never under an "action plan"-style header or its own card.
 */
@Composable
fun TheReadSection(verdict: MarketVerdict) {
    val paddingLarge = dimensionResource(id = R.dimen.padding_large)

    // 💡 No regime/setup pills here anymore -- regime is already the Signal card's headline chip
    // and repeating it here was the exact redundancy this redesign set out to remove; setup moved
    // to Market Position. The divider sits between two separately-padded Columns rather than
    // inside one Column padded as a whole -- same structural idiom LeadStoryCard/MacroCard/
    // DominoStepCard already use -- so it spans the card's full width instead of stopping short
    // at the content inset.
    PulseCard(
        style = PulseCardStyle.SYNTHESIS,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = dimensionResource(id = R.dimen.padding_small))
    ) {
        Column {
            Column(
                modifier = Modifier.padding(
                    horizontal = paddingLarge,
                    vertical = paddingLarge,
                )
            ) {
                Text(
                    text = stringResource(id = R.string.market_read).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                // 💡 The depth -- the full paragraph, for the user who scrolls this far.
                verdict.analysis?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // 💡 posture rendered as its own distinct, scannable "positioning" beat -- separated
            // from verdict_text by the divider rather than tacked on as another paragraph in the
            // same block, exactly so it doesn't read as a third restatement of the same thesis.
            verdict.posture?.let { posture ->
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                    thickness = dimensionResource(id = R.dimen.border_thin)
                )
                Column(
                    modifier = Modifier.padding(
                        horizontal = paddingLarge,
                        vertical = paddingLarge
                    )
                ) {
                    Text(
                        text = stringResource(id = R.string.label_posture),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = posture,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_tiny))
                    )
                }
            }
        }
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

                    item.tag?.let { tag -> TagPill(text = tag.label) }
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
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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

/**
 * The neutral tag/timeframe pill shared by [MacroCard] and [WatchSection] -- one component, not
 * two independently-styled ones, so a Macro Mix tag and a Watch timeframe read as the same visual
 * language rather than two different chips that happen to share a color.
 */
@Composable
private fun TagPill(text: String) {
    val pulseColors = LocalPulseColors.current
    Surface(
        color = pulseColors.accentPrimary,
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_chip))
    ) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = pulseColors.accentOn,
            modifier = Modifier.padding(
                horizontal = dimensionResource(id = R.dimen.padding_medium),
                vertical = dimensionResource(id = R.dimen.padding_tiny)
            )
        )
    }
}

/**
 * The small trailing chevron marking a [SignalPill] as a tap target that opens a glossary sheet
 * scoped to just that term -- regime, setup, and cycle-zone chips all use this. `contentDescription`
 * is null (decorative): the pill's own text is already what a screen reader announces for this tap
 * target, so a second "chevron forward" announcement on top of it would be redundant, unlike
 * [DriversSectionHeader]'s standalone chevron, which isn't paired with its own adjacent label text.
 */
@Composable
private fun GlossaryChevron(tint: Color) {
    Icon(
        painter = painterResource(id = R.drawable.ic_chevron_forward),
        contentDescription = null,
        tint = tint,
        modifier = Modifier.size(dimensionResource(id = R.dimen.icon_size_small))
    )
}

// ---------------------------------------------------------
// PREVIEWS
// ---------------------------------------------------------

private val previewVerdict = MarketVerdict(
    regime = MarketRegime.SIDEWAYS_RANGE,
    setup = TechnicalSetup.NEUTRAL_MEAN,
    signalLine = "Breaking Middle East maritime escalations and a dead US-Iran ceasefire are injecting stagflationary oil risks directly into a domestic consumer slowdown.",
    analysis = "The deterministic signal correctly identifies a completely neutral tape, requiring a continuation of the SIDEWAYS RANGE regime.",
    posture = "Capital is hiding in short-duration paper and defensive staples, refusing to commit to cyclical growth.",
    direction = SignalDirection.MIXED,
    conviction = Conviction.LOW,
    convictionReason = "Signals mixed -- no clear directional consensus."
)

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun PreviewSignalSection() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        Column(modifier = Modifier.padding(16.dp)) {
            SignalSection(verdict = previewVerdict, onRegimeClick = {})
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun PreviewDriversSectionHeader() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        Column(modifier = Modifier.padding(16.dp)) {
            DriversSectionHeader(onClick = {}, onInfoClick = {})
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun PreviewDriversSection() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        Column(modifier = Modifier.padding(16.dp)) {
            DriversSection(
                listOf(
                    MarketDriver(
                        label = "Retail Sales (MoM)",
                        direction = SignalColor.RED,
                        category = IndicatorCategory.MACRO_ECONOMY,
                        impact = DriverImpact.HIGH
                    ),
                    MarketDriver(
                        label = "Crude Oil",
                        direction = SignalColor.GREEN,
                        category = IndicatorCategory.SYSTEMIC_RISK,
                        impact = DriverImpact.HIGH
                    ),
                    MarketDriver(
                        label = "Credit Spreads (High Yield)",
                        direction = SignalColor.GREEN,
                        category = IndicatorCategory.SYSTEMIC_RISK,
                        impact = DriverImpact.MODERATE
                    ),
                    MarketDriver(
                        label = "Nonfarm Payrolls",
                        direction = SignalColor.RED,
                        category = IndicatorCategory.MACRO_ECONOMY,
                        impact = DriverImpact.MODERATE
                    )
                )
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun PreviewMarketPositionSection() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        Column(modifier = Modifier.padding(16.dp)) {
            MarketPositionSection(
                position = MarketPosition(
                    positioning = Positioning(
                        pctFrom52wHigh = -1.28,
                        pctFrom52wLow = 21.51,
                        rangePosition = 93.17,
                        extensionPercentile1y = 96.4,
                        windowLabel = "251-day",
                        signalText = "Extended Near Highs",
                        signalColor = SignalColor.RED
                    ),
                    valuation = Valuation(pePercentile5y = 71.0),
                    cycleZone = "TRANSITION ZONE"
                ),
                setup = TechnicalSetup.OVERBOUGHT,
                whatChanged = "Retail sales missed, pulling forward Fed cut odds.",
                onSetupClick = {},
                onCycleZoneClick = {}
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun PreviewWatchSection() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        Column(modifier = Modifier.padding(16.dp)) {
            WatchSection(
                listOf(
                    WatchItem(
                        label = "CENTCOM Maritime Interdictions",
                        timeframe = "Over the next 48 hours",
                        why = "Any transition to kinetic engagement will spike war-risk insurance premiums."
                    )
                )
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun PreviewRisksSection() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        Column(modifier = Modifier.padding(16.dp)) {
            RisksSection(
                listOf(
                    RiskItem(
                        risk = "Strait of Hormuz Blockade or Retaliation",
                        severity = RiskImpactLevel.HIGH,
                        note = "Aggressive US vessel boardings create a hair-trigger environment for a maritime clash."
                    )
                )
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun PreviewDominoCard() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DominoCard(
                domino = DominoEffect(
                    trigger = "Expiration of the US-Iran memorandum of understanding and subsequent CENTCOM commercial vessel interdictions in the Gulf.",
                    impact = "Crude oil catches a sustained bid on supply chain fears, elevating breakeven inflation expectations.",
                    outlook = "This is a trend shift toward structurally higher geopolitical risk premiums."
                )
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun PreviewTheReadSection() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        Column(modifier = Modifier.padding(16.dp)) {
            TheReadSection(verdict = previewVerdict)
        }
    }
}
