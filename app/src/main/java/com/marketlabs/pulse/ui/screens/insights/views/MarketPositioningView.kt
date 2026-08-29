package com.marketlabs.pulse.ui.screens.insights.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.marketlabs.pulse.R
import com.marketlabs.pulse.storage.model.positioning.DomainFuturesContract
import com.marketlabs.pulse.storage.model.positioning.DomainInstitutionalPositioning
import com.marketlabs.pulse.storage.model.positioning.DomainMarketPositioning
import com.marketlabs.pulse.storage.model.positioning.DomainPositioningSynthesis
import com.marketlabs.pulse.storage.model.positioning.DomainRetailSentiment
import com.marketlabs.pulse.storage.model.positioning.DomainShortInterest
import com.marketlabs.pulse.storage.model.positioning.DomainShortInterestInstrument
import com.marketlabs.pulse.ui.components.FirstTimeExplainerCard
import com.marketlabs.pulse.ui.components.MetricCardFooter
import com.marketlabs.pulse.ui.components.MetricStatusRow
import com.marketlabs.pulse.ui.components.PulseCard
import com.marketlabs.pulse.ui.components.PulseCardStyle
import com.marketlabs.pulse.ui.components.SynthesisHeroCard
import androidx.compose.foundation.clickable
import androidx.compose.material3.HorizontalDivider
import com.marketlabs.pulse.ui.components.widgets.ChangeDirection
import com.marketlabs.pulse.ui.components.widgets.DirectionalChangePill
import com.marketlabs.pulse.ui.components.widgets.GlossaryTapChevron
import com.marketlabs.pulse.ui.components.widgets.MetricInfoAction
import com.marketlabs.pulse.ui.components.widgets.PercentileBar
import com.marketlabs.pulse.ui.components.widgets.TriSegmentBar
import com.marketlabs.pulse.ui.theme.LocalPulseColors
import com.marketlabs.pulse.ui.theme.MarketPulseTheme
import com.marketlabs.pulse.ui.theme.pillColor
import com.marketlabs.pulse.ui.theme.textColor
import com.marketlabs.pulse.utils.enums.CotPositioningStatus
import com.marketlabs.pulse.utils.enums.DeltaDirection
import com.marketlabs.pulse.utils.enums.RetailSentimentStatus
import com.marketlabs.pulse.utils.enums.ShortInterestStatus
import com.marketlabs.pulse.utils.toDisplayDate
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

