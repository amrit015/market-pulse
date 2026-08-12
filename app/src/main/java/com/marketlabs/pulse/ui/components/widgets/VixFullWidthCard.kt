package com.marketlabs.pulse.ui.components.widgets

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.marketlabs.pulse.R
import com.marketlabs.pulse.storage.model.dashboard.AssetOverview
import com.marketlabs.pulse.ui.components.PulseCard
import com.marketlabs.pulse.ui.components.PulseCardStyle
import com.marketlabs.pulse.ui.theme.LocalPulseColors
import kotlin.math.abs

@Composable
fun VixFullWidthCard(asset: AssetOverview, onClick: () -> Unit) {

    val pulseColors = LocalPulseColors.current
    // 💡 The old flat, theme-unaware ColorGreen/Red/Neutral constants are deleted -- the locked
    // signal.*.text tokens are the direct replacement for a gradient/brush use like this one.
    val colorGreen = pulseColors.signalBullishText
    val colorRed = pulseColors.signalBearishText
    val colorNeutral = pulseColors.signalNeutralText

    val textBearish = pulseColors.signalBearishText
    val textBullish = pulseColors.signalBullishText

    val paddingLarge = dimensionResource(id = R.dimen.padding_large)
    val paddingMedium = dimensionResource(id = R.dimen.padding_medium)

    val price = asset.price ?: 0.0
    val change = asset.changePercent
    val drawLineColor = MaterialTheme.colorScheme.onSurface

    // Load dimens
    val needleWidthDimen = dimensionResource(id = R.dimen.gauge_needle_width)
    val needleOverhangDimen = dimensionResource(id = R.dimen.gauge_needle_overhang)
    val cornerRadiusDimen = dimensionResource(id = R.dimen.vix_corner_radius)

    // 💡 This card previously flipped its own background between a bullish and bearish tint based
    // on a "contrarian" read of the VIX status (high VIX -> bullish-tinted card, since a volatility
    // spike is often a buying opportunity; low VIX -> bearish-tinted card, since complacency often
    // precedes a pullback). The container is uniform and non-directional now, matching every other
    // price/gauge card in this app -- direction still reads clearly from the status text color and
    // the needle position, just not the card fill.
    //
    // DATA style -- the same tinted background and hairline border every equity price card uses.
    PulseCard(
        style = PulseCardStyle.DATA,
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(paddingLarge)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(id = R.string.dashboard_vix_title),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // 💡 Null-safe Status with Dynamic Red/Green Coloring!
                    if (!asset.rsiStatus.isNullOrEmpty()) {
                        val statusColor = when (asset.rsiStatus.uppercase()) {
                            "EXTREME GREED", "GREED", "BULLISH" -> textBullish  // VIX is low (Bad for market/Sell warning)
                            "EXTREME FEAR", "FEAR", "BEARISH" -> textBearish // VIX is high (Good for market/Buy opp)
                            else -> colorNeutral
                        }

                        Text(
                            text = asset.rsiStatus,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = statusColor
                        )
                    }
                }

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = String.format("%.2f", price),
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // 💡 Null-safe Change % -- shown as a directional pill now instead of plain
                    // text. The triangle still follows the raw numeric sign (up = VIX rose), but the
                    // pill's color keeps the VIX contrarian read (rising VIX = bearish for
                    // equities, falling VIX = bullish) -- those two things are allowed to disagree.
                    // The triangle already states the sign, so the text is unsigned (magnitude
                    // only); an exact 0% reading gets the neutral tone and a flat bar instead of a
                    // (necessarily arbitrary) bullish/bearish read, since VIX did not actually move.
                    if (change != null) {
                        // 💡 Gap from the price value bumped from `padding_small` to `padding_medium`
                        // -- the pill sitting almost flush against the price read cramped once it grew.
                        Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_medium)))
                        val isFlat = change == 0.0
                        val isVixRising = change > 0
                        val changeDirection = when {
                            isFlat -> ChangeDirection.FLAT
                            isVixRising -> ChangeDirection.UP
                            else -> ChangeDirection.DOWN
                        }
                        DirectionalChangePill(
                            changeText = "${String.format("%.2f", abs(change))}%",
                            direction = changeDirection,
                            pillColor = when {
                                isFlat -> pulseColors.signalNeutralPill
                                isVixRising -> pulseColors.signalBearishPill
                                else -> pulseColors.signalBullishPill
                            },
                            contentColor = when {
                                isFlat -> colorNeutral
                                isVixRising -> textBearish
                                else -> textBullish
                            },
                            modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.padding_tiny))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(paddingMedium))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(dimensionResource(id = R.dimen.padding_large))
            ) {
                // Convert Dp to Px dynamically
                val needleWidth = needleWidthDimen.toPx()
                val needleOverhang = needleOverhangDimen.toPx()
                val cornerRadius = cornerRadiusDimen.toPx()

                val percentage = ((price.toFloat() - 10f) / 30f).coerceIn(0f, 1f)
                val thumbX = size.width * percentage

                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        listOf(colorGreen, colorNeutral, colorRed)
                    ),
                    size = size,
                    cornerRadius = CornerRadius(cornerRadius, cornerRadius)
                )

                drawLine(
                    color = drawLineColor,
                    start = Offset(thumbX, -needleOverhang),
                    end = Offset(thumbX, size.height + needleOverhang),
                    strokeWidth = needleWidth,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}