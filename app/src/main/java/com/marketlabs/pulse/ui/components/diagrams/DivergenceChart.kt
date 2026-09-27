package com.marketlabs.pulse.ui.components.diagrams

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.marketlabs.pulse.R
import com.marketlabs.pulse.ui.theme.MarketPulseTheme

/** End labels sit right-anchored past where the series stop -- capping their wrap width to roughly
 * the remaining plot width keeps a long label from running back over the curves themselves at a
 * narrow screen width, wrapping onto a second line instead. */
private const val END_LABEL_WIDTH_FRACTION = 0.45f
private val MIN_END_LABEL_WIDTH = 56.dp

/** How far across the plot both series run before stopping -- neither line reaches the right
 * edge, so nothing here reads as "and then price does X next." */
private const val SERIES_END_FRACTION = 0.66f

/**
 * Two lines that start together and pull apart -- P3 in the Learn diagram library. [GAP] shades the
 * widening space between them and brackets it at the end; [STACKED] instead splits into two panels,
 * a price comparison on top and the ratio between them (the actual point of a relative-strength
 * read) emphasized below.
 */
enum class DivergenceStyle { GAP, STACKED }

@Composable
fun DivergenceChart(
    primaryLabel: String,
    secondaryLabel: String,
    style: DivergenceStyle,
    gapLabel: String? = null,
    modifier: Modifier = Modifier
) {
    when (style) {
        DivergenceStyle.GAP -> GapDivergenceChart(primaryLabel, secondaryLabel, gapLabel.orEmpty(), modifier)
        DivergenceStyle.STACKED -> StackedDivergenceChart(primaryLabel, secondaryLabel, modifier)
    }
}

@Composable
private fun GapDivergenceChart(primaryLabel: String, secondaryLabel: String, gapLabel: String, modifier: Modifier = Modifier) {
    val colors = rememberDiagramColors()
    val primaryStroke = dimensionResource(id = R.dimen.diagram_stroke_primary)
    val bracketStroke = dimensionResource(id = R.dimen.diagram_stroke_secondary)

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val endLabelMaxWidth = (maxWidth * END_LABEL_WIDTH_FRACTION).coerceAtLeast(MIN_END_LABEL_WIDTH)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width * SERIES_END_FRACTION
            val h = size.height

            val primaryP0 = Offset(0f, h * 0.75f)
            val primaryC1 = Offset(w * 0.35f, h * 0.62f)
            val primaryC2 = Offset(w * 0.68f, h * 0.30f)
            val primaryP3 = Offset(w, h * 0.20f)

            val secondaryP0 = Offset(0f, h * 0.78f)
            val secondaryC1 = Offset(w * 0.35f, h * 0.68f)
            val secondaryC2 = Offset(w * 0.68f, h * 0.60f)
            val secondaryP3 = Offset(w, h * 0.55f)

            // Gap fill: the region where the two curves have visibly separated, not their full run.
            val fillStartT = 0.4f
            val steps = 10
            val fillPath = Path().apply {
                for (i in 0..steps) {
                    val t = fillStartT + (1f - fillStartT) * (i / steps.toFloat())
                    val p = cubicBezierPoint(t, primaryP0, primaryC1, primaryC2, primaryP3)
                    if (i == 0) moveTo(p.x, p.y) else lineTo(p.x, p.y)
                }
                for (i in steps downTo 0) {
                    val t = fillStartT + (1f - fillStartT) * (i / steps.toFloat())
                    val p = cubicBezierPoint(t, secondaryP0, secondaryC1, secondaryC2, secondaryP3)
                    lineTo(p.x, p.y)
                }
                close()
            }
            drawPath(path = fillPath, color = colors.band)

            val primaryPath = Path().apply {
                moveTo(primaryP0.x, primaryP0.y)
                cubicTo(primaryC1.x, primaryC1.y, primaryC2.x, primaryC2.y, primaryP3.x, primaryP3.y)
            }
            val secondaryPath = Path().apply {
                moveTo(secondaryP0.x, secondaryP0.y)
                cubicTo(secondaryC1.x, secondaryC1.y, secondaryC2.x, secondaryC2.y, secondaryP3.x, secondaryP3.y)
            }
            drawPath(path = primaryPath, color = colors.ink, style = Stroke(width = primaryStroke.toPx(), cap = StrokeCap.Round))
            drawPath(path = secondaryPath, color = colors.inkMuted, style = Stroke(width = primaryStroke.toPx(), cap = StrokeCap.Round))

            // Emphasis bracket calling out the gap at the point it's widest (the end).
            val tickLength = 6.dp.toPx()
            drawLine(colors.emphasis, Offset(w, primaryP3.y), Offset(w, secondaryP3.y), bracketStroke.toPx())
            drawLine(colors.emphasis, Offset(w - tickLength, primaryP3.y), Offset(w, primaryP3.y), bracketStroke.toPx())
            drawLine(colors.emphasis, Offset(w - tickLength, secondaryP3.y), Offset(w, secondaryP3.y), bracketStroke.toPx())
        }

        Text(
            text = primaryLabel,
            style = MaterialTheme.typography.labelSmall,
            color = colors.ink,
            textAlign = TextAlign.End,
            maxLines = 2,
            modifier = Modifier
                .align(BiasAlignment(horizontalBias = 1f, verticalBias = -0.6f))
                .widthIn(max = endLabelMaxWidth)
        )
        Text(
            text = secondaryLabel,
            style = MaterialTheme.typography.labelSmall,
            color = colors.inkMuted,
            textAlign = TextAlign.End,
            maxLines = 2,
            modifier = Modifier
                .align(BiasAlignment(horizontalBias = 1f, verticalBias = 0.1f))
                .widthIn(max = endLabelMaxWidth)
        )
        if (gapLabel.isNotEmpty()) {
            Text(
                text = gapLabel,
                style = MaterialTheme.typography.labelSmall,
                color = colors.emphasis,
                maxLines = 1,
                modifier = Modifier
                    .align(BiasAlignment(horizontalBias = 0.72f, verticalBias = -0.25f))
                    .widthIn(max = MIN_END_LABEL_WIDTH)
            )
        }
    }
}

