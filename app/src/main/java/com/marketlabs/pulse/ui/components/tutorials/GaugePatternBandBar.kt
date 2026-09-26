package com.marketlabs.pulse.ui.components.tutorials

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import com.marketlabs.pulse.R
import com.marketlabs.pulse.ui.theme.LocalPulseColors
import com.marketlabs.pulse.ui.theme.MarketPulseTheme

/**
 * A real (non-illustrative) 3-segment bullish/neutral/bearish band bar with a position marker at
 * an arbitrary fraction -- `TriSegmentBar` can't take a marker without a rewrite (its segments are
 * `RowScope.weight()`-based, no absolute x-coordinate to hang a tick off), so this is a small
 * `Canvas` sibling for the one spot that needs one (How to Read Any Gauge's pattern diagram).
 * Deliberately outside `ui/components/diagrams/` and reads real `LocalPulseColors` signal tokens
 * directly, same as any other production gauge widget -- that package's diagrams are illustrative
 * and signal-color-banned, this one is a literal explanation of the real bullish/neutral/bearish
 * mechanic, not a fictional scenario.
 */
@Composable
fun GaugePatternBandBar(
    bullFraction: Float,
    neutralFraction: Float,
    bearFraction: Float,
    markerFraction: Float,
    modifier: Modifier = Modifier
) {
    val pulseColors = LocalPulseColors.current
    val markerColor = MaterialTheme.colorScheme.onSurface
    val barHeight = dimensionResource(id = R.dimen.tri_segment_bar_height)
    val markerWidth = dimensionResource(id = R.dimen.gauge_needle_width)
    val markerOverhang = dimensionResource(id = R.dimen.bar_tick_overhang)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(barHeight)
            .clip(RoundedCornerShape(barHeight / 2))
    ) {
        val bullWidth = size.width * bullFraction
        val neutralWidth = size.width * neutralFraction
        val bearWidth = size.width - bullWidth - neutralWidth

        drawRect(color = pulseColors.signalBullishText, topLeft = Offset.Zero, size = Size(bullWidth, size.height))
        drawRect(color = pulseColors.signalNeutralText, topLeft = Offset(bullWidth, 0f), size = Size(neutralWidth, size.height))
        drawRect(color = pulseColors.signalBearishText, topLeft = Offset(bullWidth + neutralWidth, 0f), size = Size(bearWidth, size.height))

        val markerX = size.width * markerFraction
        val overhangPx = markerOverhang.toPx()
        drawLine(
            color = markerColor,
            start = Offset(markerX, -overhangPx),
            end = Offset(markerX, size.height + overhangPx),
            strokeWidth = markerWidth.toPx(),
            cap = StrokeCap.Round
        )
    }
}

// ============================================================================
// 🎨 PREVIEWS
// ============================================================================

@Preview(showBackground = true)
@Composable
private fun PreviewGaugePatternBandBar() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        GaugePatternBandBar(bullFraction = 1f / 3f, neutralFraction = 1f / 3f, bearFraction = 1f / 3f, markerFraction = 0.5f)
    }
}
