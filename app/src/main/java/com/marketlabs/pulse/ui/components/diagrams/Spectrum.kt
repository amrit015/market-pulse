package com.marketlabs.pulse.ui.components.diagrams

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.marketlabs.pulse.R
import com.marketlabs.pulse.ui.theme.MarketPulseTheme

/** A label never shrinks its wrap width below this, even when its neighbors are packed tight --
 * better to risk a little visual crowding at an extreme than compress text into an unreadable sliver. */
private val MIN_SPECTRUM_LABEL_WIDTH = 56.dp

/** One marked point on a [Spectrum]'s axis. [position] is a 0f (start) - 1f (end) fraction of the
 * axis, not a pixel value, so the preset scales to any screen width. [isAbove] alternates a
 * spectrum's items between the row above and below the axis so adjacent labels never collide. */
data class SpectrumItem(
    val label: String,
    val position: Float,
    val isAbove: Boolean,
    val emphasized: Boolean = false
)

/**
 * A labeled axis with open-chevron ends and a handful of marked points along it -- P5 in the Learn
 * diagram library. No signal colors: an [SpectrumItem.emphasized] point is called out by size and
 * [DiagramColors.emphasis] alone, never bullish/bearish hue.
 */
@Composable
fun Spectrum(startLabel: String, endLabel: String, items: List<SpectrumItem>, modifier: Modifier = Modifier) {
    val colors = rememberDiagramColors()
    val insetDp = dimensionResource(id = R.dimen.diagram_axis_inset)
    val dotRadius = dimensionResource(id = R.dimen.diagram_dot_radius)
    val dotRadiusEmphasis = dimensionResource(id = R.dimen.diagram_dot_radius_emphasis)
    val chevronSize = dimensionResource(id = R.dimen.diagram_chevron_size)
    val tickLength = dimensionResource(id = R.dimen.diagram_tick_length)
    val strokeWidth = dimensionResource(id = R.dimen.diagram_stroke_secondary)

    Column(modifier = modifier.fillMaxSize()) {
        // Labels for items above the axis sit in their own row, bottom-anchored so they hug the
        // axis strip regardless of how many lines a given label wraps to.
        SpectrumLabelRow(items = items, isAboveRow = true, insetDp = insetDp, colors = colors, verticalBias = 1f)

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensionResource(id = R.dimen.diagram_axis_strip_height))
        ) {
            val insetPx = insetDp.toPx()
            val axisY = size.height / 2f
            val axisStart = insetPx
            val axisEnd = size.width - insetPx
            val tickPx = tickLength.toPx()
            val strokePx = strokeWidth.toPx()

            drawLine(
                color = colors.rule,
                start = Offset(axisStart, axisY),
                end = Offset(axisEnd, axisY),
                strokeWidth = strokePx
            )
            drawOpenChevron(color = colors.inkMuted, center = Offset(axisStart, axisY), pointingAngleDegrees = 180f, size = chevronSize.toPx(), strokeWidth = strokePx)
            drawOpenChevron(color = colors.inkMuted, center = Offset(axisEnd, axisY), pointingAngleDegrees = 0f, size = chevronSize.toPx(), strokeWidth = strokePx)

            items.forEach { item ->
                val x = axisStart + (axisEnd - axisStart) * item.position
                val tickEndY = if (item.isAbove) axisY - tickPx else axisY + tickPx
                drawLine(color = colors.inkMuted, start = Offset(x, axisY), end = Offset(x, tickEndY), strokeWidth = strokePx)
                drawCircle(
                    color = if (item.emphasized) colors.emphasis else colors.ink,
                    radius = if (item.emphasized) dotRadiusEmphasis.toPx() else dotRadius.toPx(),
                    center = Offset(x, axisY)
                )
            }
        }

        SpectrumLabelRow(items = items, isAboveRow = false, insetDp = insetDp, colors = colors, verticalBias = -1f)

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = insetDp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "← $startLabel", style = MaterialTheme.typography.labelSmall, color = colors.inkMuted)
            Text(text = "$endLabel →", style = MaterialTheme.typography.labelSmall, color = colors.inkMuted)
        }
    }
}

/**
 * One row (above or below the axis) of item labels. Each label's wrap width is capped at the
 * fraction of the plot it actually has room in -- the gap to its nearest same-row neighbor (or the
 * axis edge), not just its own text length -- so a long label wraps onto a second line instead of
 * overflowing into the next one. Two same-row items placed close together (a crowded preset, or a
 * narrow screen) still can't visually collide; they just wrap sooner.
 */
