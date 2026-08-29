package com.marketlabs.pulse.ui.screens.indicators.views

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.marketlabs.pulse.R
import com.marketlabs.pulse.storage.model.indicators.DomainAiSynthesis
import com.marketlabs.pulse.storage.model.indicators.DomainExecutiveBlock
import com.marketlabs.pulse.storage.model.indicators.DomainHorizons
import com.marketlabs.pulse.storage.model.indicators.DomainIndicatorPillar
import com.marketlabs.pulse.storage.model.indicators.DomainPillarScorecardEntry
import com.marketlabs.pulse.storage.model.indicators.DomainShift
import com.marketlabs.pulse.storage.model.indicators.DomainUnifiedMetric
import com.marketlabs.pulse.storage.model.indicators.MarketIndicators
import com.marketlabs.pulse.ui.components.AnalyzedAtHeader
import com.marketlabs.pulse.ui.components.PulseCard
import com.marketlabs.pulse.ui.components.PulseCardStyle
import com.marketlabs.pulse.ui.components.UniversalMetricCard
import com.marketlabs.pulse.ui.components.widgets.SignalPill
import com.marketlabs.pulse.ui.theme.LocalPulseColors
import com.marketlabs.pulse.ui.theme.MarketPulseTheme
import com.marketlabs.pulse.ui.theme.pillColor
import com.marketlabs.pulse.ui.theme.textColor
import com.marketlabs.pulse.utils.enums.AgreementState
import com.marketlabs.pulse.utils.enums.AlignmentState
import com.marketlabs.pulse.utils.enums.IndicatorCategory
import com.marketlabs.pulse.utils.enums.ShiftDirection
import com.marketlabs.pulse.utils.enums.SignalColor
import com.marketlabs.pulse.utils.enums.SubcategoryEnums

// ============================================================================
// 📱 MASTER STATE CONTROLLER
// ============================================================================
@Composable
fun IndicatorsScreen(
    data: MarketIndicators,
    scaffoldPadding: PaddingValues,
    onNavigateToHorizons: () -> Unit,
    onNavigateToMetricDetail: (String) -> Unit
) {
    // 💡 metric_id -> display name, resolved once per composition from the already-loaded pillar
    // lists. `executive.shifts[]` only ever carries a metric_id string -- the backend spec
    // deliberately keeps that cross-reference a UI-layer concern (validated server-side, but never
    // resolved to a display name server-side) so this app can render whatever name it's already
    // showing on that metric's own card. (`horizons.*.key_drivers[]` used to need this same
    // resolution but was removed from the backend schema entirely 2026-08-22 -- see
    // IndicatorHorizonsScreen.kt.)
    val metricNames = remember(data) {
        listOfNotNull(data.tacticalMomentum, data.systemicRisk, data.valuation, data.macroVitals)
            .flatMap { it.metrics }
            .associate { it.id to it.name }
    }

    IndicatorsMainFeed(
        data = data,
        metricNames = metricNames,
        scaffoldPadding = scaffoldPadding,
        onShowHorizons = onNavigateToHorizons,
        onNavigateToMetricDetail = onNavigateToMetricDetail
    )
}

