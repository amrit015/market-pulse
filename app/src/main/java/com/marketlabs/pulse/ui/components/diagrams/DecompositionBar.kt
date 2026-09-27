package com.marketlabs.pulse.ui.components.diagrams

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.marketlabs.pulse.R
import com.marketlabs.pulse.ui.theme.MarketPulseTheme
import kotlin.math.abs
import kotlin.math.max

/** One bar of a [DecompositionBar] -- [magnitude] is a signed fraction of the tallest bar's height
 * (0f reads as a flat "≈ flat" stub, never an empty bar), [changeLabel] is the two-line label's
 * second line (e.g. "+18%"), and [isTotal] marks the single summed bar drawn in emphasis. */
data class DecompositionBarItem(
    val name: String,
    val magnitude: Float,
    val changeLabel: String,
    val isTotal: Boolean = false
)

/**
 * A "component A (+) component B (=) total" bar chart -- O2 in the Learn diagram library. Bars
 * grow/shrink from a shared mid-height baseline so a negative component reads as a downward bar,
 * not a shorter one; [DecompositionBarItem.isTotal] is the one bar drawn in
 * [DiagramColors.emphasis], the rest in [DiagramColors.fill].
 */
@Composable
fun DecompositionBar(items: List<DecompositionBarItem>, modifier: Modifier = Modifier) {
    val colors = rememberDiagramColors()
    val cornerRadius = dimensionResource(id = R.dimen.diagram_bar_corner_radius)
    val strokeWidth = dimensionResource(id = R.dimen.diagram_stroke_secondary)

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            items.forEachIndexed { index, item ->
                Row(modifier = Modifier.weight(1f).fillMaxSize(), horizontalArrangement = Arrangement.Center) {
                    DecompositionBarColumn(
                        item = item,
                        color = if (item.isTotal) colors.emphasis else colors.fill,
                        edgeColor = colors.edge,
                        cornerRadiusDp = cornerRadius,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (index != items.lastIndex) {
                        OperatorDisc(
                            symbol = if (items.getOrNull(index + 1)?.isTotal == true) "=" else "+",
                            colors = colors,
                            strokeWidth = strokeWidth
                        )
                    }
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            items.forEach { item ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = item.name, style = MaterialTheme.typography.labelSmall, color = colors.ink, textAlign = TextAlign.Center)
                    Text(text = item.changeLabel, style = MaterialTheme.typography.labelSmall, color = colors.inkMuted, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

@Composable
private fun DecompositionBarColumn(
    item: DecompositionBarItem,
    color: Color,
    edgeColor: Color,
    cornerRadiusDp: Dp,
    modifier: Modifier = Modifier
) {
    val isFlat = abs(item.magnitude) < 0.05f
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val baselineY = size.height * 0.54f
            val barWidth = size.width * 0.5f
            val left = (size.width - barWidth) / 2f
            val cornerPx = cornerRadiusDp.toPx()
            val edgeStrokePx = 1.dp.toPx()

            if (isFlat) {
                val stubHeight = size.height * 0.04f
                drawRoundRect(
                    color = color,
                    topLeft = Offset(left, baselineY - stubHeight / 2f),
                    size = Size(barWidth, stubHeight),
                    cornerRadius = CornerRadius(cornerPx, cornerPx),
                    style = Stroke(width = edgeStrokePx)
                )
            } else {
                val barHeight = max(size.height * 0.4f * abs(item.magnitude).coerceAtMost(1f), size.height * 0.06f)
                val top = if (item.magnitude > 0) baselineY - barHeight else baselineY
                drawRoundRect(
                    color = color,
                    topLeft = Offset(left, top),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(cornerPx, cornerPx)
                )
                drawRoundRect(
                    color = edgeColor,
                    topLeft = Offset(left, top),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(cornerPx, cornerPx),
                    style = Stroke(width = edgeStrokePx)
                )
            }
        }
    }
}

@Composable
private fun OperatorDisc(symbol: String, colors: DiagramColors, strokeWidth: Dp) {
    Box(
        modifier = Modifier
            .size(dimensionResource(id = R.dimen.diagram_operator_disc_size))
            .background(color = colors.card, shape = CircleShape)
            .border(width = strokeWidth, color = colors.rule, shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(text = symbol, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = colors.ink)
    }
}

// ============================================================================
// Presets
// ============================================================================

@Composable
fun PeExpansionBar(modifier: Modifier = Modifier) {
    DecompositionBar(
        items = listOf(
            DecompositionBarItem(
                name = stringResource(id = R.string.diagram_decomposition_earnings_label),
                magnitude = 0f,
                changeLabel = stringResource(id = R.string.diagram_decomposition_flat_label)
            ),
            DecompositionBarItem(
                name = stringResource(id = R.string.diagram_decomposition_multiple_label),
                magnitude = 0.7f,
                changeLabel = "+18%"
            ),
            DecompositionBarItem(
                name = stringResource(id = R.string.diagram_decomposition_price_label),
                magnitude = 0.7f,
                changeLabel = "+18%",
                isTotal = true
            )
        ),
        modifier = modifier
    )
}

@Composable
fun PeContractionBar(modifier: Modifier = Modifier) {
    DecompositionBar(
        items = listOf(
            DecompositionBarItem(
                name = stringResource(id = R.string.diagram_decomposition_earnings_label),
                magnitude = 0.15f,
                changeLabel = "+3%"
            ),
            DecompositionBarItem(
                name = stringResource(id = R.string.diagram_decomposition_multiple_label),
                magnitude = -0.9f,
                changeLabel = "−15%"
            ),
            DecompositionBarItem(
                name = stringResource(id = R.string.diagram_decomposition_price_label),
                magnitude = -0.65f,
                changeLabel = "−12%",
                isTotal = true
            )
        ),
        modifier = modifier
    )
}

// ============================================================================
// 🎨 PREVIEWS
// ============================================================================

@Preview(name = "P/E Expansion", showBackground = true)
@Composable
private fun PreviewPeExpansionBar() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        PeExpansionBar(modifier = Modifier.height(dimensionResource(id = R.dimen.tutorial_diagram_height)))
    }
}

@Preview(name = "P/E Contraction", showBackground = true)
@Composable
private fun PreviewPeContractionBar() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        PeContractionBar(modifier = Modifier.height(dimensionResource(id = R.dimen.tutorial_diagram_height)))
    }
}
