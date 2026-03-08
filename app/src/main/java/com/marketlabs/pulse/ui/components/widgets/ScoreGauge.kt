package com.marketlabs.pulse.ui.components.widgets

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marketlabs.pulse.R
import com.marketlabs.pulse.ui.theme.PulseStatusColors
import kotlin.math.abs

@Composable
fun ScoreGauge(
    score: Int,
    previousScore: Int,
    isHigherBetter: Boolean,
    statusText: String?,
    ringColor: Color,
    modifier: Modifier = Modifier
) {
    val delta = score - previousScore

    // 💡 Filled geometric icons visually matched to the exact text height
    val (deltaSymbol, deltaColor) = when {
        delta > 0 -> "▲" to if (isHigherBetter) PulseStatusColors.BullishText else PulseStatusColors.BearishText
        delta < 0 -> "▼" to if (isHigherBetter) PulseStatusColors.BearishText else PulseStatusColors.BullishText
        else -> "▲" to PulseStatusColors.NeutralText // A bold flat line for neutral
    }

    // 💡 Forces long text (like "Sideways Range") to neatly stack into two centered lines
    val formattedStatusText = statusText?.replace(" ", "\n")

    var animationPlayed by remember { mutableFloatStateOf(0f) }
    val currentProgress by animateFloatAsState(
        targetValue = animationPlayed,
        animationSpec = tween(durationMillis = 1500),
        label = "score_animation"
    )

    LaunchedEffect(score) {
        animationPlayed = score / 100f
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(dimensionResource(id = R.dimen.gauge_size_large))
    ) {
        val trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
        val strokeWidthDp = dimensionResource(id = R.dimen.gauge_stroke_width)
        val density = LocalDensity.current

        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidthPx = with(density) { strokeWidthDp.toPx() }
            val sweepAngle = currentProgress * 360f
            val inset = strokeWidthPx / 2
            val arcSize = Size(
                width = size.width - strokeWidthPx,
                height = size.height - strokeWidthPx
            )

            // Faint Background Track
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
            )

            // Foreground Animated Progress Arc
            drawArc(
                color = ringColor,
                startAngle = 270f, // Starts exactly at the top (12 o'clock)
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
            )
        }

        // 💡 Everything is grouped in a single column starting at the 40% vertical mark
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = dimensionResource(id = R.dimen.gauge_size_large) * 0.30f)
        ) {
            // 1. The Score and Delta Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = score.toString(),
                    style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(dimensionResource(R.dimen.padding_micro)))
                Text(
                    text = deltaSymbol,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = deltaColor
                )
                Text(
                    text = abs(delta).toString(),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = deltaColor
                )
            }

            // 2. The Status Text (Immediately below the score)
            if (!formattedStatusText.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp)) // Tiny spacing so they don't visually overlap

                Text(
                    text = formattedStatusText,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = ringColor,
                    lineHeight = 16.sp
                )
            }
        }
    }
}