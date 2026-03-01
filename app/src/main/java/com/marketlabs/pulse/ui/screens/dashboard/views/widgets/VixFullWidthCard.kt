package com.marketlabs.pulse.ui.screens.dashboard.views.widgets

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.marketlabs.pulse.ui.theme.ColorBearish
import com.marketlabs.pulse.ui.theme.ColorBullish
import com.marketlabs.pulse.ui.theme.ColorNeutral

@Composable
fun VixFullWidthCard(asset: AssetOverview, onClick: () -> Unit) {
    val paddingLarge = dimensionResource(id = R.dimen.padding_large)
    val paddingMedium = dimensionResource(id = R.dimen.padding_medium)

    val price = asset.price ?: 0.0
    val change = asset.changePercent
    val drawLineColor = MaterialTheme.colorScheme.onSurface

    // Load dimens
    val needleWidthDimen = dimensionResource(id = R.dimen.gauge_needle_width)
    val needleOverhangDimen = dimensionResource(id = R.dimen.gauge_needle_overhang)
    val cornerRadiusDimen = dimensionResource(id = R.dimen.vix_corner_radius)

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_card_large)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
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

                    // 💡 Null-safe Status
                    if (!asset.rsiStatus.isNullOrEmpty()) {
                        Text(
                            text = asset.rsiStatus,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = String.format("%.2f", price),
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // 💡 Null-safe Change %
                    if (change != null) {
                        Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_small)))
                        val sign = if (change >= 0) "+" else ""
                        Text(
                            text = "$sign${String.format("%.2f", change)}%",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (change > 0) ColorBearish else ColorBullish,
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
                        listOf(ColorBullish, ColorNeutral, ColorBearish)
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