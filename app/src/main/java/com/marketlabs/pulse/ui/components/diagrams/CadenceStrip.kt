package com.marketlabs.pulse.ui.components.diagrams

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.marketlabs.pulse.R
import com.marketlabs.pulse.ui.theme.MarketPulseTheme

/** One row of a [CadenceStrip] -- [measuredFraction]/[publishedFraction] are 0f-1f positions on the
 * strip's SHARED time scale, so every source's lag length is directly comparable at a glance. */
data class CadenceSource(val label: String, val measuredFraction: Float, val publishedFraction: Float)

/**
 * Stacked horizontal tracks comparing how long each data source lags between when a value is
 * measured and when it's actually published -- P7 in the Learn diagram library. No emphasis: the
 * point is a side-by-side comparison, not calling out one source as more important.
 */
@Composable
fun CadenceStrip(sources: List<CadenceSource>, modifier: Modifier = Modifier) {
    val colors = rememberDiagramColors()
    val dotRadius = dimensionResource(id = R.dimen.diagram_dot_radius_emphasis)
    val strokeWidth = dimensionResource(id = R.dimen.diagram_stroke_secondary)

    Column(modifier = modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CadenceLegendDot(filled = false, colors = colors)
                Text(
                    text = stringResource(id = R.string.diagram_cadence_legend_measured),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.inkMuted,
                    modifier = Modifier.padding(start = 4.dp, end = 12.dp)
                )
                CadenceLegendDot(filled = true, colors = colors)
                Text(
                    text = stringResource(id = R.string.diagram_cadence_legend_published),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.inkMuted,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
            Text(
                text = stringResource(id = R.string.diagram_cadence_legend_time),
                style = MaterialTheme.typography.labelSmall,
                color = colors.inkMuted
            )
        }

        Column(modifier = Modifier.fillMaxWidth().weight(1f), verticalArrangement = Arrangement.SpaceEvenly) {
            sources.forEach { source ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(text = source.label, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = colors.ink)
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(dimensionResource(id = R.dimen.diagram_cadence_track_height))
                    ) {
                        val trackY = size.height / 2f
                        drawLine(colors.rule, Offset(0f, trackY), Offset(size.width, trackY), strokeWidth.toPx())

                        val measuredX = size.width * source.measuredFraction
                        val publishedX = size.width * source.publishedFraction
                        drawRect(
                            color = colors.band,
                            topLeft = Offset(measuredX, trackY - dotRadius.toPx()),
                            size = Size(publishedX - measuredX, dotRadius.toPx() * 2)
                        )

                        drawCircle(color = colors.ink, radius = dotRadius.toPx() * 0.6f, center = Offset(measuredX, trackY), style = Stroke(width = strokeWidth.toPx() * 1.5f))
                        drawCircle(color = colors.ink, radius = dotRadius.toPx() * 0.6f, center = Offset(publishedX, trackY))
                    }
                }
            }
        }
    }
}

@Composable
private fun CadenceLegendDot(filled: Boolean, colors: DiagramColors) {
    Canvas(modifier = Modifier.height(10.dp)) {
        if (filled) {
            drawCircle(color = colors.ink, radius = 4.dp.toPx())
        } else {
            drawCircle(color = colors.ink, radius = 4.dp.toPx(), style = Stroke(width = 1.5.dp.toPx()))
        }
    }
}

// ============================================================================
// Presets
// ============================================================================

@Composable
fun PositioningCadenceStrip(modifier: Modifier = Modifier) {
    CadenceStrip(
        sources = listOf(
            CadenceSource(stringResource(id = R.string.diagram_cadence_aaii), measuredFraction = 0.45f, publishedFraction = 0.55f),
            CadenceSource(stringResource(id = R.string.diagram_cadence_cot), measuredFraction = 0.45f, publishedFraction = 0.70f),
            CadenceSource(stringResource(id = R.string.diagram_cadence_short_interest), measuredFraction = 0.45f, publishedFraction = 0.90f)
        ),
        modifier = modifier
    )
}

// ============================================================================
// 🎨 PREVIEWS
// ============================================================================

@Preview(name = "Positioning Cadence", showBackground = true)
@Composable
private fun PreviewPositioningCadenceStrip() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        PositioningCadenceStrip(modifier = Modifier.fillMaxWidth().height(dimensionResource(id = R.dimen.tutorial_diagram_height)))
    }
}
