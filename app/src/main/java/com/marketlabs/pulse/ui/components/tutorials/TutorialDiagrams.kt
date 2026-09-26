package com.marketlabs.pulse.ui.components.tutorials

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.marketlabs.pulse.R
import com.marketlabs.pulse.ui.components.diagrams.BreadthDivergenceChart
import com.marketlabs.pulse.ui.components.diagrams.CorrelationBreakdownPair
import com.marketlabs.pulse.ui.components.diagrams.DiagramScaffold
import com.marketlabs.pulse.ui.components.diagrams.DiagramSize
import com.marketlabs.pulse.ui.components.diagrams.GrowthVsValueSpectrum
import com.marketlabs.pulse.ui.components.diagrams.IllustrativeNote
import com.marketlabs.pulse.ui.components.diagrams.MacroVitalsFlow
import com.marketlabs.pulse.ui.components.diagrams.MarketPhasesWheel
import com.marketlabs.pulse.ui.components.diagrams.PeContractionBar
import com.marketlabs.pulse.ui.components.diagrams.PeExpansionBar
import com.marketlabs.pulse.ui.components.diagrams.PositioningCadenceStrip
import com.marketlabs.pulse.ui.components.diagrams.RateSensitivitySpectrum
import com.marketlabs.pulse.ui.components.diagrams.RelativeStrengthDivergenceChart
import com.marketlabs.pulse.ui.components.diagrams.RoleOfLiquidityWaterfall
import com.marketlabs.pulse.ui.components.diagrams.SectorRotationWheel
import com.marketlabs.pulse.ui.components.diagrams.StockAnalysisFlow
import com.marketlabs.pulse.ui.components.diagrams.ValuationBasicsSpectrum
import com.marketlabs.pulse.ui.components.widgets.CardEyebrowLabel
import com.marketlabs.pulse.ui.components.widgets.SignalPill
import com.marketlabs.pulse.ui.theme.LocalPulseColors
import com.marketlabs.pulse.ui.theme.MarketPulseTheme
import com.marketlabs.pulse.ui.theme.PulseColors

/**
 * Four inline, code-authored, illustrative-only
 * diagrams for the Tutorials articles (no live data, no Gemini). D2 and D3 are "threshold-bearing"
 * -- their band cutoffs are written to match the exact `metric_glossary.json` constants for
 * `sma_extension` (D2) and `fear_and_greed`/`put_call_ratio`/`vix` (D3), so they go stale the same
 * way that JSON's own maintenance note describes if those constants ever move. D1 and D4 are
 * conceptual shapes only, not tied to a coded threshold.
 */

// ============================================================================
// D1 -- Inverted yield curve (Macro concepts + yield_curve glossary)
// ============================================================================