/** See `MarketPostureView.kt`'s identical doc comment on `InstitutionalPostureSection` for the 2026-08-27 whole-card-tap convergence. */
@Composable
fun MarketPositioningSection(
    positioningData: DomainMarketPositioning,
    onNavigateToGlossaryDetail: (metricIds: List<String>, title: String, description: String?, status: String?) -> Unit,
    isIntroDismissed: Boolean,
    onDismissIntro: () -> Unit
) {
    val paddingMedium = dimensionResource(id = R.dimen.padding_medium)
    val paddingLarge = dimensionResource(id = R.dimen.padding_large)

    Column(modifier = Modifier.fillMaxWidth()) {
        // --- SECTION HEADER ---
        Row(verticalAlignment = Alignment.CenterVertically) {
            val textStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            val iconSize = with(LocalDensity.current) { textStyle.fontSize.toDp() }

            Icon(
                painter = painterResource(id = R.drawable.ic_engine_quant),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(iconSize)
            )
            Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_small)))
            Text(
                text = stringResource(id = R.string.positioning_section_title),
                style = textStyle,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_small)))
            // 💡 2026-08-27 convergence: this icon and the first-time explainer card below now
            // show the SAME merged string (positioning_explainer_text) -- was two separate,
            // partly overlapping paragraphs.
            MetricInfoAction(
                title = stringResource(id = R.string.positioning_section_title),
                description = stringResource(id = R.string.positioning_explainer_text)
            )
        }

        val date = positioningData.timestamp?.let { Date(it) } ?: Date()
        val format = SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault())
        Text(
            text = stringResource(id = R.string.analyzed_at, format.format(date)),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(
                top = dimensionResource(id = R.dimen.padding_micro),
                bottom = dimensionResource(id = R.dimen.padding_large)
            )
        )

        // --- FIRST-TIME EXPLAINER (2026-08-27 interpretive-layer spec, Layer 3) -- stays visible
        // until the reader taps "Got it" here. ---
        if (!isIntroDismissed) {
            FirstTimeExplainerCard(
                text = stringResource(id = R.string.positioning_explainer_text),
                onDismiss = onDismissIntro
            )
            Spacer(modifier = Modifier.height(paddingLarge))
        }

        // --- SYNTHESIS HERO ---
        positioningData.synthesis?.let { synthesis ->
            SynthesisHeroCard(
                headline = synthesis.headline,
                detail = synthesis.detail,
                isUnavailable = synthesis.state == "unavailable"
            )
            Spacer(modifier = Modifier.height(paddingLarge))
        }

        // --- RETAIL SENTIMENT ---
        positioningData.retailSentiment?.let { retail ->
            SectionLabel(
                text = stringResource(id = R.string.positioning_retail_sentiment_section),
                infoTitle = stringResource(id = R.string.positioning_retail_sentiment_section),
                infoDescription = stringResource(id = R.string.positioning_retail_sentiment_description)
            )
            Spacer(modifier = Modifier.height(paddingMedium))
            RetailSentimentCard(retail, onNavigateToGlossaryDetail)
            Spacer(modifier = Modifier.height(paddingLarge))
        }

        // --- INSTITUTIONAL POSITIONING (CFTC COT) ---
        positioningData.institutionalPositioning?.let { institutional ->
            SectionLabel(
                text = stringResource(id = R.string.positioning_institutional_section),
                infoTitle = stringResource(id = R.string.positioning_institutional_section),
                infoDescription = stringResource(id = R.string.positioning_institutional_description)
            )
            Spacer(modifier = Modifier.height(paddingMedium))
            InstitutionalPositioningCard(institutional, onNavigateToGlossaryDetail)
            Spacer(modifier = Modifier.height(paddingLarge))
        }

        // --- SHORT INTEREST (FINRA) ---
        positioningData.shortInterest?.let { shortInterest ->
            SectionLabel(
                text = stringResource(id = R.string.positioning_short_interest_section),
                infoTitle = stringResource(id = R.string.positioning_short_interest_section),
                infoDescription = stringResource(id = R.string.positioning_short_interest_description)
            )
            Spacer(modifier = Modifier.height(paddingMedium))
            ShortInterestCombinedCard(shortInterest, onNavigateToGlossaryDetail)
        }
    }
}

/**
 * Section label + info icon (2026-08-27: new, backed by the group descriptions the backend used to
 * send as `*.description`). All 3 sub-headers (Retail Sentiment, Institutional Positioning, Short
 * Interest) share one `titleSmall` style -- was `labelSmall` until Institutional Positioning and
 * Short Interest were bumped up for visual weight once they started heading a multi-row combined
 * card; Retail Sentiment was brought up to match rather than left as the odd one out.
 */
@Composable
private fun SectionLabel(text: String, infoTitle: String, infoDescription: String?) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_small)))
        MetricInfoAction(title = infoTitle, description = infoDescription)
    }
}

/**
 * Positioning's own deltas (retail bull-bear spread, COT % OI, short-interest shares) don't have
 * an unambiguous "up is good/bad" reading the way Posture's three gauges do -- a crowded COT
 * reading is a caution flag in EITHER direction, and retail sentiment is read contrarian (see
 * RetailSentimentStatus's own doc comment). So every delta pill here uses the plain neutral tone,
 * reporting magnitude only -- the SignalPill status badge next to it is what actually carries the
 * bullish/bearish/caution read for that card.
 */
@Composable
private fun neutralDeltaColors(): Pair<Color, Color> {
    val pulseColors = LocalPulseColors.current
    return pulseColors.signalNeutralPill to pulseColors.signalNeutralText
}

