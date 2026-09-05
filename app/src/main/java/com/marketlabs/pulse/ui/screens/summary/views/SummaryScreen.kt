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
import com.marketlabs.pulse.storage.model.summary.MarketSentiment
import com.marketlabs.pulse.storage.model.summary.MarketVerdict
import com.marketlabs.pulse.storage.model.summary.NewsItem
import com.marketlabs.pulse.storage.model.summary.Positioning
import com.marketlabs.pulse.storage.model.summary.RiskItem
import com.marketlabs.pulse.storage.model.summary.Valuation
import com.marketlabs.pulse.storage.model.summary.WatchItem
import com.marketlabs.pulse.storage.model.summary.WhatsNewItem
import com.marketlabs.pulse.ui.components.AnalyzedAtHeader
import com.marketlabs.pulse.ui.components.PulseCard
import com.marketlabs.pulse.ui.components.PulseCardStyle
import com.marketlabs.pulse.ui.components.bottomSheet.DriversInfoBottomSheet
import com.marketlabs.pulse.ui.components.bottomSheet.MarketGlossaryBottomSheet
import com.marketlabs.pulse.ui.components.widgets.CardEyebrowLabel
import com.marketlabs.pulse.ui.components.widgets.SignalPill
import com.marketlabs.pulse.ui.theme.LocalPulseColors
import com.marketlabs.pulse.ui.theme.MarketPulseTheme
import com.marketlabs.pulse.ui.theme.pillColor
import com.marketlabs.pulse.ui.theme.textColor
import com.marketlabs.pulse.utils.enums.Conviction
import com.marketlabs.pulse.utils.enums.DriverImpact
import com.marketlabs.pulse.utils.enums.IndicatorCategory
import com.marketlabs.pulse.utils.enums.MarketRegime
import com.marketlabs.pulse.utils.enums.RiskImpactLevel
import com.marketlabs.pulse.utils.enums.SignalColor
import com.marketlabs.pulse.utils.enums.SignalDirection
import com.marketlabs.pulse.utils.enums.TechnicalSetup
import java.text.ParseException
import java.text.SimpleDateFormat
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
    onNavigateToIndicators: () -> Unit,
    // spec-20260902-market-sentiment-android.md: the Market Sentiment card's whole-card tap
    // target -- Posture is a tab on the Insights screen, not its own destination, same shape as
    // onNavigateToIndicators above but landing on a specific Insights tab.
    onNavigateToPosture: () -> Unit = {}
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
            top = scaffoldPadding.calculateTopPadding(),
            bottom = scaffoldPadding.calculateBottomPadding() + paddingLarge,
            start = paddingLarge,
            end = paddingLarge
        ),
        verticalArrangement = Arrangement.spacedBy(paddingLarge)
    ) {

        data?.let { validData ->

            // 💡 The old in-content header (icon + report-type label + "Analyzed as of") is gone
            // -- the report-type label now lives in the global top bar (MainActivity resolves it
            // dynamically per the loaded ReportType, see MarketSummaryRoute's onReportTypeLoaded),
            // so keeping it here would have said the same thing twice. Only the timestamp stays,
            // via the same shared AnalyzedAtHeader the Indicators tab uses, so both screens' "as
            // of" line sits at the exact same pixel offset from the top bar (same content-padding
            // formula, same component, no internal padding of its own).
            item { AnalyzedAtHeader(timestamp = validData.lastUpdated) }

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

            // spec-20260902-market-sentiment-android.md: placed directly below the primary
            // verdict/read block, grouped with the narrative sections rather than the trailing
            // watch/risks -- sentiment is context for the read, not a footnote. No external
            // SectionTitle -- same shape as SignalSection above, whose "Market Signal" header
            // lives inside the card itself rather than as a separate list item. The whole card
            // always navigates to Posture (not a per-link choice) -- Positioning is reachable from
            // there once on Insights.
            validData.marketSentiment?.let { sentiment ->
                item {
                    MarketSentimentCard(sentiment = sentiment, onClick = onNavigateToPosture)
                }
            }

            val drivers = validData.drivers
            if (!drivers.isNullOrEmpty()) {
                item {
                    DriversSection(
                        drivers = drivers,
                        onClick = onNavigateToIndicators,
                        onInfoClick = { showDriversInfo = true }
                    )
                }
            }

            validData.position?.let { position ->
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

            // 💡 New 2026-08-21, placed here as a reasonable default -- no hierarchy slot has
            // been assigned for this section yet. See WhatsNewSection's doc comment below.
            val whatsNew = validData.whatsNew
            if (!whatsNew.isNullOrEmpty()) {
                item { WhatsNewSection(whatsNew) }
            }

            val stories = validData.leadStories
            if (!stories.isNullOrEmpty()) {
                item { LeadStoriesSection(stories) }
            }

            val macros = validData.macroMix
            if (!macros.isNullOrEmpty()) {
                item { MacroMixSection(macros) }
            }

            validData.dominoEffect?.let { domino ->
                item { DominoCard(domino) }
            }

            val watch = validData.watch
            if (!watch.isNullOrEmpty()) {
                item { WatchSection(watch) }
            }

            val risks = validData.risks
            if (!risks.isNullOrEmpty()) {
                item { RisksSection(risks) }
            }

            validData.verdict?.let { verdict ->
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

    val paddingMedium = dimensionResource(id = R.dimen.padding_medium)
    PulseCard(
        style = PulseCardStyle.SYNTHESIS,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Column(modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))) {
                CardEyebrowLabel(
                    text = stringResource(id = R.string.section_signal),
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(paddingMedium))
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
                        onClick = onRegimeClick
                    )
                }
                Spacer(modifier = Modifier.height(paddingMedium))

                // 💡 The flash -- largest prose on the card, no callout box around it; a headline
                // doesn't need to be quoted. titleMedium/bold.
                verdict.signalLine?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
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
// assets/market_glossary.json's "setups" definitions: EXHAUSTED OVERSOLD/OVERSOLD read as buying
// opportunities, OVERBOUGHT/BLOW-OFF TOP as danger), so the color has to be derived client-side from that same
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
 *
 * The header (title + info icon) now lives inside the card, same header+divider treatment as
 * [LeadStoriesSection]/[MacroMixSection], rather than a separate `DriversSectionHeader` list item
 * above it. The info icon stays its own separate tap target (opens [com.marketlabs.pulse.ui.
 * components.bottomSheet.DriversInfoBottomSheet]) -- a nested `clickable` consumes its own taps
 * before they reach the outer card's `onClick`, same as `DataCardTitleWithInfo`'s icon does inside
 * its own row elsewhere in the app. The whole card (below the header) is still the tap target into
 * Indicators (using the same tab-preserving `popUpTo`/`launchSingleTop`/`restoreState` pattern the
 * bottom nav bar itself uses -- see `MainActivity.kt`'s `FloatingBottomNav` `onItemClick`).
 * `drivers[].direction` means "this driver's net effect on equities," not "the underlying
 * indicator's own reading" (backend change, 2026-08-18) -- a distinction a color/arrow alone
 * can't communicate, which is what the info icon explains.
 */
@Composable
fun DriversSection(drivers: List<MarketDriver>, onClick: () -> Unit, onInfoClick: () -> Unit) {
    val sorted = drivers.sortedBy { if (it.impact == DriverImpact.HIGH) 0 else 1 }

    PulseCard(
        style = PulseCardStyle.DATA,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimensionResource(id = R.dimen.padding_large)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.section_drivers).uppercase(),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
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
            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                thickness = dimensionResource(id = R.dimen.border_thin)
            )
            // 💡 Only this row (the pills + chevron) is the tap target now -- not the whole card
            // -- so tapping the title/info row above never accidentally navigates away.
            Row(
                modifier = Modifier
                    .clickable(onClick = onClick)
                    .padding(dimensionResource(id = R.dimen.padding_large)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FlowRow(
                    modifier = Modifier.weight(1f),
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
                Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_small)))
                Icon(
                    painter = painterResource(id = R.drawable.ic_chevron_forward),
                    contentDescription = stringResource(id = R.string.drivers_navigate_content_description),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(dimensionResource(id = R.dimen.padding_large))
                )
            }
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
            Text(
                text = stringResource(id = R.string.section_market_position).uppercase(),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))
            )
            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                thickness = dimensionResource(id = R.dimen.border_thin)
            )
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

                position.valuation?.pePercentile5y?.let {
                    Text(
                        text = "${stringResource(id = R.string.label_valuation)} $it",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_small))
                    )
                }

                // 💡 Its own eyebrow-labeled block now -- same format Market Sentiment/Market
                // Read/Where Capital's Moving use (CardEyebrowLabel + the content below it) --
                // rather than an inline "What Changed: ..." sentence tacked onto the footer.
                whatChanged?.let {
                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))
                    CardEyebrowLabel(
                        text = stringResource(id = R.string.label_what_changed),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))
                    Text(
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
 * whats_new[] (new 2026-08-21) -- a deterministic (non-AI), most-recent-first list of indicators
 * whose data posted in the last 7 days. Styled as one `DATA`-style card holding every row, split
 * by a divider per entry -- same "one card, divided rows" structure [TheReadSection]'s
 * analysis/posture split already uses, one step further since the row count here is dynamic.
 * `changeDisplay` (the signed delta, e.g. "+0.3%"/"-5k" -- already carries its own up/down sign)
 * is tinted by `signalColor`, the same pre-classified backend read [MarketPositionSection]'s gauge
 * caption and [RisksSection]'s severity pill already render directly with no client-side
 * threshold logic. `category` is deliberately not shown -- kept on the domain model for whichever
 * screen ends up grouping by it, but redundant with `label` for a flat list like this one.
 * TODO(hierarchy-placement): this section's placement (currently right after Market Position) is
 * still a reasonable-default guess, not a confirmed design decision.
 */
@Composable
fun WhatsNewSection(items: List<WhatsNewItem>) {
    PulseCard(style = PulseCardStyle.DATA, modifier = Modifier.fillMaxWidth()) {
        Column {
            Text(
                text = stringResource(id = R.string.section_whats_new).uppercase(),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))
            )
            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                thickness = dimensionResource(id = R.dimen.border_thin)
            )
            items.forEachIndexed { index, item ->
                val changeColor = item.signalColor.textColor

                Column(modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.label ?: "",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        item.changeDisplay?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.titleSmall,
                                color = changeColor
                            )
                        }
                    }

                    val detailLine = listOfNotNull(
                        item.valueDisplay,
                        item.signalText,
                        item.releaseDate.toShortReleaseDate()
                    ).joinToString(" · ")
                    if (detailLine.isNotBlank()) {
                        Text(
                            text = detailLine,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_micro))
                        )
                    }
                }

                if (index != items.lastIndex) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                        thickness = dimensionResource(id = R.dimen.border_thin)
                    )
                }
            }
        }
    }
}