@Composable
fun YieldCurveDiagram(modifier: Modifier = Modifier) {
    val pulseColors = LocalPulseColors.current
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensionResource(id = R.dimen.tutorial_diagram_height)),
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_large))
        ) {
            YieldCurvePanel(
                label = stringResource(id = R.string.tutorial_diagram_yield_curve_normal_label),
                inverted = false,
                lineColor = pulseColors.signalBullishText,
                modifier = Modifier.weight(1f)
            )
            YieldCurvePanel(
                label = stringResource(id = R.string.tutorial_diagram_yield_curve_inverted_label),
                inverted = true,
                lineColor = pulseColors.signalBearishText,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun YieldCurvePanel(label: String, inverted: Boolean, lineColor: Color, modifier: Modifier = Modifier) {
    val axisColor = LocalPulseColors.current.onSurfaceMuted
    Column(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxWidth().weight(1f)) {
            val inset = size.height * 0.15f
            // Axes
            drawLine(
                color = axisColor,
                start = Offset(0f, size.height - inset),
                end = Offset(size.width, size.height - inset),
                strokeWidth = 1.dp.toPx()
            )
            val start = if (inverted) Offset(0f, inset) else Offset(0f, size.height - inset)
            val end = if (inverted) Offset(size.width, size.height - inset) else Offset(size.width, inset)
            drawLine(
                color = lineColor,
                start = start,
                end = end,
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = lineColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// ============================================================================
// D2 -- 200-day SMA + extension bands (Technical concepts + sma_extension glossary).
// Threshold-bearing: band split matches sma_extension's -5%/+10% constants.
// ============================================================================

@Composable
fun SmaExtensionDiagram(modifier: Modifier = Modifier) {
    val pulseColors = LocalPulseColors.current
    val overextendedColor = pulseColors.signalWarningText
    val trendColor = pulseColors.onSurfaceMuted
    val undervaluedColor = pulseColors.signalBullishText
    val priceLineColor = pulseColors.accentPrimary

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensionResource(id = R.dimen.tutorial_diagram_height))
        ) {
            // Zone bands, top to bottom: Overextended (>=+10%), Trend Level (-5%..+10%), Undervalued (<=-5%).
            // A -5%..+10% band out of an illustrative -20%..+20% total range puts the split at 30%/50% from the top.
            val overextendedBottom = size.height * 0.30f
            val trendBottom = size.height * 0.65f
            drawRect(color = overextendedColor.copy(alpha = 0.14f), size = size.copy(height = overextendedBottom))
            drawRect(
                color = trendColor.copy(alpha = 0.10f),
                topLeft = Offset(0f, overextendedBottom),
                size = size.copy(height = trendBottom - overextendedBottom)
            )
            drawRect(
                color = undervaluedColor.copy(alpha = 0.14f),
                topLeft = Offset(0f, trendBottom),
                size = size.copy(height = size.height - trendBottom)
            )

            // Dashed 200d SMA reference line through the middle of the Trend Level band.
            val smaY = (overextendedBottom + trendBottom) / 2
            drawLine(
                color = trendColor,
                start = Offset(0f, smaY),
                end = Offset(size.width, smaY),
                strokeWidth = 1.5.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f))
            )

            // Wavy price line weaving from undervalued, through trend, up into overextended.
            val path = androidx.compose.ui.graphics.Path().apply {
                moveTo(0f, trendBottom + (size.height - trendBottom) * 0.5f)
                cubicTo(
                    size.width * 0.2f, size.height * 0.95f,
                    size.width * 0.35f, smaY,
                    size.width * 0.5f, smaY - 4.dp.toPx()
                )
                cubicTo(
                    size.width * 0.65f, overextendedBottom * 0.6f,
                    size.width * 0.85f, overextendedBottom * 0.2f,
                    size.width, overextendedBottom * 0.15f
                )
            }
            drawPath(path = path, color = priceLineColor, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
        }
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            SmaLegendChip(color = undervaluedColor, label = stringResource(id = R.string.tutorial_diagram_sma_undervalued_label))
            SmaLegendChip(color = trendColor, label = stringResource(id = R.string.tutorial_diagram_sma_trend_label))
            SmaLegendChip(color = overextendedColor, label = stringResource(id = R.string.tutorial_diagram_sma_overextended_label))
        }
    }
}

@Composable
private fun SmaLegendChip(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).background(color = color, shape = CircleShape))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = color)
    }
}

// ============================================================================
// D3 -- Sentiment gauges: three stacked gradient bars (Sentiment concepts + glossary).
// Threshold-bearing: band splits match fear_and_greed/put_call_ratio/vix constants.
// ============================================================================

