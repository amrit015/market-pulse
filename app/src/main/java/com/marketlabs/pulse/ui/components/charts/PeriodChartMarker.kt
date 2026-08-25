package com.marketlabs.pulse.ui.components.charts

// Rescoped from the orphaned ui/charts/types/RememberMarker.kt — same balloon-label + indicator
// dot + guideline shape, restyled off LocalPulseColors instead of raw MaterialTheme.colorScheme
// so it matches this app's token contract like every other themed component.

import android.graphics.Typeface
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
 * Touch marker for [PeriodChart] and [IntradayPeriodChart]: a balloon label showing the touched
 * point's label *and* price (not just the raw y-value Vico's default formatter would show), an
 * indicator dot on the line, and a vertical guideline. Shape-agnostic over what's actually being
 * plotted -- [pointCount] is the series length and [labelForIndex] resolves a touched index back
 * to its display label (a short date for [PeriodChart]'s `ChartPoint`s, a time-of-day for
 * [IntradayPeriodChart]'s `IntradayPoint`s), since the two callers' x-values are both plain
 * indices into their own point list (see each chart's `x = points.indices...` series) but disagree
 * on what that index actually represents.
 */
@Composable
fun rememberPeriodChartMarker(pointCount: Int, labelForIndex: (Int) -> String): CartesianMarker {
    val pulseColors = LocalPulseColors.current
    val density = LocalDensity.current

    val paddingInsets = remember(density) {
        with(density) {
            Insets(startDp = 8.dp.toPx(), topDp = 4.dp.toPx(), endDp = 8.dp.toPx(), bottomDp = 4.dp.toPx())
        }
    }

    val labelBackgroundColor = pulseColors.surfaceTinted.toArgb()
    val labelStrokeColor = pulseColors.accentPrimary.toArgb()
    val labelTextColor = pulseColors.onSurfaceMuted.toArgb()
    val indicatorColor = pulseColors.accentPrimary.toArgb()
    val indicatorStrokeColor = pulseColors.accentOn.toArgb()
    val guidelineColor = pulseColors.onSurfaceMuted.copy(alpha = 0.2f).toArgb()

    val labelBackground = rememberShapeComponent(
        shape = MarkerCorneredShape(CorneredShape.Corner.Rounded),
        fill = Fill(labelBackgroundColor),
        strokeFill = Fill(labelStrokeColor),
        strokeThickness = 1.dp
    )

    val label = rememberTextComponent(
        color = Color(labelTextColor),
        textSize = 12.sp,
        typeface = Typeface.MONOSPACE,
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
            indicatorSizeDp = 12f,
            guideline = guideline
        )
    }
}
