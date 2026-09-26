package com.marketlabs.pulse.ui.components.diagrams

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.marketlabs.pulse.R
import com.marketlabs.pulse.ui.theme.MarketPulseTheme
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/** One quarter of a [CycleWheel] -- [name] and [signature] both render at the same corner,
 * [name] bold, [signature] muted underneath it. */
data class CyclePhase(val name: String, val signature: String)

private const val SEGMENT_GAP_DEGREES = 8f
private const val SEGMENT_SWEEP_DEGREES = 90f - SEGMENT_GAP_DEGREES

/**
 * A 4-segment ring read clockwise from 12 o'clock -- P4 in the Learn diagram library. The centre
 * stays empty on purpose: this names the recurring stages of a cycle, not a "you are here" marker
 * for the current one. [showOuterRing] adds a second, thinner ring in [DiagramColors.emphasis] for
 * the one preset (sector rotation) where a second layer of meaning rides on top of the same phases.
 */
@Composable
fun CycleWheel(phases: List<CyclePhase>, showOuterRing: Boolean = false, modifier: Modifier = Modifier) {
    require(phases.size == 4) { "CycleWheel always shows exactly 4 phases" }
    val colors = rememberDiagramColors()
    val labelMaxWidth = dimensionResource(id = R.dimen.diagram_wheel_label_max_width)
    val chevronSize = dimensionResource(id = R.dimen.diagram_chevron_size)

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val ringRadius = min(size.width, size.height) * 0.30f
            val ringStroke = ringRadius * (12f / 48f)
            val chevronPx = chevronSize.toPx()

            for (i in 0 until 4) {
                val startAngle = -90f + i * 90f + SEGMENT_GAP_DEGREES / 2f
                drawArc(
                    color = colors.fill,
                    startAngle = startAngle,
                    sweepAngle = SEGMENT_SWEEP_DEGREES,
                    useCenter = false,
                    topLeft = Offset(center.x - ringRadius, center.y - ringRadius),
                    size = Size(ringRadius * 2, ringRadius * 2),
                    style = Stroke(width = ringStroke, cap = StrokeCap.Butt)
                )
                val endAngle = startAngle + SEGMENT_SWEEP_DEGREES
                val endAngleRad = Math.toRadians(endAngle.toDouble())
                val chevronCenter = Offset(
                    center.x + ringRadius * cos(endAngleRad).toFloat(),
                    center.y + ringRadius * sin(endAngleRad).toFloat()
                )
                drawOpenChevron(
                    color = colors.inkMuted,
                    center = chevronCenter,
                    pointingAngleDegrees = endAngle + 90f,
                    size = chevronPx,
                    strokeWidth = ringStroke * 0.25f
                )
            }

            if (showOuterRing) {
                val outerRadius = ringRadius * (56f / 48f)
                val outerStroke = ringRadius * (6f / 48f)
                for (i in 0 until 4) {
                    val startAngle = -90f + i * 90f + SEGMENT_GAP_DEGREES / 2f
                    drawArc(
                        color = colors.emphasis,
                        startAngle = startAngle,
                        sweepAngle = SEGMENT_SWEEP_DEGREES,
                        useCenter = false,
                        topLeft = Offset(center.x - outerRadius, center.y - outerRadius),
                        size = Size(outerRadius * 2, outerRadius * 2),
                        style = Stroke(width = outerStroke, cap = StrokeCap.Butt)
                    )
                }
            }
        }

        WheelCornerLabel(phases[0], colors, Alignment.TopEnd, labelMaxWidth)
        WheelCornerLabel(phases[1], colors, Alignment.BottomEnd, labelMaxWidth)
        WheelCornerLabel(phases[2], colors, Alignment.BottomStart, labelMaxWidth)
        WheelCornerLabel(phases[3], colors, Alignment.TopStart, labelMaxWidth)
    }
}

@Composable
private fun BoxScope.WheelCornerLabel(phase: CyclePhase, colors: DiagramColors, alignment: Alignment, maxWidth: Dp) {
    val textAlign = if (alignment == Alignment.TopEnd || alignment == Alignment.BottomEnd) TextAlign.End else TextAlign.Start
    val horizontalAlignment = if (textAlign == TextAlign.End) Alignment.End else Alignment.Start
    Column(
        modifier = Modifier.align(alignment).widthIn(max = maxWidth),
        horizontalAlignment = horizontalAlignment
    ) {
        Text(
            text = phase.name,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = colors.ink,
            textAlign = textAlign
        )
        Text(
            text = phase.signature,
            style = MaterialTheme.typography.labelSmall,
            color = colors.inkMuted,
            textAlign = textAlign
        )
    }
}

// ============================================================================
// Presets
// ============================================================================

@Composable
fun MarketPhasesWheel(modifier: Modifier = Modifier) {
    CycleWheel(
        phases = listOf(
            CyclePhase(stringResource(id = R.string.diagram_wheel_market_phases_accumulation), stringResource(id = R.string.diagram_wheel_market_phases_accumulation_sig)),
            CyclePhase(stringResource(id = R.string.diagram_wheel_market_phases_markup), stringResource(id = R.string.diagram_wheel_market_phases_markup_sig)),
            CyclePhase(stringResource(id = R.string.diagram_wheel_market_phases_distribution), stringResource(id = R.string.diagram_wheel_market_phases_distribution_sig)),
            CyclePhase(stringResource(id = R.string.diagram_wheel_market_phases_markdown), stringResource(id = R.string.diagram_wheel_market_phases_markdown_sig))
        ),
        modifier = modifier
    )
}

@Composable
fun SectorRotationWheel(modifier: Modifier = Modifier) {
    CycleWheel(
        phases = listOf(
            CyclePhase(stringResource(id = R.string.diagram_wheel_sector_rotation_early), stringResource(id = R.string.diagram_wheel_sector_rotation_early_sig)),
            CyclePhase(stringResource(id = R.string.diagram_wheel_sector_rotation_late), stringResource(id = R.string.diagram_wheel_sector_rotation_late_sig)),
            CyclePhase(stringResource(id = R.string.diagram_wheel_sector_rotation_slowdown), stringResource(id = R.string.diagram_wheel_sector_rotation_slowdown_sig)),
            CyclePhase(stringResource(id = R.string.diagram_wheel_sector_rotation_recession), stringResource(id = R.string.diagram_wheel_sector_rotation_recession_sig))
        ),
        showOuterRing = true,
        modifier = modifier
    )
}

// ============================================================================
// 🎨 PREVIEWS
// ============================================================================

@Preview(name = "Market Phases", showBackground = true)
@Composable
private fun PreviewMarketPhasesWheel() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        MarketPhasesWheel(modifier = Modifier.height(dimensionResource(id = R.dimen.tutorial_diagram_height_tall)))
    }
}

@Preview(name = "Sector Rotation", showBackground = true)
@Composable
private fun PreviewSectorRotationWheel() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        SectorRotationWheel(modifier = Modifier.height(dimensionResource(id = R.dimen.tutorial_diagram_height_tall)))
    }
}