// ============================================================================
// 📜 MAIN MINIMALIST FEED
// ============================================================================
@Composable
private fun IndicatorsMainFeed(
    data: MarketIndicators,
    metricNames: Map<String, String>,
    scaffoldPadding: PaddingValues,
    onShowHorizons: () -> Unit,
    onNavigateToMetricDetail: (String) -> Unit
) {
    val paddingLarge = dimensionResource(id = R.dimen.padding_large)

    val allPillars = listOfNotNull(
        data.tacticalMomentum?.let {
            PillarUIConfig(stringResource(id = R.string.pillar_tactical_momentum), IndicatorCategory.TACTICAL_MOMENTUM, it)
        },
        data.systemicRisk?.let {
            PillarUIConfig(stringResource(id = R.string.pillar_systemic_risk), IndicatorCategory.SYSTEMIC_RISK, it)
        },
        data.valuation?.let {
            PillarUIConfig(stringResource(id = R.string.pillar_valuation), IndicatorCategory.VALUATION, it)
        },
        data.macroVitals?.let {
            // 💡 FLAGGED AS MACRO: This allows us to conditionally render the release dates
            PillarUIConfig(stringResource(id = R.string.pillar_macro_vitals), IndicatorCategory.MACRO_ECONOMY, it, isMacro = true)
        }
    )

    val scorecardByPillar = data.aiSynthesis?.pillarScorecard?.associateBy { it.pillar } ?: emptyMap()

    // 💡 The icon+"Market Indicators" row that used to open this screen is gone -- the title now
    // lives in the global top bar (MainActivity resolves it per-route), so keeping this row would
    // have said the same thing twice in two places on screen at once. `scaffoldPadding`'s top
    // component (not the raw status bar inset alone) is what actually accounts for the top bar's
    // real rendered height, so content starts right below it instead of underneath it.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            contentPadding = PaddingValues(
                start = paddingLarge,
                end = paddingLarge,
                top = scaffoldPadding.calculateTopPadding(),
                bottom = scaffoldPadding.calculateBottomPadding() + paddingLarge
            ),
            modifier = Modifier.fillMaxSize()
        ) {
            data.aiSynthesis?.timestamp?.let { timestamp ->
                item {
                    AnalyzedAtHeader(timestamp = timestamp)
                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_large)))
                }
            }

            item {
                AiExecutiveBriefingHero(
                    executive = data.aiSynthesis?.executive,
                    timestamp = data.aiSynthesis?.timestamp,
                    metricNames = metricNames
                )
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_large)))
            }

            item {
                HorizonNavigationCard(onClick = onShowHorizons)
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_large)))
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                    thickness = dimensionResource(id = R.dimen.border_thin)
                )
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_large)))
            }

            items(allPillars) { config ->
                PillarSection(
                    config = config,
                    scorecardEntry = scorecardByPillar[config.pillarCategory],
                    onIndicatorClick = { metric -> onNavigateToMetricDetail(metric.id) }
                )
            }
        }
    }
}

// ============================================================================
// 🧱 SUB-COMPONENTS FOR MAIN FEED
// ============================================================================

