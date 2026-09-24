package com.marketlabs.pulse.ui.components.tutorials

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.marketlabs.pulse.R
import com.marketlabs.pulse.ui.theme.LocalPulseColors
import com.marketlabs.pulse.ui.theme.MarketPulseTheme

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
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))
        Text(
            text = stringResource(id = R.string.tutorial_diagram_yield_curve_caption),
            style = MaterialTheme.typography.labelSmall,
            color = pulseColors.onSurfaceMuted
        )
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
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))
        Text(
            text = stringResource(id = R.string.tutorial_diagram_sma_caption),
            style = MaterialTheme.typography.labelSmall,
            color = pulseColors.onSurfaceMuted
        )
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
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))
        Text(
            text = stringResource(id = R.string.tutorial_diagram_sentiment_caption),
            style = MaterialTheme.typography.labelSmall,
            color = pulseColors.onSurfaceMuted
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
// ============================================================================

@Composable
fun GaugeAnatomyDiagram(modifier: Modifier = Modifier) {
    val pulseColors = LocalPulseColors.current
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnatomyChip(
            text = stringResource(id = R.string.tutorial_diagram_gauge_anatomy_raw_value_label),
            background = pulseColors.surfaceTinted,
            contentColor = MaterialTheme.colorScheme.onBackground
        )
        AnatomyArrow()
        AnatomyChip(
            text = stringResource(id = R.string.tutorial_diagram_gauge_anatomy_band_label),
            background = pulseColors.accentSurface,
            contentColor = pulseColors.accentPrimary
        )
        AnatomyArrow()
        AnatomyChip(
            text = stringResource(id = R.string.tutorial_diagram_gauge_anatomy_color_label),
            background = pulseColors.signalWarningPill,
            contentColor = pulseColors.signalWarningText
        )
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))
        Text(
            text = stringResource(id = R.string.tutorial_diagram_gauge_anatomy_meaning_label),
            style = MaterialTheme.typography.labelSmall,
            color = pulseColors.onSurfaceMuted,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AnatomyChip(text: String, background: Color, contentColor: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_pill)))
            .background(color = background)
            .padding(
                horizontal = dimensionResource(id = R.dimen.padding_large),
                vertical = dimensionResource(id = R.dimen.padding_small)
            )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = contentColor
        )
    }
}

@Composable
private fun AnatomyArrow() {
    val color = LocalPulseColors.current.onSurfaceMuted
    Canvas(modifier = Modifier.height(24.dp).width(2.dp)) {
        drawLine(
            color = color,
            start = Offset(size.width / 2, 0f),
            end = Offset(size.width / 2, size.height),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

/** Maps a `learn_content.json` diagram key to the composable it names -- `null` (no key, or an
 * unrecognized one) means the card carries no diagram. */
fun learnDiagramFor(key: String?): (@Composable () -> Unit)? = when (key) {
    "sma_extension" -> ({ SmaExtensionDiagram() })
    "yield_curve" -> ({ YieldCurveDiagram() })
    "sentiment_gauges" -> ({ SentimentGaugesDiagram() })
    "gauge_anatomy" -> ({ GaugeAnatomyDiagram() })
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
