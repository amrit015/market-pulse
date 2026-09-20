package com.marketlabs.pulse.ui.components.widgets

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.marketlabs.pulse.R
import com.marketlabs.pulse.ui.theme.LocalPulseColors
import com.marketlabs.pulse.ui.theme.MarketPulseTheme
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * A 5-band Fear & Greed dial (redesigned to match a reference "arc + tick marks + floating
 * triangle pointer" gauge), replacing the old 3-color smooth-gradient/needle-and-dot version.
 * Bands run green (Fear) to red (Greed), left to right -- a contrarian read, same convention
 * [PutCallHorizontalBar] now uses: Fear is colored bullish (a buying-opportunity framing), Greed
 * bearish (a warning framing), confirmed against the reference image's own green-on-Fear/
 * red-on-Greed coloring rather than the app's old direct (Fear=red) reading.
 *
 * Only [pulseColors]' own signal tokens feed the 5 bands (green/neutral/red, with the two
 * in-between bands interpolated from them) -- no new raw colors, so every theme preset still
 * produces a correctly-tinted dial.
 */
@Composable
fun SpeedometerGauge(score: Double, previousScore: Double?, status: String?) {
    val pointerColor = MaterialTheme.colorScheme.onSurface
    val tickColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
    val tickStrokeWidthDimen = dimensionResource(id = R.dimen.border_thin)
    val strokeWidthDimen = dimensionResource(id = R.dimen.gauge_stroke_width)

    val pulseColors = LocalPulseColors.current
    val textBearish = pulseColors.signalBearishText
    val textBullish = pulseColors.signalBullishText
    val textNeutral = pulseColors.signalNeutralText

    // 💡 5 bands, not one smooth gradient -- green (Fear) through yellow (Neutral) to red (Greed),
    // the two in-between bands interpolated from the app's own bullish/neutral/bearish tokens so
    // this stays theme-correct without introducing any new raw color.
    val bandColors = remember(textBullish, textNeutral, textBearish) {
        listOf(
            textBullish,
            lerp(textBullish, textNeutral, 0.5f),
            textNeutral,
            lerp(textNeutral, textBearish, 0.5f),
            textBearish
        )
    }

    var needleAngleTarget by remember { mutableFloatStateOf(180f) }
    val needleAngle by animateFloatAsState(
        targetValue = needleAngleTarget,
        animationSpec = tween(durationMillis = 1500),
        label = "speedometer_needle_angle"
    )
    LaunchedEffect(score) {
        needleAngleTarget = 180f + (score.toFloat() / 100f) * 180f
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(0.85f),
            contentAlignment = Alignment.BottomCenter
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f)
            ) {
                val strokeWidth = strokeWidthDimen.toPx()
                val canvasWidth = size.width
                val canvasHeight = size.height
                val center = Offset(canvasWidth / 2, canvasHeight - strokeWidth / 2)
                val arcTopLeft = Offset(strokeWidth / 2, strokeWidth / 2)
                val arcSize = Size(canvasWidth - strokeWidth, canvasWidth - strokeWidth)
                val arcRadius = (canvasWidth - strokeWidth) / 2

                // 💡 5 separate arcs with a thin angular gap between each, not one continuous
                // stroke -- reads as distinct bands (matching the reference gauge) rather than a
                // single smooth blend.
                val bandSweep = 180f / bandColors.size
                val gapDegrees = 2.5f
                bandColors.forEachIndexed { index, color ->
                    drawArc(
                        color = color,
                        startAngle = 180f + index * bandSweep + gapDegrees / 2,
                        sweepAngle = bandSweep - gapDegrees,
                        useCenter = false,
                        topLeft = arcTopLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                // 💡 Minor tick marks just inside the band, evenly spaced -- purely decorative
                // (speedometer-style), proportioned off the band's own stroke width rather than a
                // new fixed dimension.
                val tickOuterRadius = arcRadius - strokeWidth * 0.75f
                val tickLength = strokeWidth * 0.5f
                val tickInnerRadius = tickOuterRadius - tickLength
                val tickStrokePx = tickStrokeWidthDimen.toPx() * 1.5f
                val tickCount = 24
                for (i in 0..tickCount) {
                    val angleRad = Math.toRadians(180.0 + (180.0 / tickCount) * i)
                    val cosA = cos(angleRad).toFloat()
                    val sinA = sin(angleRad).toFloat()
                    drawLine(
                        color = tickColor,
                        start = Offset(center.x + tickInnerRadius * cosA, center.y + tickInnerRadius * sinA),
                        end = Offset(center.x + tickOuterRadius * cosA, center.y + tickOuterRadius * sinA),
                        strokeWidth = tickStrokePx,
                        cap = StrokeCap.Round
                    )
                }

                // 💡 A floating triangle marker just inside the ticks, not a needle from center --
                // rotated to point outward toward the current angle. Compose's `rotate()` turns
                // clockwise for positive degrees (same convention `cos`/`sin` already use below),
                // and the triangle is drawn pointing "up" (angle 270°) before rotation, so
                // `needleAngle + 90f` (== `needleAngle - 270f` mod 360) lands it pointing exactly
                // toward `needleAngle`.
                val pointerRadius = tickInnerRadius - strokeWidth * 0.3f
                val pointerAngleRad = Math.toRadians(needleAngle.toDouble())
                val pointerCenter = Offset(
                    center.x + pointerRadius * cos(pointerAngleRad).toFloat(),
                    center.y + pointerRadius * sin(pointerAngleRad).toFloat()
                )
                val pointerSize = strokeWidth * 0.55f
                rotate(degrees = needleAngle + 90f, pivot = pointerCenter) {
                    val trianglePath = Path().apply {
                        moveTo(pointerCenter.x, pointerCenter.y - pointerSize)
                        lineTo(pointerCenter.x - pointerSize * 0.7f, pointerCenter.y + pointerSize * 0.6f)
                        lineTo(pointerCenter.x + pointerSize * 0.7f, pointerCenter.y + pointerSize * 0.6f)
                        close()
                    }
                    drawPath(path = trianglePath, color = pointerColor)
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 💡 Change pill stacked below the score, not inline beside it -- asked to move
                // off the "number, pill right beside it" row layout.
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))
                Text(
                    text = "${score.toInt()}",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (previousScore != null) {
                    val delta = score - previousScore
                    val isFlat = delta == 0.0
                    val isRising = delta > 0
                    val changeDirection = when {
                        isFlat -> ChangeDirection.FLAT
                        isRising -> ChangeDirection.UP
                        else -> ChangeDirection.DOWN
                    }
                    // 💡 Contrarian, matching the bands/status label below: rising (toward
                    // Greed) reads bearish, falling (toward Fear) reads bullish -- the
                    // triangle still follows the raw numeric sign.
                    DirectionalChangePill(
                        changeText = abs(delta).toInt().toString(),
                        direction = changeDirection,
                        pillColor = when {
                            isFlat -> pulseColors.signalNeutralPill
                            isRising -> pulseColors.signalBearishPill
                            else -> pulseColors.signalBullishPill
                        },
                        contentColor = when {
                            isFlat -> textNeutral
                            isRising -> textBearish
                            else -> textBullish
                        }
                    )
                }
            }
        }

        // 💡 Status and the Fear/Greed scale-end labels on one line now (was Status on its own
        // line, then a separate Fear/Greed row below it) -- Fear/Greed stay the faint,
        // `labelSmall` scale reference at each end; the colored status reading sits between them,
        // still contrarian (Fear -> bullish, Greed -> bearish). `SpaceBetween` across all three
        // (rather than just the two end labels) is what actually centers the status between them.
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))

        Row(
            modifier = Modifier.fillMaxWidth(0.85f),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = R.string.speedometer_gauge_fear_label),
                style = MaterialTheme.typography.labelSmall,
                color = pulseColors.onSurfaceMuted
            )
            Text(
                text = stringResource(id = R.string.speedometer_gauge_greed_label),
                style = MaterialTheme.typography.labelSmall,
                color = pulseColors.onSurfaceMuted
            )
        }

        if (!status.isNullOrEmpty()) {
            val statusColor = when (status.uppercase()) {
                "EXTREME FEAR", "FEAR" -> textBullish
                "EXTREME GREED", "GREED" -> textBearish
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

@Preview(name = "Light", showBackground = true)
@Composable
private fun PreviewSpeedometerGauge() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        SpeedometerGauge(score = 72.0, previousScore = 65.0, status = "GREED")
    }
}

@Preview(name = "Dark", showBackground = true, backgroundColor = 0xFF0D0E12)
@Composable
private fun PreviewSpeedometerGaugeDark() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        SpeedometerGauge(score = 29.0, previousScore = 27.0, status = "FEAR")
    }
}
