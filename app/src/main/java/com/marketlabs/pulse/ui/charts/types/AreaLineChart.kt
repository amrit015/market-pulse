package com.marketlabs.pulse.ui.charts.types

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.toArgb
import com.marketlabs.pulse.ui.charts.model.MockDataGenerator
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.ProvideVicoTheme
import com.patrykandpatrick.vico.compose.m3.common.rememberM3VicoTheme
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.common.Fill
import com.patrykandpatrick.vico.core.common.shader.ShaderProvider

/**
 * Renders a financial-style Area Chart with a gradient fill.
 *
 * **Key Features:**
 * - **High Density Data:** Optimized for rendering intraday stock data (approx. 100 points).
 * - **Gradient Fill:** Uses a vertical gradient that fades from opaque to transparent under the line.
 * - **Clean Look:** No data points (dots) are rendered to maintain a clean "sparkline" aesthetic.
 * - **Interaction:** Includes a touch marker via [rememberMarker].
 *
 * @param modifier The modifier to be applied to the chart container.
 */
@Composable
fun AreaLineChart(
    modifier: Modifier = Modifier
) {

    val modelProducer = remember { CartesianChartModelProducer() }

    // Load mock intraday data (high frequency, jagged line)
    LaunchedEffect(Unit) {
        val rawData = MockDataGenerator.generateIntradayData()
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

    // Define Colors (Red/Orange Stock Look)
    val stockColor = Color(0xFFD32F2F) // Stock Red
    val stockColorInt = stockColor.toArgb()

    // Create a Compose Brush for the gradient (Red -> Transparent)
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            stockColor.copy(alpha = 0.4f), // Top: 40% opacity
            stockColor.copy(alpha = 0.05f) // Bottom: Almost transparent
        )
    )

    // Define the appearance of the line and the fill area
    val lineSpec = remember(stockColorInt, gradientBrush) {
        LineCartesianLayer.Line(
            // The solid line stroke
            fill = LineCartesianLayer.LineFill.single(Fill(stockColorInt)),
            stroke = LineCartesianLayer.LineStroke.Continuous(thicknessDp = 2f),

            // The gradient area under the line
            areaFill = LineCartesianLayer.AreaFill.single(
                fill = Fill(
                    // ADAPTER: Bridge Compose Brush -> Vico Shader
                    // Vico's 'Fill' expects a 'ShaderProvider'. We implement this interface manually
                    // to convert our Compose 'gradientBrush' into an Android 'Shader' using the bounds provided by Vico.
                    ShaderProvider { _, left, top, right, bottom ->
                        (gradientBrush as ShaderBrush)
                            .createShader(Size(right - left, bottom - top))
                    }
                )
            ),
            // Hide dots for a cleaner financial look
            pointProvider = null
        )
    }

    ProvideVicoTheme(rememberM3VicoTheme()) {
        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberLineCartesianLayer(
                    lineProvider = LineCartesianLayer.LineProvider.series(lineSpec)
                ),
                // Minimalist Axes (no labels/grid customized here, relying on defaults)
                startAxis = VerticalAxis.rememberStart(),
                bottomAxis = HorizontalAxis.rememberBottom(),
                marker = rememberMarker()
            ),
            modelProducer = modelProducer,
            modifier = modifier
        )
    }

}