@Composable
private fun AiExecutiveBriefingHero(
    executive: DomainExecutiveBlock?,
    timestamp: Long?,
    metricNames: Map<String, String>
) {
    if (executive == null || executive.headline.isBlank() || timestamp == null) return

    var isExpanded by remember { mutableStateOf(false) }

    val paddingMedium = dimensionResource(id = R.dimen.padding_medium)
    val paddingLarge = dimensionResource(id = R.dimen.padding_large)
    val paddingSmall = dimensionResource(id = R.dimen.padding_small)

    // 💡 SYNTHESIS style -- this is the AI Executive Briefing, the same kind of AI-interpreted
    // content as Dashboard's Technical Briefing and the News cards.
    PulseCard(
        style = PulseCardStyle.SYNTHESIS,
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        onClick = { isExpanded = !isExpanded }
    ) {
        Column(modifier = Modifier.padding(paddingLarge)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val textStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    val iconSize = with(LocalDensity.current) { textStyle.fontSize.toDp() }

                    // 💡 This eyebrow (icon + label) marks the card as AI-sourced, the same
                    // role Dashboard's Technical Briefing eyebrow plays -- matches its
                    // accentPrimary color instead of the muted `colorScheme.secondary` this
                    // used before. The "Analyzed as of" timestamp that used to sit under this
                    // row moved out of the card entirely -- see AnalyzedAtHeader, now the first
                    // item in the screen's own LazyColumn, matching how every other screen in
                    // this app (Summary's HeaderSection, for instance) places its own timestamp
                    // at the top of the content, not nested inside a card.
                    Icon(
                        painter = painterResource(id = R.drawable.ic_ai_sparkle_filled),
                        contentDescription = "Analysis Engine",
                        tint = LocalPulseColors.current.accentPrimary,
                        modifier = Modifier.size(iconSize)
                    )
                    Spacer(modifier = Modifier.width(paddingSmall))
                    Text(
                        text = stringResource(id = R.string.indicators_todays_read),
                        style = textStyle,
                        color = LocalPulseColors.current.accentPrimary
                    )
                }
                Icon(
                    painter = painterResource(id = if (isExpanded) R.drawable.ic_arrow_up else R.drawable.ic_arrow_down),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = paddingMedium)
                )
            }

            // 💡 Card order: alignment_with_macro pill, then headline, then alignment_note --
            // the code-computed read of whether the pillars agree with the macro regime frames
            // the headline, and the AI's own explanation of that read follows the headline
            // rather than sitting bundled with the pill above it.
            Spacer(modifier = Modifier.height(paddingMedium))
            SignalPill(
                text = executive.alignmentWithMacro.label,
                pillColor = executive.alignmentWithMacro.pillColor,
                contentColor = executive.alignmentWithMacro.textColor,
                outlined = true
            )

            Spacer(modifier = Modifier.height(paddingMedium))

            // 💡 The headline is the primary heading of this card -- always shown in full, never
            // clamped, the same way a news headline would be.
            Text(
                text = executive.headline,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            if (executive.alignmentNote.isNotBlank()) {
                Spacer(modifier = Modifier.height(paddingLarge))
                Text(
                    text = executive.alignmentNote,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = if (isExpanded) Int.MAX_VALUE else 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // 💡 what_changed and shifts[] are both "since yesterday" detail -- collapsed by
            // default along with the rest of the card's supporting detail, not shown until the
            // reader taps to expand.
            if (isExpanded) {
                if (executive.whatChanged.isNotBlank()) {
                    Spacer(modifier = Modifier.height(paddingMedium))
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                        thickness = dimensionResource(id = R.dimen.border_thin)
                    )
                    Spacer(modifier = Modifier.height(paddingMedium))
                    Text(
                        text = executive.whatChanged,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.2f
                    )
                }

                if (executive.shifts.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(paddingMedium))
                    Column(verticalArrangement = Arrangement.spacedBy(paddingSmall)) {
                        executive.shifts.forEach { shift ->
                            ShiftRow(shift = shift, metricName = metricNames[shift.metricId] ?: shift.metricId)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ShiftRow(shift: DomainShift, metricName: String) {
    val paddingMedium = dimensionResource(id = R.dimen.padding_medium)
    val paddingSmall = dimensionResource(id = R.dimen.padding_small)

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_small)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(paddingMedium)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = metricName,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(modifier = Modifier.width(paddingSmall))
                SignalPill(
                    text = shift.direction.name,
                    pillColor = shift.direction.pillColor,
                    contentColor = shift.direction.textColor,
                    outlined = true
                )
            }
            Text(
                text = shift.note,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun HorizonNavigationCard(onClick: () -> Unit) {
    // 💡 SYNTHESIS style -- an AI-sourced entry point, same card family as the executive briefing
    // above it, not the plain `primaryContainer` pill this used to be.
    PulseCard(
        style = PulseCardStyle.SYNTHESIS,
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(id = R.dimen.padding_large)),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(id = R.string.indicators_horizons_title),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_tiny)))
                Text(
                    text = stringResource(id = R.string.indicators_horizons_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                painter = painterResource(id = R.drawable.ic_chevron_forward),
                contentDescription = null,
                tint = LocalPulseColors.current.accentPrimary,
                modifier = Modifier.padding(start = dimensionResource(id = R.dimen.padding_medium))
            )
        }
    }
}

data class PillarUIConfig(
    val title: String,
    val pillarCategory: IndicatorCategory,
    val pillarData: DomainIndicatorPillar,
    val isMacro: Boolean = false
)

@Composable
private fun PillarSection(
    config: PillarUIConfig,
    scorecardEntry: DomainPillarScorecardEntry?,
    onIndicatorClick: (DomainUnifiedMetric) -> Unit
) {
    val paddingMedium = dimensionResource(id = R.dimen.padding_medium)
    val paddingLarge = dimensionResource(id = R.dimen.padding_large)

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = config.title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = paddingLarge)
        )

        scorecardEntry?.let { entry ->
            PillarScorecardCard(entry = entry, modifier = Modifier.padding(bottom = paddingMedium))
        }

        val groupedMetrics = config.pillarData.metrics.groupBy { it.subcategory }

        groupedMetrics.forEach { (subcatEnum, metrics) ->

            if (subcatEnum != null) {
                val subcategoryText = when (subcatEnum) {
                    SubcategoryEnums.INFLATION -> stringResource(id = R.string.subcategory_inflation)
                    SubcategoryEnums.LABOR -> stringResource(id = R.string.subcategory_labor)
                    SubcategoryEnums.GROWTH -> stringResource(id = R.string.subcategory_growth)
                    SubcategoryEnums.POLICY -> stringResource(id = R.string.subcategory_policy)
                    else -> subcatEnum.label
                }

                // 💡 UPDATED: Typography changed to match native headers visually without uppercase labels
                Text(
                    text = subcategoryText,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.padding_medium), top = paddingLarge)
                )
            }

            metrics.chunked(2).forEach { rowMetrics ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Max),
                    horizontalArrangement = Arrangement.spacedBy(paddingMedium)
                ) {
                    rowMetrics.forEach { metric ->

                        val formattedChange = metric.changeDisplay?.let { changeStr ->
                            if (metric.changeRaw == 0.0 && !changeStr.startsWith("+") && !changeStr.startsWith("-")) {
                                "+$changeStr"
                            } else {
                                changeStr
                            }
                        }

                        UniversalMetricCard(
                            title = metric.name,
                            value = metric.valueDisplay,
                            changeString = formattedChange,
                            signalText = metric.signalText,
                            signalColor = metric.signalColor,
                            // 💡 HIDDEN: Evaluates macro flag to strip dates from pure technicals
                            dateString = if (config.isMacro) metric.releaseDate else null,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            onClick = { onIndicatorClick(metric) }
                        )
                    }
                    if (rowMetrics.size == 1) {
                        Spacer(modifier = Modifier.weight(1f).fillMaxHeight())
                    }
                }
                Spacer(modifier = Modifier.height(paddingMedium))
            }
        }

        HorizontalDivider(
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
            modifier = Modifier.padding(vertical = paddingMedium)
        )
    }
}