@Composable
private fun StackedDivergenceChart(primaryLabel: String, secondaryLabel: String, modifier: Modifier = Modifier) {
    val colors = rememberDiagramColors()
    val primaryStroke = dimensionResource(id = R.dimen.diagram_stroke_primary)
    val emphasisStroke = dimensionResource(id = R.dimen.diagram_stroke_emphasis)

    Column(modifier = modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width * SERIES_END_FRACTION
                val h = size.height

                val primaryPath = Path().apply {
                    moveTo(0f, h * 0.55f)
                    cubicTo(w * 0.3f, h * 0.35f, w * 0.55f, h * 0.5f, w, h * 0.20f)
                }
                val secondaryPath = Path().apply {
                    moveTo(0f, h * 0.60f)
                    cubicTo(w * 0.3f, h * 0.50f, w * 0.55f, h * 0.55f, w, h * 0.48f)
                }
                drawPath(path = secondaryPath, color = colors.inkMuted, style = Stroke(width = primaryStroke.toPx(), cap = StrokeCap.Round))
                drawPath(path = primaryPath, color = colors.ink, style = Stroke(width = primaryStroke.toPx(), cap = StrokeCap.Round))
            }
            Text(
                text = primaryLabel,
                style = MaterialTheme.typography.labelSmall,
                color = colors.ink,
                modifier = Modifier.align(BiasAlignment(horizontalBias = 1f, verticalBias = -0.8f))
            )
            Text(
                text = secondaryLabel,
                style = MaterialTheme.typography.labelSmall,
                color = colors.inkMuted,
                modifier = Modifier.align(BiasAlignment(horizontalBias = 1f, verticalBias = 0.5f))
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensionResource(id = R.dimen.diagram_stroke_secondary))
                .background(colors.rule)
        )

        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width * SERIES_END_FRACTION
                val h = size.height
                val ratioPath = Path().apply {
                    moveTo(0f, h * 0.35f)
                    cubicTo(w * 0.35f, h * 0.60f, w * 0.6f, h * 0.75f, w, h * 0.25f)
                }
                drawPath(path = ratioPath, color = colors.emphasis, style = Stroke(width = emphasisStroke.toPx(), cap = StrokeCap.Round))
            }
        }
    }
}

// ============================================================================
// Presets
// ============================================================================

@Composable
fun BreadthDivergenceChart(modifier: Modifier = Modifier) {
    DivergenceChart(
        primaryLabel = stringResource(id = R.string.diagram_divergence_breadth_primary),
        secondaryLabel = stringResource(id = R.string.diagram_divergence_breadth_secondary),
        style = DivergenceStyle.GAP,
        gapLabel = stringResource(id = R.string.diagram_divergence_breadth_gap),
        modifier = modifier
    )
}

@Composable
fun RelativeStrengthDivergenceChart(modifier: Modifier = Modifier) {
    DivergenceChart(
        primaryLabel = stringResource(id = R.string.diagram_divergence_relative_strength_primary),
        secondaryLabel = stringResource(id = R.string.diagram_divergence_relative_strength_secondary),
        style = DivergenceStyle.STACKED,
        modifier = modifier
    )
}

// ============================================================================
// 🎨 PREVIEWS
// ============================================================================

@Preview(name = "Breadth Divergence", showBackground = true)
@Composable
private fun PreviewBreadthDivergenceChart() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        BreadthDivergenceChart(modifier = Modifier.height(dimensionResource(id = R.dimen.tutorial_diagram_height)))
    }
}

@Preview(name = "Relative Strength", showBackground = true)
@Composable
private fun PreviewRelativeStrengthDivergenceChart() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        RelativeStrengthDivergenceChart(modifier = Modifier.height(dimensionResource(id = R.dimen.tutorial_diagram_height_tall)))
    }
}
