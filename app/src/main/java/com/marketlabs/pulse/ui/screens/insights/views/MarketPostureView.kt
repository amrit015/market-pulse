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
import com.marketlabs.pulse.storage.model.posture.DomainDarkPoolIndex
import com.marketlabs.pulse.storage.model.posture.DomainMarketPosture
import com.marketlabs.pulse.storage.model.posture.DomainNaaimExposure
import com.marketlabs.pulse.storage.model.posture.DomainNetLiquidity
import com.marketlabs.pulse.storage.model.posture.DomainPostureSynthesis
import com.marketlabs.pulse.ui.components.FirstTimeExplainerCard
import com.marketlabs.pulse.ui.components.MetricCardFooter
import com.marketlabs.pulse.ui.components.MetricStatusRow
import com.marketlabs.pulse.ui.components.PulseCard
import com.marketlabs.pulse.ui.components.PulseCardStyle
import com.marketlabs.pulse.ui.components.SynthesisHeroCard
import com.marketlabs.pulse.ui.components.widgets.ChangeDirection
import com.marketlabs.pulse.ui.components.widgets.DirectionalChangePill
import com.marketlabs.pulse.ui.components.widgets.GlossaryTapChevron
import com.marketlabs.pulse.ui.components.widgets.MetricInfoAction
import com.marketlabs.pulse.ui.components.widgets.RingGauge
import com.marketlabs.pulse.ui.theme.LocalPulseColors
import com.marketlabs.pulse.ui.theme.MarketPulseTheme
import com.marketlabs.pulse.ui.theme.pillColor
import com.marketlabs.pulse.ui.theme.textColor
import com.marketlabs.pulse.utils.enums.DeltaDirection
import com.marketlabs.pulse.utils.enums.DixStatus
import com.marketlabs.pulse.utils.enums.NaaimStatus
import com.marketlabs.pulse.utils.enums.NetLiquidityStatus
import com.marketlabs.pulse.utils.toDisplayDate
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * `onNavigateToGlossaryDetail` -- 2026-08-27 convergence: the whole CARD is the tap target now
 * (was individual values with their own chevrons), so this pushes to a merged glossary-detail page
 * covering everything the card shows. Each Posture card maps to exactly one `core/glossary/` entry
 * (`metricIds` is a single-element list every time here, unlike Positioning's multi-field COT/
 * short-interest cards), but the callback shape stays a `List<String>` for both domains rather than
 * having two different signatures.
 */
@Composable
fun InstitutionalPostureSection(
    postureData: DomainMarketPosture,
    onNavigateToGlossaryDetail: (metricIds: List<String>, title: String, description: String?, status: String?) -> Unit,
    isIntroDismissed: Boolean,
    onDismissIntro: () -> Unit
) {
    val paddingMedium = dimensionResource(id = R.dimen.padding_medium)
    val paddingLarge = dimensionResource(id = R.dimen.padding_large)

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
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
                text = stringResource(id = R.string.posture_section_title),
                style = textStyle,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_small)))

            // 💡 2026-08-27 convergence: this icon and the first-time explainer card below now
            // show the SAME merged string (posture_explainer_text) -- was two separate, partly
            // overlapping paragraphs.
            MetricInfoAction(
                title = stringResource(id = R.string.posture_section_title),
                description = stringResource(id = R.string.posture_explainer_text)
            )
        }

        val date = postureData.timestamp?.let { Date(it) } ?: Date()
        val format = SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault())

        Text(
            text = stringResource(id = R.string.analyzed_at, format.format(date)),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_micro))
        )

        // --- FIRST-TIME EXPLAINER (2026-08-27 interpretive-layer spec, Layer 3) -- stays visible
        // (not auto-dismissed by anything else) until the reader taps "Got it" here. ---
        if (!isIntroDismissed) {
            Spacer(modifier = Modifier.height(paddingLarge))
            FirstTimeExplainerCard(
                text = stringResource(id = R.string.posture_explainer_text),
                onDismiss = onDismissIntro
            )
        }

        // --- SYNTHESIS HERO ---
        // 💡 Every block below (synthesis, then each metric card) owns its OWN leading spacer
        // rather than the block before it owning a trailing one, so removing/reordering any single
        // block never leaves a doubled or missing gap next to its neighbors.
        postureData.synthesis?.let { synthesis ->
            Spacer(modifier = Modifier.height(paddingLarge))
            SynthesisHeroCard(
                headline = synthesis.headline,
                detail = synthesis.detail,
                isUnavailable = synthesis.state == "unavailable"
            )
        }

        // --- METRIC CARDS ---
        postureData.naaimExposure?.let {
            Spacer(modifier = Modifier.height(paddingLarge))
            NaaimExposureCard(it, onNavigateToGlossaryDetail)
        }
        postureData.darkPoolIndex?.let {
            Spacer(modifier = Modifier.height(paddingLarge))
            DarkPoolCard(it, onNavigateToGlossaryDetail)
        }
        postureData.netLiquidity?.let {
            Spacer(modifier = Modifier.height(paddingLarge))
            NetLiquidityCard(it, onNavigateToGlossaryDetail)
        }
    }
}

