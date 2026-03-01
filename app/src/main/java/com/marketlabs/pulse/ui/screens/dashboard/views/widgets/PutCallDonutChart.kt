package com.marketlabs.pulse.ui.screens.dashboard.views.widgets

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import com.marketlabs.pulse.R
import com.marketlabs.pulse.ui.theme.ColorBearish
import com.marketlabs.pulse.ui.theme.ColorBullish
import com.marketlabs.pulse.ui.theme.ColorNeutral

@Composable
fun PutCallDonutChart(ratio: Double, change: Double, status: String?) {
    val totalOptions = ratio + 1.0
    val putPercentage = (ratio / totalOptions).toFloat()

    val putSweepAngle = putPercentage * 360f
    val callSweepAngle = 360f - putSweepAngle

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 💡 The Box layers the Canvas and Text on top of each other
        Box(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .aspectRatio(1f),
            contentAlignment = Alignment.Center // Centers text inside the donut
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 24f

                drawArc(
                    color = ColorBearish,
                    startAngle = 270f,
                    sweepAngle = -putSweepAngle,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                drawArc(
                    color = ColorBullish,
                    startAngle = 270f,
                    sweepAngle = callSweepAngle,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            // 💡 NEW: Text is now INSIDE the Box
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = String.format("%.2f", ratio),
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                val sign = if (change >= 0) "+" else ""
                Text(
                    text = "$sign${String.format("%.2f", change)}%",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (change > 0) ColorBearish else ColorBullish // INVERTED logic for P/C
                )
            }
        }

        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))

        // 💡 ONLY the Status ("FEAR" / "GREED") stays outside the circle
        val statusColor = when (status?.uppercase()) {
            "EXTREME GREED", "GREED", "BULLISH" -> ColorBullish
            "EXTREME FEAR", "FEAR", "BEARISH" -> ColorBearish
            else -> ColorNeutral
        }
        Text(
            text = status ?: "NEUTRAL",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = statusColor
        )
    }
}