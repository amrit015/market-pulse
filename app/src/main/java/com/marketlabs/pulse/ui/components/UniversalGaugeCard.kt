package com.marketlabs.pulse.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.marketlabs.pulse.R

@Composable
fun UniversalGaugeCard(
    title: String,
    value: String?,
    score: Int? = null,
    signalText: String? = null,
    baseColor: Color,
    bgColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    // 💡 Initialize to safe fallbacks if null is provided
    val safeScore = score ?: 0
    val notAvail = stringResource(id = R.string.not_available_short)

    Card(
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_card_large)),
        modifier = modifier.clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))
        ) {
            // Header Row (Title + Chevron - matching UniversalMetricCard)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    painter = painterResource(id = R.drawable.ic_chevron_forward),
                    contentDescription = "View Details",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(dimensionResource(R.dimen.padding_large))
                        .padding(start = dimensionResource(R.dimen.padding_small))
                )
            }

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))

            // 💡 Bottom Row: Split between Value/Signal (Left) and Score/Bar (Right)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                // Left Column: Raw Value and Signal Label
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = value ?: notAvail,
                        // 💡 Matched typography to UniversalMetricCard's value styling
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))

                    // 💡 Wrapped in Surface and matched typography to UniversalMetricCard's signal styling
                    Surface(
                        color = baseColor.copy(alpha = 0.0f),
                        shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_chip))
                    ) {
                        Text(
                            // 💡 ADDED: .uppercase() to ensure the status is always fully capitalized
                            text = (signalText ?: notAvail).uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = baseColor,
                            modifier = Modifier.padding(
                                horizontal = dimensionResource(id = R.dimen.padding_small),
                                vertical = dimensionResource(id = R.dimen.padding_tiny)
                            )
                        )
                    }
                }

                // Right Column: Score and Progress Bar
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Text(
                        text = stringResource(id = R.string.radar_score_out_of_100, safeScore),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))

                    LinearProgressIndicator(
                        progress = { safeScore / 100f },
                        modifier = Modifier
                            .width(dimensionResource(id = R.dimen.progress_bar_width))
                            .height(dimensionResource(id = R.dimen.progress_bar_height)),
                        color = baseColor,
                        trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
                        strokeCap = StrokeCap.Round,
                        gapSize = 0.dp,
                        drawStopIndicator = {}
                    )
                }
            }
        }
    }
}