/** "%.1f pts" -- pulled from `strings.xml` (shared with MarketPositioningView.kt's identical suffix) rather than hardcoded here. */
@Composable
private fun formatDeltaPts(delta: Double?): String? =
    delta?.let { stringResource(id = R.string.insights_pts_suffix, String.format(Locale.US, "%.1f", abs(it))) }

private fun DeltaDirection.toChangeDirection(): ChangeDirection = when (this) {
    DeltaDirection.UP -> ChangeDirection.UP
    DeltaDirection.DOWN -> ChangeDirection.DOWN
    DeltaDirection.FLAT, DeltaDirection.UNKNOWN -> ChangeDirection.FLAT
}

/**
 * Posture's three gauges all have an unambiguous "up is good/bad" reading (rising manager
 * exposure/dark pool accumulation/net liquidity are each bullish), unlike Positioning's COT/short-
 * interest/retail-spread deltas -- see MarketPositioningView.kt's identical-purpose helper for why
 * those instead always use the neutral tone.
 */
@Composable
private fun bullishBearishDeltaColors(direction: DeltaDirection): Pair<Color, Color> {
    val pulseColors = LocalPulseColors.current
    return when (direction) {
        DeltaDirection.UP -> pulseColors.signalBullishPill to pulseColors.signalBullishText
        DeltaDirection.DOWN -> pulseColors.signalBearishPill to pulseColors.signalBearishText
        DeltaDirection.FLAT, DeltaDirection.UNKNOWN -> pulseColors.signalNeutralPill to pulseColors.signalNeutralText
    }
}

@Composable
private fun NaaimExposureCard(naaim: DomainNaaimExposure, onNavigateToGlossaryDetail: (List<String>, String, String?, String?) -> Unit) {
    val status = NaaimStatus.fromString(naaim.status)
    val title = stringResource(id = R.string.posture_naaim_title)
    val description = stringResource(id = R.string.posture_naaim_description)

    PulseCard(
        style = PulseCardStyle.DATA,
        modifier = Modifier.fillMaxWidth(),
        onClick = { onNavigateToGlossaryDetail(listOf("posture.naaim_exposure"), title, description, naaim.status) }
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
                    label = stringResource(id = R.string.insights_label_status),
                    statusText = naaim.status ?: stringResource(id = R.string.insights_status_unknown),
                    pillColor = status.pillColor,
                    contentColor = status.textColor
                )

                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_large)))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (naaim.value != null) {
                        RingGauge(value = naaim.value, maxValue = 100.0, ringColor = status.textColor)
                        Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_large)))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${String.format(Locale.US, "%.1f", naaim.value)}%",
                                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                formatDeltaPts(naaim.delta)?.let { deltaText ->
                                    val (pillColor, textColor) = bullishBearishDeltaColors(naaim.deltaDirection)
                                    Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_small)))
                                    DirectionalChangePill(
                                        changeText = deltaText,
                                        direction = naaim.deltaDirection.toChangeDirection(),
                                        pillColor = pillColor,
                                        contentColor = textColor
                                    )
                                }
                            }
                            Text(
                                text = stringResource(id = R.string.posture_naaim_caption),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Text(
                            text = stringResource(id = R.string.insights_value_unavailable),
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))
                MetricCardFooter(
                    asOfText = naaim.fetchedAt?.let { stringResource(id = R.string.insights_as_of, SimpleDateFormat("MMM d, yyyy", Locale.US).format(Date(it))) }
                )
            }

            Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_small)))
            GlossaryTapChevron(tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun DarkPoolCard(dix: DomainDarkPoolIndex, onNavigateToGlossaryDetail: (List<String>, String, String?, String?) -> Unit) {
    val status = DixStatus.fromString(dix.status)
    val title = stringResource(id = R.string.posture_dix_title)
    val description = stringResource(id = R.string.posture_dix_description)

    PulseCard(
        style = PulseCardStyle.DATA,
        modifier = Modifier.fillMaxWidth(),
        onClick = { onNavigateToGlossaryDetail(listOf("posture.dark_pool_index"), title, description, dix.status) }
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
                    label = stringResource(id = R.string.insights_label_status),
                    statusText = dix.status ?: stringResource(id = R.string.insights_status_unknown),
                    pillColor = status.pillColor,
                    contentColor = status.textColor
                )

                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_large)))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (dix.value != null) {
                        RingGauge(value = dix.value, maxValue = 100.0, ringColor = status.textColor)
                        Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_large)))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = String.format(Locale.US, "%.1f", dix.value),
                                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                formatDeltaPts(dix.delta)?.let { deltaText ->
                                    val (pillColor, textColor) = bullishBearishDeltaColors(dix.deltaDirection)
                                    Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_small)))
                                    DirectionalChangePill(
                                        changeText = deltaText,
                                        direction = dix.deltaDirection.toChangeDirection(),
                                        pillColor = pillColor,
                                        contentColor = textColor
                                    )
                                }
                            }
                            Text(
                                text = stringResource(id = R.string.posture_dix_caption),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Text(
                            text = stringResource(id = R.string.insights_value_unavailable),
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))
                MetricCardFooter(asOfText = dix.date?.let { stringResource(id = R.string.insights_as_of, it.toDisplayDate()) })
            }

            Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_small)))
            GlossaryTapChevron(tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun NetLiquidityCard(liquidity: DomainNetLiquidity, onNavigateToGlossaryDetail: (List<String>, String, String?, String?) -> Unit) {
    val status = NetLiquidityStatus.fromString(liquidity.status)
    val title = stringResource(id = R.string.posture_net_liquidity_title)
    val description = stringResource(id = R.string.posture_net_liquidity_description)

    PulseCard(
        style = PulseCardStyle.DATA,
        modifier = Modifier.fillMaxWidth(),
        onClick = { onNavigateToGlossaryDetail(listOf("posture.net_liquidity"), title, description, liquidity.status) }
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
                    label = stringResource(id = R.string.insights_label_trend),
                    statusText = liquidity.status ?: stringResource(id = R.string.insights_status_unknown),
                    pillColor = status.pillColor,
                    contentColor = status.textColor
                )

                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_large)))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$${String.format(Locale.US, "%.2f", liquidity.value ?: 0.0)}T",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    formatNetLiquidityDelta(liquidity.delta)?.let { deltaText ->
                        val (pillColor, textColor) = bullishBearishDeltaColors(liquidity.deltaDirection)
                        Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_small)))
                        DirectionalChangePill(
                            changeText = deltaText,
                            direction = liquidity.deltaDirection.toChangeDirection(),
                            pillColor = pillColor,
                            contentColor = textColor
                        )
                    }
                }
                Text(
                    text = stringResource(id = R.string.posture_net_liquidity_caption),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val separator = stringResource(id = R.string.posture_net_liquidity_separator)
                    LiquidityComponentCol(stringResource(id = R.string.posture_net_liquidity_fed_assets), liquidity.assetsT, isPositive = true)
                    Text(separator, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    LiquidityComponentCol(stringResource(id = R.string.posture_net_liquidity_tga), liquidity.tgaT, isPositive = false)
                    Text(separator, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    LiquidityComponentCol(stringResource(id = R.string.posture_net_liquidity_rrp), liquidity.rrpT, isPositive = false)
                }

                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))
                MetricCardFooter(asOfText = liquidity.date?.let { stringResource(id = R.string.insights_as_of, it.toDisplayDate()) })
            }

            Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_small)))
            GlossaryTapChevron(tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** Net liquidity deltas are in $T -- sub-$1T moves read far more naturally as "$34B" than "$0.03T". */
private fun formatNetLiquidityDelta(deltaT: Double?): String? {
    if (deltaT == null) return null
    val absT = abs(deltaT)
    return if (absT < 1.0) {
        "$${(absT * 1000).roundToIntOrZero()}B"
    } else {
        "$${String.format(Locale.US, "%.2f", absT)}T"
    }
}

private fun Double.roundToIntOrZero(): Int = if (isFinite()) this.roundToInt() else 0

@Composable
private fun LiquidityComponentCol(label: String, valueT: Double?, isPositive: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "$${String.format(Locale.US, "%.2f", valueT ?: 0.0)}T",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = if (isPositive) LocalPulseColors.current.signalBullishText else LocalPulseColors.current.signalBearishText
        )
    }
}

