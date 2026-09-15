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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.marketlabs.pulse.R
import com.marketlabs.pulse.ui.theme.LocalPulseColors
import com.marketlabs.pulse.ui.theme.MarketPulseTheme
import kotlin.math.abs

@Composable
fun PutCallHorizontalBar(ratio: Double, change: Double?, status: String?) {
    val totalOptions = ratio + 1.0

    var putPercentageTarget by remember { mutableFloatStateOf(0f) }
    val putPercentage by animateFloatAsState(
        targetValue = putPercentageTarget,
        animationSpec = tween(durationMillis = 1500),
        label = "put_call_split"
    )
    LaunchedEffect(ratio) {
        putPercentageTarget = (ratio / totalOptions).toFloat()
    }

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

                    // 💡 Only show Change % if it's not null -- shown as a directional pill now.
                    // Contrarian coloring, matching Fear & Greed: a rising ratio means more puts
                    // relative to calls (more hedging/fear), read here as a bullish signal on the
                    // market itself (capitulation/extreme fear, a potential bottom) -- so it gets
                    // the bullish tone even though the triangle points up (the triangle still
                    // follows the raw numeric sign; the pill color is the market-direction read,
                    // those two are allowed to disagree, same as VIX's own pill). Text is unsigned
                    // (magnitude only) since the triangle already states the sign; an exact 0%
                    // reading gets the neutral tone and a flat bar instead of a triangle, since the
                    // ratio did not actually move.
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
                                isRatioRising -> pulseColors.signalBullishPill
                                else -> pulseColors.signalBearishPill
                            },
                            contentColor = when {
                                isFlat -> textNeutral
                                isRatioRising -> textBullish
                                else -> textBearish
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))

                // MIDDLE: Green / Red Split Bar -- green on the puts (fear) side, red on the
                // calls (greed) side, matching the same contrarian convention as the change pill
                // and status text below (more puts = more fear = read as bullish, not bearish).
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(dimensionResource(id = R.dimen.padding_large))
                ) {
                    val splitBrush = Brush.horizontalGradient(
                        0.0f to colorGreen,
                        putPercentage to colorGreen,
                        putPercentage to colorRed,
                        1.0f to colorRed
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

        // BOTTOM: Optional Status ("FEAR" / "GREED") -- contrarian, matching the pill/bar above:
        // Fear (more puts) reads bullish, Greed (more calls) reads bearish.
        if (!status.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))

            val statusColor = when (status.uppercase()) {
                "EXTREME GREED", "GREED", "BULLISH" -> textBearish
                "EXTREME FEAR", "FEAR", "BEARISH" -> textBullish
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
private fun PreviewPutCallHorizontalBar() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        PutCallHorizontalBar(ratio = 0.85, change = -4.2, status = "GREED")
    }
}

@Preview(name = "Dark", showBackground = true, backgroundColor = 0xFF0D0E12)
@Composable
private fun PreviewPutCallHorizontalBarDark() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        PutCallHorizontalBar(ratio = 1.35, change = 6.1, status = "FEAR")
    }
}