// release_date is the backend's plain `yyyy-MM-dd` (confirmed against marketPulseComposer.ts,
// which parses it as `${release_date}T00:00:00Z`) -- reformatted to "Aug 19" for display. Falls
// back to the raw string on a parse miss rather than dropping the date, since this is external
// data being parsed, not a value this app itself formatted.
private fun String?.toShortReleaseDate(): String? {
    if (this == null) return null
    return try {
        val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(this)
        parsed?.let { SimpleDateFormat("MMM d", Locale.getDefault()).format(it) } ?: this
    } catch (e: ParseException) {
        this
    }
}

/**
 * The watch[] list -- one `DATA` style card (same header+divider treatment as
 * [LeadStoriesSection]/[MacroMixSection]) holding every item, timeframe using the same [TagPill]
 * [MacroMixSection]'s tag does, on its own line below the label rather than sharing a row with it
 * (so a long label never crowds it). A full-width divider sits between entries; no divider within
 * one entry's own label/why text.
 */
@Composable
fun WatchSection(watch: List<WatchItem>) {
    PulseCard(style = PulseCardStyle.DATA, modifier = Modifier.fillMaxWidth()) {
        Column {
            Text(
                text = stringResource(id = R.string.section_watch).uppercase(),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))
            )
            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                thickness = dimensionResource(id = R.dimen.border_thin)
            )
            watch.forEachIndexed { index, item ->
                Column(modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))) {
                    Text(
                        text = item.label ?: "",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    item.timeframe?.let {
                        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))
                        TagPill(text = it)
                    }
                    item.why?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_medium))
                        )
                    }
                }
                if (index != watch.lastIndex) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                        thickness = dimensionResource(id = R.dimen.border_thin)
                    )
                }
            }
        }
    }
}