private fun DeltaDirection.toChangeDirection(): ChangeDirection = when (this) {
    DeltaDirection.UP -> ChangeDirection.UP
    DeltaDirection.DOWN -> ChangeDirection.DOWN
    DeltaDirection.FLAT, DeltaDirection.UNKNOWN -> ChangeDirection.FLAT
}

@Composable
private fun RetailSentimentCard(retail: DomainRetailSentiment, onNavigateToGlossaryDetail: (List<String>, String, String?, String?) -> Unit) {
    val status = RetailSentimentStatus.fromString(retail.status)
    val title = stringResource(id = R.string.positioning_retail_sentiment_title)
    val description = stringResource(id = R.string.positioning_retail_sentiment_description)

    PulseCard(
        style = PulseCardStyle.DATA,
        modifier = Modifier.fillMaxWidth(),
        onClick = { onNavigateToGlossaryDetail(listOf("positioning.aaii_bull_bear_spread"), title, description, retail.status) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(id = R.dimen.padding_large)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))
                MetricStatusRow(
                    label = stringResource(id = R.string.insights_label_sentiment),
                    statusText = retail.status ?: stringResource(id = R.string.insights_status_unknown),
                    pillColor = status.pillColor,
                    contentColor = status.textColor
                )

                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_large)))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = retail.bullBearSpread?.let { formatPts(it) } ?: stringResource(id = R.string.insights_value_unavailable),
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    retail.delta?.let { delta ->
                        val (pillColor, textColor) = neutralDeltaColors()
                        Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_small)))
                        DirectionalChangePill(
                            changeText = formatPts(abs(delta)),
                            direction = retail.deltaDirection.toChangeDirection(),
                            pillColor = pillColor,
                            contentColor = textColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))
                TriSegmentBar(
                    bullFraction = ((retail.bullPct ?: 0.0) / 100.0).toFloat(),
                    neutralFraction = ((retail.neutralPct ?: 0.0) / 100.0).toFloat(),
                    bearFraction = ((retail.bearPct ?: 0.0) / 100.0).toFloat()
                )
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    RetailSplitCol(stringResource(id = R.string.positioning_retail_bull), retail.bullPct)
                    RetailSplitCol(stringResource(id = R.string.positioning_retail_neutral), retail.neutralPct)
                    RetailSplitCol(stringResource(id = R.string.positioning_retail_bear), retail.bearPct)
                }

                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))
                MetricCardFooter(asOfText = retail.reportedDate?.let { stringResource(id = R.string.insights_as_of, it.toDisplayDate()) })
            }

            Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_small)))
            GlossaryTapChevron(tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun RetailSplitCol(label: String, pct: Double?) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = "${String.format(Locale.US, "%.1f", pct ?: 0.0)}%",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/** "CFTC Legacy · Non-Commercial" / "CFTC TFF · Leveraged Funds" -- see `NetworkFuturesContract.methodology`'s doc comment for why DIA differs. */
@Composable
private fun methodologyLabel(methodology: String): String = when (methodology) {
    "tff_leveraged_funds" -> stringResource(id = R.string.positioning_futures_methodology_tff)
    else -> stringResource(id = R.string.positioning_futures_methodology_legacy)
}

/**
 * 2026-08-27: was 4 separate `PulseCard`s (one per contract) -- collapsed into one card holding a
 * compact row per contract, ES/DIA/NQ/RTY (the order Amrit asked for), separated by hairline
 * dividers matching `StockPreviewCard`'s divider treatment. The outer `Column` only carries VERTICAL
 * padding now -- each row insets its own horizontal padding instead -- so the divider (added with no
 * horizontal inset of its own) spans the card's full width edge-to-edge, not just the width between
 * the rows' side margins. Each row keeps its OWN tap target and chevron (not one tap for the whole
 * card), since a contract's status/percentile is only meaningful per-contract -- collapsing the
 * outer card chrome doesn't collapse what a tap means.
 */
@Composable
private fun InstitutionalPositioningCard(
    institutional: DomainInstitutionalPositioning,
    onNavigateToGlossaryDetail: (List<String>, String, String?, String?) -> Unit
) {
    data class ContractEntry(val label: String, val contract: DomainFuturesContract)

    val entries = listOfNotNull(
        institutional.es?.let { ContractEntry(stringResource(id = R.string.positioning_futures_es), it) },
        institutional.dia?.let { ContractEntry(stringResource(id = R.string.positioning_futures_dia), it) },
        institutional.nq?.let { ContractEntry(stringResource(id = R.string.positioning_futures_nq), it) },
        institutional.rty?.let { ContractEntry(stringResource(id = R.string.positioning_futures_rty), it) }
    )
    if (entries.isEmpty()) return

    val rowHorizontalPadding = Modifier.padding(horizontal = dimensionResource(id = R.dimen.padding_large))

    PulseCard(style = PulseCardStyle.DATA, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.padding_large))) {
            entries.forEachIndexed { index, entry ->
                FuturesContractRow(entry.label, entry.contract, onNavigateToGlossaryDetail, modifier = rowHorizontalPadding)
                if (index != entries.lastIndex) {
                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_large)))
                    HorizontalDivider(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                        thickness = dimensionResource(id = R.dimen.border_thin)
                    )
                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_large)))
                }
            }
        }
    }
}

