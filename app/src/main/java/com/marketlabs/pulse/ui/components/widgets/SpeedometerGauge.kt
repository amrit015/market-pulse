package com.marketlabs.pulse.ui.components.widgets

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import com.marketlabs.pulse.R
import com.marketlabs.pulse.ui.theme.ColorGreen
import com.marketlabs.pulse.ui.theme.ColorNeutral
import com.marketlabs.pulse.ui.theme.ColorRed
import com.marketlabs.pulse.ui.theme.PulseStatusColors.BearishText
import com.marketlabs.pulse.ui.theme.PulseStatusColors.BullishText
import com.marketlabs.pulse.ui.theme.PulseStatusColors.NeutralText
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SpeedometerGauge(score: Double, previousScore: Double?, status: String?) {
    val pointerColor = MaterialTheme.colorScheme.onSurface

    // Load dimens
    val strokeWidthDimen = dimensionResource(id = R.dimen.gauge_stroke_width)
    val needleWidthDimen = dimensionResource(id = R.dimen.gauge_needle_width)
    val needleRadiusDimen = dimensionResource(id = R.dimen.gauge_needle_radius)

    val colorGreen  = ColorGreen
    val colorRed = ColorRed
    val colorNeutral = ColorNeutral

    val textBearish = BearishText
    val textBullish = BullishText
    val textNeutral = NeutralText

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            // Shrunk the width to 85%, which proportionally decreases the height
            modifier = Modifier.fillMaxWidth(0.85f),
            contentAlignment = Alignment.BottomCenter
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f)
            ) {
                // Convert Dp to Px dynamically for the current screen size
                val strokeWidth = strokeWidthDimen.toPx()
                val needleWidth = needleWidthDimen.toPx()
                val needleRadius = needleRadiusDimen.toPx()

                val canvasWidth = size.width
                val canvasHeight = size.height

                // Swapped Red to the left (0-40) and Green to the right (60-100)
                val brush = Brush.horizontalGradient(
                    colors = listOf(colorRed, colorNeutral, colorGreen)
                )

                drawArc(
                    brush = brush,
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                    size = Size(canvasWidth - strokeWidth, (canvasWidth - strokeWidth)),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                val angleDegree = 180f + (score.toFloat() / 100f) * 180f
                val angleRad = Math.toRadians(angleDegree.toDouble())
                val radius = canvasWidth / 2 - strokeWidth
                val centerPoint = Offset(canvasWidth / 2, canvasHeight - strokeWidth / 2)

                val endX = (centerPoint.x + radius * cos(angleRad)).toFloat()
                val endY = (centerPoint.y + radius * sin(angleRad)).toFloat()

                drawLine(
                    color = pointerColor.copy(alpha = 0.2f),
                    start = centerPoint,
                    end = Offset(endX, endY),
                    strokeWidth = needleWidth,
                    cap = StrokeCap.Round
                )
                drawCircle(color = pointerColor, radius = needleRadius, center = centerPoint)
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.padding_medium))
            ) {
                Text(
                    text = "${score.toInt()}",
                    // Reduced text size from headlineMedium to headlineSmall
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                // 💡 Calculated Delta from previousScore
                if (previousScore != null) {
                    val delta = score - previousScore

                    val (deltaSymbol, deltaColor) = when {
                        delta > 0 -> "▲" to textBullish // Up = Greed (Green)
                        delta < 0 -> "▼" to textBearish // Down = Fear (Red)
                        else -> "━" to textNeutral
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = deltaSymbol,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = deltaColor
                        )
                        Spacer(modifier = Modifier.width(dimensionResource(R.dimen.padding_micro)))
                        Text(
                            text = abs(delta).toInt().toString(),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = deltaColor
                        )
                    }
                }
            }
        }

        // 💡 ONLY the Status stays below the graphic, if it exists
        if (!status.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_large)))

            val statusColor = when (status.uppercase()) {
                "EXTREME GREED", "GREED" -> textBullish // Greed = Green
                "EXTREME FEAR", "FEAR" -> textBearish   // Fear = Red
                else -> textNeutral
            }
            Text(
                text = status,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = statusColor
            )
        }
    }
}