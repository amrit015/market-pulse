package com.marketlabs.pulse.ui.charts.compose

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.marketlabs.pulse.ui.charts.model.ChartPoint
import com.marketlabs.pulse.ui.charts.model.MockDataGenerator
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.ProvideVicoTheme
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.m3.common.rememberM3VicoTheme
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.common.Fill
import com.patrykandpatrick.vico.core.common.component.LineComponent
import com.patrykandpatrick.vico.core.common.shape.CorneredShape
import com.patrykandpatrick.vico.core.common.shape.DashedShape

/**
 * Renders a standard Line Chart suitable for comparative data (e.g., performance over years).
 *
 * **Key Features:**
 * - **Data Points:** Visible dots (circles) are rendered at every data interval.
 * - **Dashed Grid:** The background grid lines are dashed to mimic graph paper.
 * - **Markers:** Supports touch interaction via [rememberMarker].
 *
 * @param modifier The modifier to be applied to the chart container.
 */
@Composable
fun SimpleLineChart(
    modifier: Modifier = Modifier
) {

    // 1. Create the Producer that holds the data
    val modelProducer = remember { CartesianChartModelProducer() }

    // 2. Generate Data on Launch (standard SPY data simulation)
    LaunchedEffect(Unit) {
        val rawData: List<ChartPoint> = MockDataGenerator.generateSPYData(100)
        // Vico uses a transaction-style update
        modelProducer.runTransaction {
            lineSeries {
                series(
                    x = rawData.map { it.x },
                    y = rawData.map { it.y }
                )
            }
        }
    }

    // --- STYLING ---

    // Define the Point (The visible dot on the line)
    val pointColor = MaterialTheme.colorScheme.primary.toArgb()
    val pointStrokeColor = MaterialTheme.colorScheme.surface.toArgb()

    val pointComponent = rememberShapeComponent(
        shape = CorneredShape(CorneredShape.Corner.Rounded),
        fill = Fill(pointColor),
        strokeFill = Fill(pointStrokeColor),
        strokeThickness = 2.dp,
    )

    // Define the Grid Lines (Dashed styling)
    val gridColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f).toArgb()

    // We create a LineComponent that uses a DashedShape to simulate graph paper
    val dashedGuideline = remember(gridColor) {
        LineComponent(
            fill = Fill(gridColor),
            thicknessDp = 1f,
            shape = DashedShape(
                shape = CorneredShape(CorneredShape.Corner.Sharp),
                dashLengthDp = 4f,
                gapLengthDp = 8f
            )
        )
    }

    val lineColorInt = MaterialTheme.colorScheme.primary.toArgb()
    // Define the Line Spec Manually to control Stroke, Fill, and Points
    val lineSpec = remember(lineColorInt, pointComponent) {
        LineCartesianLayer.Line(
            // A. Fill: The solid color of the line connecting points
            fill = LineCartesianLayer.LineFill.single(Fill(lineColorInt)),

            // B. Stroke: Controls the thickness of the line
            stroke = LineCartesianLayer.LineStroke.Continuous(thicknessDp = 2f),

            // C. PointProvider: Configures the dots. ".single()" applies the same style to all series.
            pointProvider = LineCartesianLayer.PointProvider.single(LineCartesianLayer.Point(
                component = pointComponent,
                sizeDp = 10f
            ))
        )
    }

    // 3. Render the chart
    ProvideVicoTheme(rememberM3VicoTheme()) {
        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberLineCartesianLayer(
                    lineProvider = LineCartesianLayer.LineProvider.series(lineSpec)
                ),
                // Apply dashed guidelines to both axes
                startAxis = VerticalAxis.rememberStart(
                    guideline = dashedGuideline
                ),
                bottomAxis = HorizontalAxis.rememberBottom(
                    guideline = dashedGuideline
                ),
                // Attach the interaction marker
                marker = rememberMarker()
            ),
            modelProducer = modelProducer,
            modifier = modifier
        )
    }
}