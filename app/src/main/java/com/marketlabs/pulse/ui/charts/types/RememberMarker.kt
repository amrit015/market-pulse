package com.marketlabs.pulse.ui.charts.types

import android.graphics.Typeface
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

/**
 * Creates and remembers a [CartesianMarker] for Vico charts.
 *
 * This marker visually consists of three parts:
 * 1. **Label (Balloon):** A text bubble showing the value of the selected point.
 * 2. **Indicator (Dot):** A visual circle rendered directly on the line graph at the touch point.
 * 3. **Guideline:** A vertical line extending from the touch point to the axis.
 *
 * @return A configured [CartesianMarker] ready to be passed to [CartesianChartHost].
 */
@Composable
fun rememberMarker(): CartesianMarker {

    // Vico Insets require raw pixels, so we access the current density to convert Dp -> Px.
    val density = LocalDensity.current
    val paddingInsets = remember(density) {
        with(density) {
            Insets(
                startDp = 8.dp.toPx(),
                topDp = 4.dp.toPx(),
                endDp = 8.dp.toPx(),
                bottomDp = 4.dp.toPx()
            )
        }
    }

    // 1. Prepare Colors: Convert standard MaterialTheme Compose colors to ARGB Ints for Vico Core.
    val labelBackgroundColor = MaterialTheme.colorScheme.surfaceContainerHighest.toArgb()
    val labelStrokeColor = MaterialTheme.colorScheme.primary.toArgb()
    val labelTextColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val indicatorColor = MaterialTheme.colorScheme.primary.toArgb()
    val indicatorStrokeColor = MaterialTheme.colorScheme.surface.toArgb()
    val guidelineColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f).toArgb()

    // 2. The Background: A "Balloon" shape with a small tail pointing down to the graph.
    val labelBackground = rememberShapeComponent(
        // "MarkerCornered" is a class, not always inside Shape companion
        shape = MarkerCorneredShape(CorneredShape.Corner.Rounded),
        fill = Fill(labelBackgroundColor),          // Matches 'fill' param
        strokeFill = Fill(labelStrokeColor),        // Matches 'strokeFill' param
        strokeThickness = 1.dp,                     // Matches 'strokeThickness' param
    )

    // 3. The Label: The text component displaying the data value.
    val label = rememberTextComponent(
        color = androidx.compose.ui.graphics.Color(labelTextColor),
        textSize = 12.sp,                           // Matches 'TextUnit' (sp), NOT dp
        typeface = Typeface.MONOSPACE,
        padding = paddingInsets,
        background = labelBackground,
    )

    // 4. The Indicator: A circular dot (Pill shape) drawn on the chart line.
    val indicator = rememberShapeComponent(
        shape = CorneredShape.Pill,                         // Standard Pill shape
        fill = Fill(indicatorColor),
        strokeFill = Fill(indicatorStrokeColor),
        strokeThickness = 2.dp,
    )

    // 5. The Guideline: A thin vertical line indicating the X-axis position.
    val guideline = remember(guidelineColor) {
        LineComponent(
            fill = Fill(guidelineColor),
            thicknessDp = 1f, // Standard thickness
            shape = Shape.Rectangle
        )
    }

    return remember(label, indicator, guideline) {
        DefaultCartesianMarker(
            label = label,
            labelPosition = DefaultCartesianMarker.LabelPosition.Top,
            indicator = { _ -> indicator },
            indicatorSizeDp = 12f,
            guideline = guideline,
        )
    }
}