@Composable
fun SentimentGaugesDiagram(modifier: Modifier = Modifier) {
    val pulseColors = LocalPulseColors.current
    val green = pulseColors.signalBullishText
    val amber = pulseColors.signalWarningText
    val red = pulseColors.signalBearishText
    val neutral = pulseColors.onSurfaceMuted

    Column(modifier = modifier.fillMaxWidth()) {
        // 💡 All three rows read CONTRARIAN, matching the real
        // SpeedometerGauge/PutCallHorizontalBar/VixFullWidthCard on the Dashboard -- Fear is
        // bullish-green (a potential buying opportunity), Greed is bearish-red (a caution), never
        // the plain-English "fear=bad=red" instinct, so the diagram teaches the same convention
        // the app actually uses.
        //
        // Fear & Greed 0-100: Extreme Fear 0-25 (green) / Fear 26-45 (amber) / Neutral 46-54 (gray) /
        // Greed 55-74 (amber) / Extreme Greed 75-100 (red).
        GaugeBarRow(
            title = stringResource(id = R.string.tutorial_diagram_fear_greed_label),
            segments = listOf(25f to green, 20f to amber, 9f to neutral, 20f to amber, 26f to red)
        )
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))
        // Put/Call -- inverted scale vs the other two: HIGH ratio = fear, so this bar is drawn
        // green-on-the-LEFT-at-a-HIGH-value too, by putting the "Extreme Fear" (>=1.0) segment
        // first, same left=fear/right=greed visual direction as the Fear & Greed bar above even
        // though the underlying number runs the opposite way -- the whole reason this needs its
        // own explicit "higher = more fearful" callout in the caption.
        GaugeBarRow(
            title = stringResource(id = R.string.tutorial_diagram_put_call_label),
            segments = listOf(25f to green, 15f to amber, 10f to neutral, 15f to amber, 35f to red)
        )
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))
        // VIX (the Dashboard's own Fear/Greed-style banding, not the Indicators Calm/Elevated/Panic
        // ruleset): low VIX/Calm (the "Greed" end) is red, high
        // VIX/Panic (the "Fear" end) is green, illustrated over roughly 10-40.
        GaugeBarRow(
            title = stringResource(id = R.string.tutorial_diagram_vix_label),
            segments = listOf(40f to red, 20f to amber, 40f to green)
        )
    }
}

@Composable
private fun GaugeBarRow(title: String, segments: List<Pair<Float, Color>>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(4.dp))
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensionResource(id = R.dimen.tutorial_diagram_gauge_bar_height))
        ) {
            val total = segments.sumOf { it.first.toDouble() }.toFloat()
            var x = 0f
            segments.forEach { (weight, color) ->
                val w = size.width * (weight / total)
                drawRect(color = color, topLeft = Offset(x, 0f), size = size.copy(width = w))
                x += w
            }
        }
    }
}

// ============================================================================
// D4 -- Gauge anatomy (the visual spine of "How to read any gauge")
//
// Unlike D1-D3, this is a literal explanation of the real raw-value -> band -> color mechanic
// every gauge in the app uses, not an illustrative scenario -- it reads real signal-color tokens
// directly (via `GaugePatternBandBar`/`SignalPill`) rather than the diagram library's banned-signal-
// color palette, and carries no "Illustrative -- not real data" caption (there's no data here to
// disclaim; the placeholder value stands for "any value," not a fictional one).
// ============================================================================

@Composable
fun GaugeAnatomyDiagram(modifier: Modifier = Modifier) {
    val pulseColors = LocalPulseColors.current
    val contentDescription = stringResource(id = R.string.tutorial_diagram_content_description_gauge_anatomy)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clearAndSetSemantics { this.contentDescription = contentDescription },
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_medium))
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            CardEyebrowLabel(text = stringResource(id = R.string.gauge_pattern_step_raw_value), color = pulseColors.onSurfaceMuted)
            Text(
                text = stringResource(id = R.string.gauge_pattern_raw_value),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        GaugePatternDownArrow()

        Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_small))) {
            CardEyebrowLabel(text = stringResource(id = R.string.gauge_pattern_step_band), color = pulseColors.onSurfaceMuted)
            GaugePatternBandBar(bullFraction = 1f / 3f, neutralFraction = 1f / 3f, bearFraction = 1f / 3f, markerFraction = 0.5f)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                GaugePatternBandLabel(text = stringResource(id = R.string.gauge_pattern_band_bullish), emphasized = false, colors = pulseColors)
                GaugePatternBandLabel(text = stringResource(id = R.string.gauge_pattern_band_neutral), emphasized = true, colors = pulseColors)
                GaugePatternBandLabel(text = stringResource(id = R.string.gauge_pattern_band_bearish), emphasized = false, colors = pulseColors)
            }
        }
        GaugePatternDownArrow()

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            CardEyebrowLabel(text = stringResource(id = R.string.gauge_pattern_step_color), color = pulseColors.onSurfaceMuted)
            SignalPill(
                text = stringResource(id = R.string.gauge_pattern_status_neutral),
                pillColor = pulseColors.signalNeutralPill,
                contentColor = pulseColors.signalNeutralText
            )
        }
    }
}