@Composable
private fun ColumnScope.SpectrumLabelRow(
    items: List<SpectrumItem>,
    isAboveRow: Boolean,
    insetDp: Dp,
    colors: DiagramColors,
    verticalBias: Float
) {
    val rowItems = items.filter { it.isAbove == isAboveRow }.sortedBy { it.position }
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .padding(horizontal = insetDp)
    ) {
        rowItems.forEachIndexed { index, item ->
            val previousPosition = if (index == 0) 0f else rowItems[index - 1].position
            val nextPosition = if (index == rowItems.lastIndex) 1f else rowItems[index + 1].position
            val availableFraction = minOf(item.position - previousPosition, nextPosition - item.position)
            val maxLabelWidth = (maxWidth * availableFraction).coerceAtLeast(MIN_SPECTRUM_LABEL_WIDTH)

            Text(
                text = item.label,
                style = MaterialTheme.typography.labelSmall,
                color = if (item.emphasized) colors.emphasis else colors.inkMuted,
                textAlign = TextAlign.Center,
                maxLines = 2,
                modifier = Modifier
                    .align(BiasAlignment(horizontalBias = item.position * 2f - 1f, verticalBias = verticalBias))
                    .widthIn(max = maxLabelWidth)
            )
        }
    }
}

// ============================================================================
// Presets
// ============================================================================

@Composable
fun RateSensitivitySpectrum(modifier: Modifier = Modifier) {
    Spectrum(
        startLabel = stringResource(id = R.string.diagram_spectrum_rate_sensitivity_start),
        endLabel = stringResource(id = R.string.diagram_spectrum_rate_sensitivity_end),
        items = listOf(
            SpectrumItem(stringResource(id = R.string.diagram_spectrum_rate_sensitivity_item_cash), position = 0.05f, isAbove = true),
            SpectrumItem(stringResource(id = R.string.diagram_spectrum_rate_sensitivity_item_financials), position = 0.30f, isAbove = false),
            SpectrumItem(stringResource(id = R.string.diagram_spectrum_rate_sensitivity_item_dividend), position = 0.55f, isAbove = true),
            SpectrumItem(stringResource(id = R.string.diagram_spectrum_rate_sensitivity_item_bonds), position = 0.78f, isAbove = false),
            SpectrumItem(stringResource(id = R.string.diagram_spectrum_rate_sensitivity_item_growth), position = 0.95f, isAbove = true)
        ),
        modifier = modifier
    )
}

@Composable
fun GrowthVsValueSpectrum(modifier: Modifier = Modifier) {
    Spectrum(
        startLabel = stringResource(id = R.string.diagram_spectrum_growth_value_start),
        endLabel = stringResource(id = R.string.diagram_spectrum_growth_value_end),
        items = listOf(
            SpectrumItem(stringResource(id = R.string.diagram_spectrum_growth_value_item_utilities), position = 0.08f, isAbove = true),
            SpectrumItem(stringResource(id = R.string.diagram_spectrum_growth_value_item_financials), position = 0.28f, isAbove = false),
            SpectrumItem(stringResource(id = R.string.diagram_spectrum_growth_value_item_energy), position = 0.48f, isAbove = true),
            SpectrumItem(stringResource(id = R.string.diagram_spectrum_growth_value_item_discretionary), position = 0.72f, isAbove = false),
            SpectrumItem(stringResource(id = R.string.diagram_spectrum_growth_value_item_tech), position = 0.92f, isAbove = true)
        ),
        modifier = modifier
    )
}

@Composable
fun ValuationBasicsSpectrum(modifier: Modifier = Modifier) {
    Spectrum(
        startLabel = stringResource(id = R.string.diagram_spectrum_valuation_basics_start),
        endLabel = stringResource(id = R.string.diagram_spectrum_valuation_basics_end),
        items = listOf(
            SpectrumItem(stringResource(id = R.string.diagram_spectrum_valuation_basics_item_market), position = 0.25f, isAbove = false),
            SpectrumItem(stringResource(id = R.string.diagram_spectrum_valuation_basics_item_this_stock), position = 0.50f, isAbove = true, emphasized = true),
            SpectrumItem(stringResource(id = R.string.diagram_spectrum_valuation_basics_item_peers), position = 0.80f, isAbove = false)
        ),
        modifier = modifier
    )
}

// ============================================================================
// 🎨 PREVIEWS
// ============================================================================

@Preview(name = "Rate Sensitivity", showBackground = true)
@Composable
private fun PreviewRateSensitivitySpectrum() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        RateSensitivitySpectrum(modifier = Modifier.height(dimensionResource(id = R.dimen.tutorial_diagram_height)))
    }
}

@Preview(name = "Growth vs Value", showBackground = true)
@Composable
private fun PreviewGrowthVsValueSpectrum() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        GrowthVsValueSpectrum(modifier = Modifier.height(dimensionResource(id = R.dimen.tutorial_diagram_height)))
    }
}

@Preview(name = "Valuation Basics", showBackground = true)
@Composable
private fun PreviewValuationBasicsSpectrum() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        ValuationBasicsSpectrum(modifier = Modifier.height(dimensionResource(id = R.dimen.tutorial_diagram_height)))
    }
}
