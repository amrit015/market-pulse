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
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import com.marketlabs.pulse.R
import com.marketlabs.pulse.ui.theme.LocalPulseColors
import kotlin.math.abs

@Composable
fun PutCallHorizontalBar(ratio: Double, change: Double?, status: String?) {
    val totalOptions = ratio + 1.0
    val putPercentage = (ratio / totalOptions).toFloat()

    val drawLineColor = MaterialTheme.colorScheme.onSurface
    val pulseColors = LocalPulseColors.current
    // 💡 The old flat, theme-unaware ColorGreen/Red/Neutral constants are deleted -- the locked
    // signal.*.text tokens are the direct replacement for a gradient/brush use like this one.
    val colorGreen = pulseColors.signalBullishText
    val colorRed = pulseColors.signalBearishText
    val colorNeutral = pulseColors.signalNeutralText

    val textBearish = pulseColors.signalBearishText
    val textBullish = pulseColors.signalBullishText
    val textNeutral = pulseColors.signalNeutralText

    // Load dimens
    val tickOverhang = dimensionResource(id = R.dimen.bar_tick_overhang)
    val needleWidth = dimensionResource(id = R.dimen.gauge_needle_width)

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // TOP: Score & Optional Change %
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = String.format("%.2f", ratio),
                        // 💡 FIX: Reduced text size from headlineMedium to headlineSmall
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // 💡 Only show Change % if it's not null -- shown as a directional pill now,
                    // matching VixFullWidthCard's treatment (same contrarian coloring: a rising
                    // ratio is bad, so it gets the bearish tone even though the triangle points
                    // up). Text is unsigned (magnitude only) since the triangle already states the
                    // sign; an exact 0% reading gets the neutral tone and a flat bar instead of a
                    // triangle, since the ratio did not actually move.
                    if (change != null) {
                        // 💡 Gap from the ratio value bumped from `padding_small` to `padding_medium`
                        // -- the pill sitting almost flush against the value read cramped once it grew.
                        Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_medium)))

                        val isFlat = change == 0.0
                        val isRatioRising = change > 0
                        val changeDirection = when {
                            isFlat -> ChangeDirection.FLAT
                            isRatioRising -> ChangeDirection.UP
                            else -> ChangeDirection.DOWN
                        }
                        DirectionalChangePill(
                            changeText = "${String.format("%.2f", abs(change))}%",
                            direction = changeDirection,
                            pillColor = when {
                                isFlat -> pulseColors.signalNeutralPill
                                isRatioRising -> pulseColors.signalBearishPill
                                else -> pulseColors.signalBullishPill
                            },
                            contentColor = when {
                                isFlat -> textNeutral
                                isRatioRising -> textBearish
                                else -> textBullish
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))

                // MIDDLE: Red / Green Split Bar
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(dimensionResource(id = R.dimen.padding_large))
                ) {
                    val splitBrush = Brush.horizontalGradient(
                        0.0f to colorRed,
                        putPercentage to colorRed,
                        putPercentage to colorGreen,
                        1.0f to colorGreen
                    )

                    drawRoundRect(
                        brush = splitBrush,
                        size = size,
                        cornerRadius = CornerRadius(size.height / 2, size.height / 2)
                    )

                    // Center Anchor Tick (converting Dp to Px for Canvas)
                    val centerX = size.width / 2
                    val overhangPx = tickOverhang.toPx()
                    drawLine(
                        color = drawLineColor,
                        start = Offset(centerX, -overhangPx),
                        end = Offset(centerX, size.height + overhangPx),
                        strokeWidth = needleWidth.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }
        }

        // BOTTOM: Optional Status ("FEAR" / "GREED")
        if (!status.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))

            val statusColor = when (status.uppercase()) {
                "EXTREME GREED", "GREED", "BULLISH" -> textBullish
                "EXTREME FEAR", "FEAR", "BEARISH" -> textBearish
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