/**
 * The risks[] list -- one `DATA` style card (same header+divider treatment as
 * [LeadStoriesSection]/[WatchSection]) holding every item, severity colored through the same
 * [RiskImpactLevel] color extension [HorizonChip] uses (`ui/theme/SignalColorExtensions.kt`), not
 * a hand-rolled `when` here, on its own line below the risk text rather than sharing a row with it
 * (so a long risk description never crowds it). A full-width divider sits between entries; no
 * divider within one entry's own risk/note text.
 */
@Composable
fun RisksSection(risks: List<RiskItem>) {
    PulseCard(style = PulseCardStyle.DATA, modifier = Modifier.fillMaxWidth()) {
        Column {
            Text(
                text = stringResource(id = R.string.section_risks).uppercase(),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))
            )
            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                thickness = dimensionResource(id = R.dimen.border_thin)
            )
            risks.forEachIndexed { index, item ->
                Column(modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))) {
                    Text(
                        text = item.risk ?: "",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    item.severity?.let {
                        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))
                        SignalPill(
                            text = it.label.uppercase(),
                            pillColor = it.pillColor,
                            contentColor = it.textColor
                        )
                    }
                    item.note?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_medium))
                        )
                    }
                }
                if (index != risks.lastIndex) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                        thickness = dimensionResource(id = R.dimen.border_thin)
                    )
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
    // inside one Column padded as a whole -- same structural idiom LeadStoriesSection/MacroMixSection/
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
                CardEyebrowLabel(
                    text = stringResource(id = R.string.market_read),
                    color = MaterialTheme.colorScheme.primary
                )
                // 💡 The depth -- the full paragraph, for the user who scrolls this far. Same
                // eyebrow-to-content gap Market Sentiment uses (padding_medium), not the 0/tiny
                // gap this used to have.
                verdict.analysis?.let {
                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))
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
                    CardEyebrowLabel(
                        text = stringResource(id = R.string.label_posture),
                        color = MaterialTheme.colorScheme.primary
                    )
                    // 💡 Same eyebrow-to-content gap Market Sentiment uses (padding_medium), not
                    // the padding_tiny gap this used to have.
                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))
                    Text(
                        text = posture,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

/**
 * 💡 Trial revamp (per CardStyleShowcase.kt's `DataTimelineSample` pattern, being tested here
 * first before any other section adopts it): the section title moves INSIDE the card as its own
 * small-caps accent header with a full-bleed divider directly beneath it, replacing the separate
 * `SectionTitle` list item that used to sit above N individual story cards. All stories now live
 * in ONE `PulseCard`, separated by an inset divider between entries -- the divider that used to
 * sit between one story's own headline and summary is gone; only the boundary between two
 * different stories gets one, since that's the boundary actually worth marking now.
 *
 * @param stories The lead-story items to display. Entries with no headline are skipped; if none
 *   have a headline, the whole section renders nothing.
 */
@Composable
fun LeadStoriesSection(stories: List<NewsItem>) {
    val validStories = stories.filter { it.headline != null }
    if (validStories.isEmpty()) return

    PulseCard(style = PulseCardStyle.DATA, modifier = Modifier.fillMaxWidth()) {
        Column {
            Text(
                text = stringResource(id = R.string.section_lead_stories).uppercase(),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))
            )
            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                thickness = dimensionResource(id = R.dimen.border_thin)
            )
            // 💡 Each story gets its OWN padded Column, with an unpadded (full-card-width)
            // divider between them at this outer level -- same "divider spans the full width
            // between two separately-padded Columns" idiom SignalSection/MacroMixSection use, rather
            // than one padded Column wrapping every story (which would inset the divider to stop
            // short at the content margin instead of reaching the card's edges).
            validStories.forEachIndexed { index, story ->
                Column(modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))) {
                    Text(
                        text = story.headline!!,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    story.summary?.let { summary ->
                        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))
                        Text(
                            text = summary,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                if (index != validStories.lastIndex) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                        thickness = dimensionResource(id = R.dimen.border_thin)
                    )
                }
            }
        }
    }
}