/**
 * Code-computed pillar-level rollup (`pillar_scorecard[]`) -- `agreement` (how much this pillar's
 * own metrics agree with each other) on the left, `stance` (this pillar's own color, same
 * [SignalColor] as its individual metric cards) on the right as a pill, `oneLiner` narrating the
 * shape below. Replaces the old ad hoc "AI Glance" box, which only ever had a single free-text
 * string with no structured agreement/stance to show.
 */
@Composable
private fun PillarScorecardCard(entry: DomainPillarScorecardEntry, modifier: Modifier = Modifier) {
    val paddingMedium = dimensionResource(id = R.dimen.padding_medium)
    val paddingLarge = dimensionResource(id = R.dimen.padding_large)
    val paddingSmall = dimensionResource(id = R.dimen.padding_small)

    // 💡 SYNTHESIS style -- same AI-sourced card family as the executive briefing hero and the
    // Horizons entry card, not the plain `surfaceVariant` box this used to be.
    PulseCard(
        style = PulseCardStyle.SYNTHESIS,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(paddingLarge)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SignalPill(
                    text = entry.agreement.name,
                    pillColor = entry.agreement.pillColor,
                    contentColor = entry.agreement.textColor,
                    outlined = true
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // 💡 "STANCE" label -- unlike "ALIGNED"/"MIXED"/"DIVERGENT" on the left, the
                    // stance pill alone just reads GREEN/YELLOW/RED with no context for what that
                    // color is rating.
                    Text(
                        text = stringResource(id = R.string.indicators_stance_label),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(paddingSmall))
                    SignalPill(
                        text = entry.stance.name,
                        pillColor = entry.stance.pillColor,
                        contentColor = entry.stance.textColor
                    )
                }
            }
            Spacer(modifier = Modifier.height(paddingMedium))
            // 💡 `oneLiner` is AI-authored prose (the model narrates around the code-computed
            // agreement/stance above it, never invents them) -- the same sparkle glyph the
            // executive briefing hero's "Today's Read" eyebrow uses marks it as AI-sourced here
            // too, rather than reading as a plain data label like the stance pill next to it.
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_ai_sparkle_filled),
                    contentDescription = stringResource(id = R.string.summary_analysis_engine_content_description),
                    tint = LocalPulseColors.current.accentPrimary,
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .size(14.dp)
                )
                Spacer(modifier = Modifier.width(paddingSmall))
                Text(
                    text = entry.oneLiner,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.2f
                )
            }
        }
    }
}

// ============================================================================
// 🎨 PREVIEWS
// ============================================================================