@Composable
private fun GaugePatternBandLabel(text: String, emphasized: Boolean, colors: PulseColors) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (emphasized) FontWeight.Bold else FontWeight.Normal),
        color = if (emphasized) MaterialTheme.colorScheme.onSurface else colors.onSurfaceMuted
    )
}

@Composable
private fun GaugePatternDownArrow() {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Icon(
            painter = painterResource(id = R.drawable.ic_arrow_down),
            contentDescription = null,
            tint = LocalPulseColors.current.onSurfaceMuted,
            modifier = Modifier.size(dimensionResource(id = R.dimen.icon_size_small))
        )
    }
}

/** Every diagram key [learnDiagramFor] resolves -- `learn_content.json` uses bare strings for its
 * diagram fields, so a typo'd or renamed key would otherwise compile, parse, and silently render
 * nothing; [LearnContentJsonTest] walks every `*_diagram` value in the file against this set. */
val KNOWN_DIAGRAM_KEYS = setOf(
    "sma_extension",
    "yield_curve",
    "sentiment_gauges",
    "gauge_anatomy",
    "sentiment_gauges_live",
    "vix_card_preview",
    "posture_rings_preview",
    "positioning_bars_preview",
    "rate_sensitivity",
    "growth_vs_value",
    "valuation_basics",
    "pe_expansion",
    "pe_contraction",
    "breadth",
    "relative_strength",
    "market_phases",
    "sector_rotation",
    "macro_vitals",
    "stock_analysis",
    "role_of_liquidity",
    "positioning",
    "correlation_breakdown"
)

/** Maps a `learn_content.json` diagram key to the composable it names, already wrapped in
 * [DiagramScaffold] -- `null` (no key, or an unrecognized one) means the card carries no diagram. */
