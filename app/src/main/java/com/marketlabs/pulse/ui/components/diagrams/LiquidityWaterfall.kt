package com.marketlabs.pulse.ui.components.diagrams

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.marketlabs.pulse.R
import com.marketlabs.pulse.ui.theme.MarketPulseTheme
import kotlin.math.max
import kotlin.math.min

/** One column of a [LiquidityWaterfall] -- [contribution] is a signed fraction of the chart's
 * height, [signLabel] is the small glyph drawn above the bar (e.g. "+", "≈", "−", "="). */
data class LiquidityStep(val name: String, val contribution: Float, val signLabel: String)

/**
 * A running-total waterfall: each column's bar floats from the previous column's cumulative level
 * to the new one, connected by a dashed carry line -- P6v in the Learn diagram library. The last
 * [LiquidityStep] is always the net total, drawn from the zero baseline in [DiagramColors.emphasis]
 * rather than floating like the rest. No arrow or label points at an outcome; the caption is what
 * frames this as backdrop, not forecast.
 */
@Composable
fun LiquidityWaterfall(steps: List<LiquidityStep>, modifier: Modifier = Modifier) {
    val colors = rememberDiagramColors()
    val cornerRadius = dimensionResource(id = R.dimen.diagram_bar_corner_radius)

    Column(modifier = modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
            var cumulative = 0f
            val levels = steps.dropLast(1).map { step ->
                val start = cumulative
                cumulative += step.contribution
                start to cumulative
            }

            steps.forEachIndexed { index, step ->
                val isNet = index == steps.lastIndex
                val (levelStart, levelEnd) = if (isNet) 0f to cumulative else levels[index]
                Box(modifier = Modifier.weight(1f).fillMaxSize(), contentAlignment = Alignment.Center) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val baselineY = size.height * 0.75f
                        val scale = size.height * 0.4f
                        val barWidth = size.width * 0.45f
                        val left = (size.width - barWidth) / 2f
                        val topY = baselineY - max(levelStart, levelEnd) * scale
                        val bottomY = baselineY - min(levelStart, levelEnd) * scale
                        val cornerPx = cornerRadius.toPx()

                        drawRoundRect(
                            color = if (isNet) colors.emphasis else colors.fill,
                            topLeft = Offset(left, topY),
                            size = Size(barWidth, (bottomY - topY).coerceAtLeast(2.dp.toPx())),
                            cornerRadius = CornerRadius(cornerPx, cornerPx)
                        )

                        if (!isNet && index < steps.lastIndex - 1) {
                            val carryY = baselineY - levelEnd * scale
                            drawLine(
                                color = colors.inkMuted,
                                start = Offset(left + barWidth, carryY),
                                end = Offset(size.width, carryY),
                                strokeWidth = 1.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(9f, 9f))
                            )
                        }
                    }
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            steps.forEach { step ->
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = step.signLabel, style = MaterialTheme.typography.labelSmall, color = colors.inkMuted, textAlign = TextAlign.Center)
                    Text(
                        text = step.name,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = colors.ink,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

// ============================================================================
// Presets
// ============================================================================

@Composable
fun RoleOfLiquidityWaterfall(modifier: Modifier = Modifier) {
    LiquidityWaterfall(
        steps = listOf(
            LiquidityStep(stringResource(id = R.string.diagram_waterfall_rates), contribution = 0.55f, signLabel = "+"),
            LiquidityStep(stringResource(id = R.string.diagram_waterfall_credit), contribution = 0.10f, signLabel = "≈"),
            LiquidityStep(stringResource(id = R.string.diagram_waterfall_risk_appetite), contribution = -0.25f, signLabel = "−"),
            LiquidityStep(stringResource(id = R.string.diagram_waterfall_net), contribution = 0f, signLabel = "=")
        ),
        modifier = modifier
    )
}

// ============================================================================
// 🎨 PREVIEWS
// ============================================================================

@Preview(name = "Role of Liquidity", showBackground = true)
@Composable
private fun PreviewRoleOfLiquidityWaterfall() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        RoleOfLiquidityWaterfall(modifier = Modifier.fillMaxWidth().height(dimensionResource(id = R.dimen.tutorial_diagram_height_tall)))
    }
}
