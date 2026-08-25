package com.marketlabs.pulse.ui.components.charts

// Rescoped from the orphaned ui/charts/types/RememberMarker.kt — same balloon-label + indicator
// dot + guideline shape, restyled off LocalPulseColors instead of raw MaterialTheme.colorScheme
// so it matches this app's token contract like every other themed component.

import android.graphics.Typeface
import android.text.Layout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marketlabs.pulse.ui.theme.LocalPulseColors
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.core.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.core.common.Fill
import com.patrykandpatrick.vico.core.common.Insets
import com.patrykandpatrick.vico.core.common.component.LineComponent
import com.patrykandpatrick.vico.core.common.shape.CorneredShape
import com.patrykandpatrick.vico.core.common.shape.MarkerCorneredShape
import com.patrykandpatrick.vico.core.common.shape.Shape
import kotlin.math.roundToInt

/**
 * Touch marker for [PeriodChart], [IntradayPeriodChart], and
 * [IndicatorHistoryChart][com.marketlabs.pulse.ui.components.charts.IndicatorHistoryChart]: a
 * balloon label showing the touched point's label, value, and percent change (not just the raw
 * y-value Vico's default formatter would show), an indicator dot on the line, and a vertical
 * guideline. Shape-agnostic over what's actually being plotted -- [pointCount] is the series
 * length and [labelForIndex] resolves a touched index back to its full display text, since each
 * caller's x-values are plain indices into its own point list (see each chart's
 * `x = points.indices...` series) but disagrees on what that index actually represents and how to
 * format it. Every caller builds a 2-line label (`"date/time  value\npercent change"`) -- the
 * underlying `TextComponent` renders `\n` as a real line break, so this needs no special handling
 * here beyond passing the string through.
 */
@Composable
fun rememberPeriodChartMarker(pointCount: Int, labelForIndex: (Int) -> String): CartesianMarker {
    val pulseColors = LocalPulseColors.current
    val density = LocalDensity.current

    // 💡 Compact insets -- was 8dp/4dp. Smaller padding directly shrinks the marker's own
    // measured height, which in turn shrinks the top margin Vico reserves above the chart's plot
    // area for it (see `DefaultCartesianMarker.updateLayerMargins`: `top = label.getHeight() +
    // tickSizeDp`, computed with `LabelPosition.Top`) -- that reserved space is static regardless
    // of whether a touch is active, so a bulkier marker meant a bigger permanent gap at the top of
    // every chart, not just a bigger balloon while scrubbing.
    val paddingInsets = remember(density) {
        with(density) {
            Insets(startDp = 6.dp.toPx(), topDp = 3.dp.toPx(), endDp = 6.dp.toPx(), bottomDp = 3.dp.toPx())
        }
    }

    val labelBackgroundColor = pulseColors.surfaceTinted.toArgb()
    val labelStrokeColor = pulseColors.accentPrimary.toArgb()
    val labelTextColor = pulseColors.onSurfaceMuted.toArgb()
    val indicatorColor = pulseColors.accentPrimary.toArgb()
    val indicatorStrokeColor = pulseColors.accentOn.toArgb()
    val guidelineColor = pulseColors.onSurfaceMuted.copy(alpha = 0.2f).toArgb()

    // 💡 Smaller tick (was Vico's 6dp default) -- same reserved-top-margin reasoning as the
    // padding above; `tickSizeDp` is added directly into that reservation too.
    val labelBackground = rememberShapeComponent(
        shape = MarkerCorneredShape(CorneredShape.Corner.Rounded, tickSizeDp = 4f),
        fill = Fill(labelBackgroundColor),
        strokeFill = Fill(labelStrokeColor),
        strokeThickness = 1.dp
    )

    // 💡 `lineCount` defaults to 1 in Vico's own TextComponent -- the label's underlying
    // StaticLayout is built with `maxLines = lineCount`, so the percent-change second line was
    // silently truncated/ellipsized regardless of available width until this was set explicitly.
    // `ALIGN_CENTER` so the (usually shorter) percent-change line centers under the date/value
    // line instead of sitting flush left with it. `lineHeight` tightens the gap between the two
    // lines (Vico's own default leading otherwise adds noticeably more than a compact 2-line
    // balloon needs).
    val label = rememberTextComponent(
        color = Color(labelTextColor),
        textSize = 10.sp,
        lineHeight = 12.sp,
        typeface = Typeface.MONOSPACE,
        textAlignment = Layout.Alignment.ALIGN_CENTER,
        lineCount = 2,
        padding = paddingInsets,
        background = labelBackground
    )

    val indicator = rememberShapeComponent(
        shape = CorneredShape.Pill,
        fill = Fill(indicatorColor),
        strokeFill = Fill(indicatorStrokeColor),
        strokeThickness = 2.dp
    )

    val guideline = remember(guidelineColor) {
        LineComponent(fill = Fill(guidelineColor), thicknessDp = 1f, shape = Shape.Rectangle)
    }

    val valueFormatter = remember(pointCount, labelForIndex) {
        DefaultCartesianMarker.ValueFormatter { _, targets ->
            // Clamp rather than falling back to "" -- an out-of-range index shouldn't be
            // reachable here (targets come from real chart positions), but an empty label reads
            // as a rendering bug rather than "no data," so this stays defensively non-empty
            // either way, same reasoning as `PeriodChart`'s x-axis formatter.
            val index = (targets.firstOrNull()?.x?.roundToInt() ?: 0).coerceIn(0, pointCount - 1)
            labelForIndex(index)
        }
    }

    return remember(label, indicator, guideline, valueFormatter) {
        DefaultCartesianMarker(
            label = label,
            valueFormatter = valueFormatter,
            labelPosition = DefaultCartesianMarker.LabelPosition.Top,
            indicator = { _ -> indicator },
            indicatorSizeDp = 10f,
            guideline = guideline
        )
    }
}