/**
 * spec-20260902-market-sentiment-android.md's Market Sentiment card -- AI-authored cohort-
 * positioning synthesis (headline + summary), styled SYNTHESIS like Signal/The Read (the two
 * other AI-narrative cards on this screen). The whole card is the tap target (same
 * `PulseCard(onClick = ...)` + trailing-chevron shape as `DriversSection` below), always landing
 * on the Posture tab on Insights -- that screen (and Positioning alongside it) owns the raw gauge
 * numbers, this card deliberately doesn't duplicate them.
 *
 * @param sentiment The [MarketSentiment] to display. Caller (`MarketSummaryScreen`) already omits
 * this card entirely when both headline and summary are blank; either field alone still renders.
 */
@Composable
fun MarketSentimentCard(sentiment: MarketSentiment, onClick: () -> Unit) {
    val paddingMedium = dimensionResource(id = R.dimen.padding_medium)
    PulseCard(
        style = PulseCardStyle.SYNTHESIS,
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        // 💡 The chevron now sits beside the headline specifically (this card's "main heading"),
        // vertically centered against just that row -- not the whole card's height -- so it reads
        // as "this heading leads somewhere," pinned to the one line that actually says so. Falls
        // back to pairing the chevron with the summary row when there's no headline (rare -- the
        // caller only omits this card entirely when both are blank).
        val headline = sentiment.headline
        Column(modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))) {
            // 💡 Header lives inside the card, same as SignalSection's "Market Signal" -- not
            // a separate SectionTitle list item.
            CardEyebrowLabel(
                text = stringResource(id = R.string.section_market_sentiment),
                color = MaterialTheme.colorScheme.primary
            )

            if (headline != null) {
                Spacer(modifier = Modifier.height(paddingMedium))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = headline,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_small)))
                    Icon(
                        painter = painterResource(id = R.drawable.ic_chevron_forward),
                        contentDescription = stringResource(id = R.string.market_sentiment_navigate_content_description),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(dimensionResource(id = R.dimen.padding_large))
                    )
                }
            }

            sentiment.summary?.let { summary ->
                Spacer(modifier = Modifier.height(paddingMedium))
                if (headline != null) {
                    Text(
                        text = summary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = summary,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_small)))
                        Icon(
                            painter = painterResource(id = R.drawable.ic_chevron_forward),
                            contentDescription = stringResource(id = R.string.market_sentiment_navigate_content_description),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(dimensionResource(id = R.dimen.padding_large))
                        )
                    }
                }
            }
        }
    }
}