fun learnDiagramFor(key: String?): (@Composable () -> Unit)? = when (key) {
    "sma_extension" -> ({
        DiagramScaffold(
            contentDescription = stringResource(id = R.string.tutorial_diagram_content_description_sma_extension),
            illustrativeNote = IllustrativeNote.SCHEMATIC,
            caption = stringResource(id = R.string.tutorial_diagram_sma_caption)
        ) { SmaExtensionDiagram() }
    })
    "yield_curve" -> ({
        DiagramScaffold(
            contentDescription = stringResource(id = R.string.tutorial_diagram_content_description_yield_curve),
            illustrativeNote = IllustrativeNote.SCHEMATIC,
            caption = stringResource(id = R.string.tutorial_diagram_yield_curve_caption)
        ) { YieldCurveDiagram() }
    })
    "sentiment_gauges" -> ({
        DiagramScaffold(
            contentDescription = stringResource(id = R.string.tutorial_diagram_content_description_sentiment_gauges),
            illustrativeNote = IllustrativeNote.SCHEMATIC,
            caption = stringResource(id = R.string.tutorial_diagram_sentiment_caption)
        ) { SentimentGaugesDiagram() }
    })
    "gauge_anatomy" -> ({ GaugeAnatomyDiagram() })
    "sentiment_gauges_live" -> ({
        DiagramScaffold(
            contentDescription = stringResource(id = R.string.tutorial_diagram_content_description_sentiment_gauges_live),
            illustrativeNote = IllustrativeNote.LIVE_WIDGET_PREVIEW
        ) { SentimentGaugesLivePreview() }
    })
    "vix_card_preview" -> ({
        DiagramScaffold(
            contentDescription = stringResource(id = R.string.tutorial_diagram_content_description_vix_card_preview),
            illustrativeNote = IllustrativeNote.LIVE_WIDGET_PREVIEW
        ) { VixCardPreview() }
    })
    "posture_rings_preview" -> ({
        DiagramScaffold(
            contentDescription = stringResource(id = R.string.tutorial_diagram_content_description_posture_rings_preview),
            illustrativeNote = IllustrativeNote.LIVE_WIDGET_PREVIEW
        ) { PostureRingsPreview() }
    })
    "positioning_bars_preview" -> ({
        DiagramScaffold(
            contentDescription = stringResource(id = R.string.tutorial_diagram_content_description_positioning_bars_preview),
            illustrativeNote = IllustrativeNote.LIVE_WIDGET_PREVIEW
        ) { PositioningBarsPreview() }
    })
    "rate_sensitivity" -> ({
        DiagramScaffold(
            contentDescription = stringResource(id = R.string.tutorial_diagram_content_description_rate_sensitivity),
            illustrativeNote = IllustrativeNote.SCHEMATIC,
            size = DiagramSize.DEFAULT,
            caption = stringResource(id = R.string.diagram_caption_rate_sensitivity)
        ) { RateSensitivitySpectrum(modifier = Modifier.fillMaxSize()) }
    })
    "growth_vs_value" -> ({
        DiagramScaffold(
            contentDescription = stringResource(id = R.string.tutorial_diagram_content_description_growth_vs_value),
            illustrativeNote = IllustrativeNote.SCHEMATIC,
            size = DiagramSize.DEFAULT,
            caption = stringResource(id = R.string.diagram_caption_growth_vs_value)
        ) { GrowthVsValueSpectrum(modifier = Modifier.fillMaxSize()) }
    })
    "valuation_basics" -> ({
        DiagramScaffold(
            contentDescription = stringResource(id = R.string.tutorial_diagram_content_description_valuation_basics),
            illustrativeNote = IllustrativeNote.SCHEMATIC,
            size = DiagramSize.DEFAULT,
            caption = stringResource(id = R.string.diagram_caption_valuation_basics)
        ) { ValuationBasicsSpectrum(modifier = Modifier.fillMaxSize()) }
    })
    "pe_expansion" -> ({
        DiagramScaffold(
            contentDescription = stringResource(id = R.string.tutorial_diagram_content_description_pe_expansion),
            illustrativeNote = IllustrativeNote.SCHEMATIC,
            size = DiagramSize.DEFAULT,
            caption = stringResource(id = R.string.diagram_caption_pe_expansion)
        ) { PeExpansionBar(modifier = Modifier.fillMaxSize()) }
    })
    "pe_contraction" -> ({
        DiagramScaffold(
            contentDescription = stringResource(id = R.string.tutorial_diagram_content_description_pe_contraction),
            illustrativeNote = IllustrativeNote.SCHEMATIC,
            size = DiagramSize.DEFAULT,
            caption = stringResource(id = R.string.diagram_caption_pe_contraction)
        ) { PeContractionBar(modifier = Modifier.fillMaxSize()) }
    })
    "breadth" -> ({
        DiagramScaffold(
            contentDescription = stringResource(id = R.string.tutorial_diagram_content_description_breadth),
            illustrativeNote = IllustrativeNote.SCHEMATIC,
            size = DiagramSize.DEFAULT,
            caption = stringResource(id = R.string.diagram_caption_breadth)
        ) { BreadthDivergenceChart(modifier = Modifier.fillMaxSize()) }
    })
    "relative_strength" -> ({
        DiagramScaffold(
            contentDescription = stringResource(id = R.string.tutorial_diagram_content_description_relative_strength),
            illustrativeNote = IllustrativeNote.SCHEMATIC,
            size = DiagramSize.TALL,
            caption = stringResource(id = R.string.diagram_caption_relative_strength)
        ) { RelativeStrengthDivergenceChart(modifier = Modifier.fillMaxSize()) }
    })
    "market_phases" -> ({
        DiagramScaffold(
            contentDescription = stringResource(id = R.string.tutorial_diagram_content_description_market_phases),
            illustrativeNote = IllustrativeNote.SCHEMATIC,
            size = DiagramSize.TALL,
            caption = stringResource(id = R.string.diagram_caption_market_phases)
        ) { MarketPhasesWheel(modifier = Modifier.fillMaxSize()) }
    })
    "sector_rotation" -> ({
        DiagramScaffold(
            contentDescription = stringResource(id = R.string.tutorial_diagram_content_description_sector_rotation),
            illustrativeNote = IllustrativeNote.SCHEMATIC,
            size = DiagramSize.TALL,
            caption = stringResource(id = R.string.diagram_caption_sector_rotation)
        ) { SectorRotationWheel(modifier = Modifier.fillMaxSize()) }
    })
    "macro_vitals" -> ({
        DiagramScaffold(
            contentDescription = stringResource(id = R.string.tutorial_diagram_content_description_macro_vitals),
            illustrativeNote = IllustrativeNote.SCHEMATIC,
            size = DiagramSize.DEFAULT,
            caption = stringResource(id = R.string.diagram_caption_macro_vitals)
        ) { MacroVitalsFlow(modifier = Modifier.fillMaxSize()) }
    })
    "stock_analysis" -> ({
        DiagramScaffold(
            contentDescription = stringResource(id = R.string.tutorial_diagram_content_description_stock_analysis),
            illustrativeNote = IllustrativeNote.SCHEMATIC,
            size = DiagramSize.DEFAULT,
            caption = stringResource(id = R.string.diagram_caption_stock_analysis)
        ) { StockAnalysisFlow(modifier = Modifier.fillMaxSize()) }
    })
    "role_of_liquidity" -> ({
        DiagramScaffold(
            contentDescription = stringResource(id = R.string.tutorial_diagram_content_description_role_of_liquidity),
            illustrativeNote = IllustrativeNote.SCHEMATIC,
            size = DiagramSize.TALL,
            caption = stringResource(id = R.string.diagram_caption_role_of_liquidity)
        ) { RoleOfLiquidityWaterfall(modifier = Modifier.fillMaxSize()) }
    })
    "positioning" -> ({
        DiagramScaffold(
            contentDescription = stringResource(id = R.string.tutorial_diagram_content_description_positioning_cadence),
            illustrativeNote = IllustrativeNote.SCHEMATIC,
            size = DiagramSize.DEFAULT,
            caption = stringResource(id = R.string.diagram_caption_positioning_cadence)
        ) { PositioningCadenceStrip(modifier = Modifier.fillMaxSize()) }
    })
    "correlation_breakdown" -> ({
        DiagramScaffold(
            contentDescription = stringResource(id = R.string.tutorial_diagram_content_description_correlation_breakdown),
            illustrativeNote = IllustrativeNote.SCHEMATIC,
            size = DiagramSize.TALL,
            caption = stringResource(id = R.string.diagram_caption_correlation_breakdown)
        ) { CorrelationBreakdownPair(modifier = Modifier.fillMaxSize()) }
    })
    else -> null
}

// ============================================================================
// 🎨 PREVIEWS
// ============================================================================

@Preview(name = "D1 Yield Curve", showBackground = true)
@Composable
private fun PreviewYieldCurveDiagram() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        YieldCurveDiagram(modifier = Modifier.padding(16.dp))
    }
}

@Preview(name = "D2 SMA Extension", showBackground = true)
@Composable
private fun PreviewSmaExtensionDiagram() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        SmaExtensionDiagram(modifier = Modifier.padding(16.dp))
    }
}

@Preview(name = "D3 Sentiment Gauges", showBackground = true)
@Composable
private fun PreviewSentimentGaugesDiagram() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        SentimentGaugesDiagram(modifier = Modifier.padding(16.dp))
    }
}

@Preview(name = "D4 Gauge Anatomy", showBackground = true)
@Composable
private fun PreviewGaugeAnatomyDiagram() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        GaugeAnatomyDiagram(modifier = Modifier.padding(16.dp))
    }
}