// ============================================================================
// 🎨 PREVIEWS
// ============================================================================

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
fun PreviewInstitutionalPosture() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        val mockData = DomainMarketPosture(
            naaimExposure = DomainNaaimExposure(
                value = 84.2,
                status = "BULLISH",
                description = null,
                delta = 4.4,
                deltaDirection = DeltaDirection.UP,
                fetchedAt = System.currentTimeMillis()
            ),
            darkPoolIndex = DomainDarkPoolIndex(
                value = 46.8,
                date = "2026-08-25",
                status = "ACCUMULATION (BULLISH)",
                description = null,
                delta = 1.9,
                deltaDirection = DeltaDirection.UP
            ),
            netLiquidity = DomainNetLiquidity(
                value = 6.14,
                status = "EXPANDING",
                assetsT = 7.32,
                tgaT = 0.65,
                rrpT = 0.43,
                date = "2026-08-20",
                description = null,
                delta = 0.034,
                deltaDirection = DeltaDirection.UP
            ),
            synthesis = DomainPostureSynthesis(
                headline = "Institutions accumulating as liquidity expands",
                detail = "Dark pool buying crossed into accumulation territory while net liquidity ticked up for a second straight reading, and manager exposure climbed to a bullish 84%.",
                generatedAt = System.currentTimeMillis(),
                contentFlags = emptyList(),
                state = "ok"
            ),
            timestamp = System.currentTimeMillis()
        )
        Column(modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))) {
            InstitutionalPostureSection(
                mockData,
                onNavigateToGlossaryDetail = { _, _, _, _ -> },
                isIntroDismissed = true,
                onDismissIntro = {}
            )
        }
    }
}