/**
 * The macro_mix[] list -- one `DATA` style card (same header+divider treatment as
 * [LeadStoriesSection]/[WatchSection]) holding every macro-economic factor. No left-rail accent
 * bar (removed -- plain padded entries, matching every other merged-card section); a full-width
 * divider sits between entries instead. `tag` renders on its own line below the heading rather
 * than sharing a row with it, so a long headline never crowds it. The divider that used to sit
 * between one item's own headline/tag row and its summary is gone, matching [LeadStoriesSection]'s
 * "only the boundary between two different entries is worth marking" reasoning.
 *
 * @param macros The macro items to display. Entries with no headline are skipped; if none have a
 *   headline, the whole section renders nothing.
 */
@Composable
fun MacroMixSection(macros: List<MacroItem>) {
    val validMacros = macros.filter { it.headline != null }
    if (validMacros.isEmpty()) return

    PulseCard(style = PulseCardStyle.DATA, modifier = Modifier.fillMaxWidth()) {
        Column {
            Text(
                text = stringResource(id = R.string.section_macro_mix).uppercase(),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))
            )
            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                thickness = dimensionResource(id = R.dimen.border_thin)
            )
            validMacros.forEachIndexed { index, item ->
                // 💡 No more left-rail accent bar -- plain padded Column, same as every other
                // merged-card entry (Lead Stories/Watch/Risks). The tag now sits on its own line
                // below the heading rather than sharing a row with it, so a long headline never
                // has to compete with the tag for width or push it to a cramped corner.
                Column(modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))) {
                    Text(
                        text = item.headline!!,
                        style = MaterialTheme.typography.titleMedium,
                        // 💡 Card titles are always onSurface -- see LeadStoriesSection above.
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    item.tag?.let { tag ->
                        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))
                        TagPill(text = tag.label)
                    }
                    item.summary?.let {
                        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                if (index != validMacros.lastIndex) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                        thickness = dimensionResource(id = R.dimen.border_thin)
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

    // 💡 DATA style -- see LeadStoriesSection above.
    PulseCard(
        style = PulseCardStyle.DATA,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Text(
                text = stringResource(id = R.string.section_domino_effect).uppercase(),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))
            )
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
 * The neutral tag/timeframe pill shared by [MacroMixSection] and [WatchSection] -- one component, not
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
 * [DriversSection]'s trailing navigate chevron, which isn't paired with its own adjacent label text.
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
private fun PreviewDriversSection() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        Column(modifier = Modifier.padding(16.dp)) {
            DriversSection(
                drivers = listOf(
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
                ),
                onClick = {},
                onInfoClick = {}
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
private fun PreviewWhatsNewSection() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        Column(modifier = Modifier.padding(16.dp)) {
            WhatsNewSection(
                listOf(
                    WhatsNewItem(
                        label = "Retail Sales (MoM)",
                        category = IndicatorCategory.MACRO_ECONOMY,
                        valueDisplay = "-0.3%",
                        changeDisplay = "-0.5%",
                        signalText = "Below Consensus",
                        signalColor = SignalColor.RED,
                        releaseDate = "2026-08-20"
                    ),
                    WhatsNewItem(
                        label = "Initial Jobless Claims",
                        category = IndicatorCategory.MACRO_ECONOMY,
                        valueDisplay = "235k",
                        changeDisplay = "-5k",
                        signalText = "In Line",
                        signalColor = SignalColor.YELLOW,
                        releaseDate = "2026-08-19"
                    ),
                    WhatsNewItem(
                        label = "Core PCE (MoM)",
                        category = IndicatorCategory.MACRO_ECONOMY,
                        valueDisplay = "0.1%",
                        changeDisplay = "-0.1%",
                        signalText = "Cooler Than Expected",
                        signalColor = SignalColor.GREEN,
                        releaseDate = "2026-08-15"
                    )
                )
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