/**
 * One contract's row inside `InstitutionalPositioningCard` -- content unchanged from the old
 * standalone card, just at tighter padding/type sizes (titleLarge value instead of headlineMedium,
 * padding_small/padding_tiny spacers instead of padding_large/padding_small) since 4 of these now
 * share one card instead of each getting a full card's worth of breathing room.
 *
 * `methodologyLabel(contract.methodology)` used to render directly on the row ("CFTC Legacy ·
 * Non-Commercial" / "CFTC TFF · Leveraged Funds") -- moved into the glossary detail page's intro
 * instead (passed as the nav call's `description` argument, which this row never used otherwise --
 * `institutional.description` is one of the fields the backend actively removed, see
 * `positioning_institutional_description`'s doc comment), since it's read-once context rather than
 * something that needs to compete for space on every row of a now-compact card.
 */
@Composable
private fun FuturesContractRow(
    displayName: String,
    contract: DomainFuturesContract,
    onNavigateToGlossaryDetail: (List<String>, String, String?, String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val status = CotPositioningStatus.fromString(contract.status)
    val methodologyText = methodologyLabel(contract.methodology)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                onNavigateToGlossaryDetail(
                    listOf("positioning.cot_nc_net_pct_oi", "positioning.cot_percentile"),
                    displayName,
                    methodologyText,
                    contract.status
                )
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = displayName,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_tiny)))
            MetricStatusRow(
                label = stringResource(id = R.string.insights_label_positioning),
                statusText = contract.status,
                pillColor = status.pillColor,
                contentColor = status.textColor
            )

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${String.format(Locale.US, "%.2f", contract.ncNetPctOi)}%",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                contract.delta?.let { delta ->
                    val (pillColor, textColor) = neutralDeltaColors()
                    Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_small)))
                    DirectionalChangePill(
                        changeText = formatPts(abs(delta), decimals = 2),
                        direction = contract.deltaDirection.toChangeDirection(),
                        pillColor = pillColor,
                        contentColor = textColor
                    )
                }
            }
            Text(
                text = stringResource(id = R.string.positioning_futures_caption),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))
            PercentileBar(percentile = contract.percentile, markerColor = status.textColor)
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_tiny)))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = stringResource(id = R.string.positioning_percentile_min), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    text = stringResource(id = R.string.positioning_futures_percentile, contract.percentile),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(text = stringResource(id = R.string.positioning_percentile_max), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // --- Layer 2: paired-field reading (percentile + status only mean something together) ---
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_tiny)))
            Text(
                text = cotReadingCaption(percentile = contract.percentile, status = status),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))
            MetricCardFooter(asOfText = stringResource(id = R.string.positioning_futures_report_date, contract.reportDate.toDisplayDate()))
        }

        Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_small)))
        GlossaryTapChevron(tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/**
 * Layer 2 (2026-08-27 interpretive-layer spec): `nc_net_pct_oi` and `percentile` are only
 * interpretable together (a -9.93% net position reads meaningless without knowing whether that's
 * a mundane wobble or a rare extreme) -- this pairs them into one de-emphasized on-card line,
 * visible without a glossary tap, rather than requiring the reader to open two separate entries
 * and connect them mentally. Client-computed per the spec's own §6 recommendation (cheap, no
 * backend round-trip needed since percentile+status are already on the domain model).
 */
@Composable
private fun cotReadingCaption(percentile: Int, status: CotPositioningStatus): String = when (status) {
    CotPositioningStatus.EXTREME_LONG_CROWDED -> stringResource(id = R.string.positioning_futures_reading_extreme_long, percentile.withOrdinalSuffix())
    CotPositioningStatus.EXTREME_SHORT_CROWDED -> stringResource(id = R.string.positioning_futures_reading_extreme_short, percentile.withOrdinalSuffix())
    CotPositioningStatus.INSUFFICIENT_HISTORY -> stringResource(id = R.string.positioning_futures_reading_insufficient_history)
    CotPositioningStatus.NEUTRAL, CotPositioningStatus.UNKNOWN -> stringResource(id = R.string.positioning_futures_reading_neutral, percentile.withOrdinalSuffix())
}

/** "7th", "42nd", "23rd", "11th" (the 11-13 range is always "th", not "st"/"nd"/"rd"). */
private fun Int.withOrdinalSuffix(): String {
    val suffix = if (this % 100 in 11..13) "th" else when (this % 10) {
        1 -> "st"
        2 -> "nd"
        3 -> "rd"
        else -> "th"
    }
    return "$this$suffix"
}

/**
 * 2026-08-27: was 6 separate `PulseCard`s (one per ETF proxy) -- collapsed into one card holding a
 * compact row per instrument, SPY/DIA/QQQ/RSP/IWM/MAGS (the order Amrit asked for), same reasoning
 * as `InstitutionalPositioningCard` above (one shared outer card, but each row keeps its own tap
 * target since each instrument's own status/days-to-cover/mom-change reading is independent).
 */
@Composable
private fun ShortInterestCombinedCard(
    shortInterest: DomainShortInterest,
    onNavigateToGlossaryDetail: (List<String>, String, String?, String?) -> Unit
) {
    data class InstrumentEntry(val label: String, val instrument: DomainShortInterestInstrument)

    val entries = listOfNotNull(
        shortInterest.spy?.let { InstrumentEntry(stringResource(id = R.string.positioning_etf_spy), it) },
        shortInterest.dia?.let { InstrumentEntry(stringResource(id = R.string.positioning_etf_dia), it) },
        shortInterest.qqq?.let { InstrumentEntry(stringResource(id = R.string.positioning_etf_qqq), it) },
        shortInterest.rsp?.let { InstrumentEntry(stringResource(id = R.string.positioning_etf_rsp), it) },
        shortInterest.iwm?.let { InstrumentEntry(stringResource(id = R.string.positioning_etf_iwm), it) },
        shortInterest.mags?.let { InstrumentEntry(stringResource(id = R.string.positioning_etf_mags), it) }
    )
    if (entries.isEmpty()) return

    val rowHorizontalPadding = Modifier.padding(horizontal = dimensionResource(id = R.dimen.padding_large))

    PulseCard(style = PulseCardStyle.DATA, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.padding_large))) {
            entries.forEachIndexed { index, entry ->
                ShortInterestRow(entry.label, entry.instrument, shortInterest.description, onNavigateToGlossaryDetail, modifier = rowHorizontalPadding)
                if (index != entries.lastIndex) {
                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_large)))
                    HorizontalDivider(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                        thickness = dimensionResource(id = R.dimen.border_thin)
                    )
                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_large)))
                }
            }
        }
    }
}

