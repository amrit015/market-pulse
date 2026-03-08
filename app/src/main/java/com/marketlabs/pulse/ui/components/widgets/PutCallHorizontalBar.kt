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
import com.marketlabs.pulse.ui.theme.ColorGreen
import com.marketlabs.pulse.ui.theme.ColorNeutral
import com.marketlabs.pulse.ui.theme.ColorRed
import com.marketlabs.pulse.ui.theme.PulseStatusColors.BearishText
import com.marketlabs.pulse.ui.theme.PulseStatusColors.BullishText
import com.marketlabs.pulse.ui.theme.PulseStatusColors.NeutralText

@Composable
fun PutCallHorizontalBar(ratio: Double, change: Double?, status: String?) {
    val totalOptions = ratio + 1.0
    val putPercentage = (ratio / totalOptions).toFloat()

    val drawLineColor = MaterialTheme.colorScheme.onSurface
    val colorGreen  = ColorGreen
    val colorRed = ColorRed
    val colorNeutral = ColorNeutral

    val textBearish = BearishText
    val textBullish = BullishText
    val textNeutral = NeutralText

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
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // 💡 Only show Change % if it's not null
                    if (change != null) {
                        Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_small)))

                        val sign = if (change >= 0) "+" else ""
                        Text(
                            text = "$sign${String.format("%.2f", change)}%",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (change > 0) textBearish else textBullish // INVERTED: Ratio UP is Bad
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
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))

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