private val previewExecutive = DomainExecutiveBlock(
    headline = "Equities Maintain Upward Trajectory Amid Macroeconomic Divergence and Compressed Credit Spreads",
    alignmentWithMacro = AlignmentState.MARKET_AHEAD_OF_FUNDAMENTALS,
    alignmentNote = "A clear tension exists between the prevailing risk-on market regime and the underlying structural deterioration in macroeconomic indicators, though systemic risk remains well-contained.",
    whatChanged = "Today's baseline is being established and day-over-day comparisons will be available starting tomorrow.",
    shifts = listOf(
        DomainShift(metricId = "pe_ratio", direction = ShiftDirection.DETERIORATED, note = "Crossed into Expensive territory as prices outran trailing earnings."),
        DomainShift(metricId = "credit_spreads", direction = ShiftDirection.IMPROVED, note = "Tightened further into Healthy range, easing default-risk concerns.")
    )
)

private val previewScorecardEntry = DomainPillarScorecardEntry(
    pillar = IndicatorCategory.VALUATION,
    stance = SignalColor.RED,
    agreement = AgreementState.ALIGNED,
    oneLiner = "Valuation metrics are uniformly stretched across all primary gauges, showing tight alignment in their historical expensiveness."
)

private val previewValuationPillar = DomainIndicatorPillar(
    timestamp = 0L,
    masterGauge = null,
    metrics = listOf(
        DomainUnifiedMetric(
            id = "pe_ratio", name = "P/E Ratio (Trailing)", category = "VALUATION", subcategory = null,
            valueRaw = 25.79, valueDisplay = "25.79x", previousValueRaw = null, previousValueDisplay = null,
            changeRaw = 0.12, changeDisplay = "0.12%", signalText = "Expensive", signalColor = SignalColor.RED, releaseDate = null
        ),
        DomainUnifiedMetric(
            id = "pb_ratio", name = "Price-to-Book", category = "VALUATION", subcategory = null,
            valueRaw = 4.8, valueDisplay = "4.80x", previousValueRaw = null, previousValueDisplay = null,
            changeRaw = 0.02, changeDisplay = "0.02%", signalText = "Premium", signalColor = SignalColor.RED, releaseDate = null
        )
    )
)

private val previewMarketIndicators = MarketIndicators(
    dateId = "2026-08-22",
    lastSyncedTimestamp = System.currentTimeMillis(),
    aiSynthesis = DomainAiSynthesis(
        timestamp = System.currentTimeMillis(),
        contentFlags = emptyList(),
        executive = previewExecutive,
        pillarScorecard = listOf(previewScorecardEntry),
        horizons = DomainHorizons(null, null, null)
    ),
    tacticalMomentum = null,
    systemicRisk = null,
    valuation = previewValuationPillar,
    macroVitals = null
)

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun PreviewIndicatorsScreen() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        IndicatorsScreen(
            data = previewMarketIndicators,
            scaffoldPadding = PaddingValues(0.dp),
            onNavigateToHorizons = {},
            onNavigateToMetricDetail = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun PreviewAiExecutiveBriefingHero() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        Column(modifier = Modifier.padding(16.dp)) {
            AiExecutiveBriefingHero(
                executive = previewExecutive,
                timestamp = System.currentTimeMillis(),
                metricNames = mapOf("pe_ratio" to "P/E Ratio (Trailing)", "credit_spreads" to "Credit Spreads (High Yield)")
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun PreviewHorizonNavigationCard() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        Column(modifier = Modifier.padding(16.dp)) {
            HorizonNavigationCard(onClick = {})
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun PreviewPillarSection() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        Column(modifier = Modifier.padding(16.dp)) {
            PillarSection(
                config = PillarUIConfig(
                    title = "Valuation",
                    pillarCategory = IndicatorCategory.VALUATION,
                    pillarData = previewValuationPillar
                ),
                scorecardEntry = previewScorecardEntry,
                onIndicatorClick = {}
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun PreviewPillarScorecardCard() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        Column(modifier = Modifier.padding(16.dp)) {
            PillarScorecardCard(entry = previewScorecardEntry)
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun PreviewShiftRow() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        Column(modifier = Modifier.padding(16.dp)) {
            ShiftRow(shift = previewExecutive.shifts.first(), metricName = "P/E Ratio (Trailing)")
        }
    }
}