/** One instrument's row inside `ShortInterestCombinedCard` -- content unchanged from the old
 * standalone card (titleLarge value instead of headlineMedium, tighter spacers), same reasoning as
 * `FuturesContractRow`. */
@Composable
private fun ShortInterestRow(
    displayName: String,
    instrument: DomainShortInterestInstrument,
    description: String?,
    onNavigateToGlossaryDetail: (List<String>, String, String?, String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val status = ShortInterestStatus.fromString(instrument.status)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                onNavigateToGlossaryDetail(
                    listOf(
                        "positioning.short_interest_days_to_cover",
                        "positioning.short_interest_shares",
                        "positioning.short_interest_mom_change"
                    ),
                    displayName,
                    description,
                    instrument.status
                )
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = displayName,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_tiny)))
            MetricStatusRow(
                label = stringResource(id = R.string.insights_label_status),
                statusText = instrument.status,
                pillColor = status.pillColor,
                contentColor = status.textColor
            )

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${String.format(Locale.US, "%.1f", instrument.daysToCover)}x",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                instrument.delta?.let { delta ->
                    val (pillColor, textColor) = neutralDeltaColors()
                    Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_small)))
                    DirectionalChangePill(
                        changeText = formatShares(abs(delta)),
                        direction = instrument.deltaDirection.toChangeDirection(),
                        pillColor = pillColor,
                        contentColor = textColor
                    )
                }
            }
            Text(
                text = stringResource(
                    id = R.string.positioning_short_interest_caption,
                    formatShares(instrument.shortShares.toDouble())
                ),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // --- Layer 2: paired-field reading (days-to-cover + mom-change + status only mean
            // something together -- see FuturesContractRow's cotReadingCaption for the identical
            // rationale). ---
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_tiny)))
            Text(
                text = shortInterestReadingCaption(instrument = instrument, status = status),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))
            MetricCardFooter(asOfText = stringResource(id = R.string.positioning_short_interest_settled, instrument.settlementDate.toDisplayDate()))
        }

        Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_small)))
        GlossaryTapChevron(tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** Layer 2 (2026-08-27 interpretive-layer spec) -- see `cotReadingCaption`'s identical rationale. */
@Composable
private fun shortInterestReadingCaption(instrument: DomainShortInterestInstrument, status: ShortInterestStatus): String {
    val daysToCoverText = String.format(Locale.US, "%.1f", instrument.daysToCover)
    return when (status) {
        ShortInterestStatus.ELEVATED_CROWDED_SHORT -> stringResource(id = R.string.positioning_short_interest_reading_elevated, daysToCoverText)
        ShortInterestStatus.COVERING_SHORTS_EXITING -> stringResource(id = R.string.positioning_short_interest_reading_covering, String.format(Locale.US, "%.1f", abs(instrument.momChangePct)))
        ShortInterestStatus.BUILDING_SHORTS_ADDING -> stringResource(id = R.string.positioning_short_interest_reading_building, String.format(Locale.US, "%.1f", abs(instrument.momChangePct)))
        ShortInterestStatus.NEUTRAL, ShortInterestStatus.UNKNOWN -> stringResource(id = R.string.positioning_short_interest_reading_neutral, daysToCoverText)
    }
}

/**
 * Raw share counts read far more naturally with a K/M suffix than as a bare integer. Takes a
 * `Double` so it works for both the instrument's own `shortShares` (Long) and its `delta` (Double
 * -- see `DomainShortInterestInstrument`'s doc comment on why gaugeDocument.ts's generic delta
 * computation is modeled as Double even for a whole-share-count metric) without two near-duplicate
 * formatters. `@Composable` so the "million"/"thousand"/plain "shares" suffix comes from
 * `strings.xml` rather than being hardcoded here -- only the numeric formatting is done in Kotlin.
 */
@Composable
private fun formatShares(shares: Double): String {
    val absShares = abs(shares)
    return when {
        absShares >= 1_000_000 -> stringResource(id = R.string.insights_shares_millions, String.format(Locale.US, "%.2f", shares / 1_000_000.0))
        absShares >= 1_000 -> stringResource(id = R.string.insights_shares_thousands, String.format(Locale.US, "%.0f", shares / 1_000.0))
        else -> stringResource(id = R.string.insights_shares_plain, shares.toLong().toString())
    }
}

/** "%.Nf pts" -- the delta-magnitude suffix shared by retail sentiment and COT futures cards, pulled from `strings.xml` rather than hardcoded per call site. */
@Composable
private fun formatPts(value: Double, decimals: Int = 1): String =
    stringResource(id = R.string.insights_pts_suffix, String.format(Locale.US, "%.${decimals}f", value))

// ============================================================================
// 🎨 PREVIEWS
// ============================================================================

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun PreviewMarketPositioningSection() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        val mockData = DomainMarketPositioning(
            retailSentiment = DomainRetailSentiment(
                bullPct = 35.5, bearPct = 39.9, neutralPct = 24.6, bullBearSpread = -4.4,
                status = "NEUTRAL", reportedDate = "2026-08-20",
                description = null,
                delta = -1.2, deltaDirection = DeltaDirection.DOWN
            ),
            institutionalPositioning = DomainInstitutionalPositioning(
                es = DomainFuturesContract(-0.51, 12000, "NEUTRAL", 42, "2026-08-18", "legacy_non_commercial", delta = -1.53, deltaDirection = DeltaDirection.DOWN),
                nq = DomainFuturesContract(-3.50, 8000, "NEUTRAL", 55, "2026-08-11", "legacy_non_commercial", delta = 0.60, deltaDirection = DeltaDirection.UP),
                rty = DomainFuturesContract(-9.93, 15000, "EXTREME SHORT (CROWDED)", 7, "2026-08-18", "legacy_non_commercial", delta = -1.83, deltaDirection = DeltaDirection.DOWN),
                dia = DomainFuturesContract(2.26, 2035, "EXTREME LONG (CROWDED)", 96, "2026-08-18", "tff_leveraged_funds", delta = null, deltaDirection = DeltaDirection.FLAT),
                description = null,
                fetchedAt = System.currentTimeMillis(), staleSince = null
            ),
            shortInterest = DomainShortInterest(
                spy = DomainShortInterestInstrument(45_230_000, 1.8, 2.2, "2026-08-14", "NEUTRAL", delta = 970_000.0, deltaDirection = DeltaDirection.DOWN),
                qqq = DomainShortInterestInstrument(28_110_000, 1.2, -3.1, "2026-08-14", "NEUTRAL", delta = 1_150_000.0, deltaDirection = DeltaDirection.UP),
                iwm = DomainShortInterestInstrument(62_480_000, 3.4, 16.2, "2026-08-14", "BUILDING (SHORTS ADDING)", delta = 9_840_000.0, deltaDirection = DeltaDirection.UP),
                dia = DomainShortInterestInstrument(3_832_828, 1.07, 14.71, "2026-08-14", "NEUTRAL", delta = null, deltaDirection = DeltaDirection.FLAT),
                rsp = DomainShortInterestInstrument(29_974_892, 4.54, 13.74, "2026-08-14", "NEUTRAL", delta = null, deltaDirection = DeltaDirection.FLAT),
                mags = DomainShortInterestInstrument(2_461_127, 1.0, -34.97, "2026-08-14", "COVERING (SHORTS EXITING)", delta = null, deltaDirection = DeltaDirection.FLAT),
                description = null,
                fetchedAt = System.currentTimeMillis(), staleSince = null
            ),
            synthesis = DomainPositioningSynthesis(
                headline = "Polarized institutional positioning clashes with cautious retail sentiment",
                detail = "While retail investors maintain a cautious stance, institutional futures positioning reveals highly polarized, crowded exposures, featuring extreme longs in the S&P 500 and Dow alongside extreme shorts in the Nasdaq-100.",
                generatedAt = System.currentTimeMillis(),
                contentFlags = emptyList(),
                state = "ok"
            ),
            timestamp = System.currentTimeMillis()
        )
        Column(modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))) {
            MarketPositioningSection(
                mockData,
                onNavigateToGlossaryDetail = { _, _, _, _ -> },
                isIntroDismissed = true,
                onDismissIntro = {}
            )
        }
    }
}
