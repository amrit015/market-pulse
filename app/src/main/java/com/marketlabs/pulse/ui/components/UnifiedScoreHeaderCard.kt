package com.marketlabs.pulse.ui.components

import androidx.compose.animation.animateContentSize
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.marketlabs.pulse.R
import com.marketlabs.pulse.ui.components.widgets.ScoreGauge

data class StatusPillState(
    val text: String,
    val textColor: Color,
    val iconRes: Int? = null
)

@Composable
fun UnifiedScoreHeaderCard(
    title: String,
    score: Int,
    previousScore: Int,
    isHigherBetter: Boolean,
    scoreLabel: String,
    ringColor: Color,
    headerBgColor: Color,
    pills: List<StatusPillState>,
    summaryText: String,
    onClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var hasOverflow by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = headerBgColor),
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_card_extra_large)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(id = R.dimen.padding_extra_large))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ScoreGauge(
                    score = score,
                    previousScore = previousScore,
                    isHigherBetter = isHigherBetter,
                    statusText = scoreLabel,
                    ringColor = ringColor
                )

                Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_large)))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_small))
                ) {
                    pills.forEach { pill ->
                        Surface(
                            color = pill.textColor.copy(alpha = .1f),
                            shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_pill))
                        ) {
                            Row(
                                modifier = Modifier.padding(
                                    horizontal = dimensionResource(id = R.dimen.padding_medium),
                                    vertical = dimensionResource(id = R.dimen.padding_small)
                                ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                Icon(
                                    painter = painterResource(id = R.drawable.ic_engine_quant),
                                    contentDescription = "Analysis Engine",
                                    tint = pill.textColor,
                                    modifier = Modifier.size(dimensionResource(R.dimen.padding_large))
                                )

                                Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_medium)))

                                pill.iconRes?.let { icon ->
                                    Icon(
                                        painter = painterResource(id = icon),
                                        contentDescription = null,
                                        tint = pill.textColor,
                                        modifier = Modifier.size(dimensionResource(id = R.dimen.icon_size_small))
                                    )
                                    Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_small)))
                                }

                                Text(
                                    text = pill.text.uppercase(),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = pill.textColor
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_medium)))

                Icon(
                    painter = painterResource(id = R.drawable.ic_chevron_forward),
                    contentDescription = "View Details",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(dimensionResource(R.dimen.padding_large))
                )
            }

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_large)))

            // 3. Expandable Summary
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (hasOverflow || expanded) {
                            Modifier.clickable { expanded = !expanded }
                        } else Modifier
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = summaryText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = if (expanded) Int.MAX_VALUE else 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .animateContentSize(),
                    onTextLayout = { textLayoutResult ->
                        if (!expanded) {
                            hasOverflow = textLayoutResult.hasVisualOverflow
                        }
                    }
                )

                if (hasOverflow || expanded) {
                    Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_small)))
                    Icon(
                        painter = painterResource(id = if (expanded) R.drawable.ic_arrow_up else R.drawable.ic_arrow_down),
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(dimensionResource(id = R.dimen.icon_size_small))
                    )
                }
            }
        }
    }
}

// ============================================================================
// 🎨 COMPOSE PREVIEWS
// ============================================================================

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
fun PreviewUnifiedScoreHeaderCard() {
    MaterialTheme {
        // Mocking colors locally for the preview so it doesn't break if
        // PulseStatusColors isn't accessible in the preview environment.
        val mockBullishText = Color(0xFF4CAF50) // Standard Green
        val mockBullishBg = Color(0xFF1B2E20)   // Dark Green Background
        val mockSurface = Color(0xFF1E1E1E)     // Dark Surface

        UnifiedScoreHeaderCard(
            title = "Market Action",
            score = 72,
            previousScore = 65,
            isHigherBetter = true,
            scoreLabel = "Action Score",
            ringColor = mockBullishText,
            headerBgColor = mockBullishBg,
            pills = listOf(
                StatusPillState(
                    text = "ACCUMULATE",
                    textColor = mockBullishText,
                    iconRes = null // No icon for this one
                ),
                StatusPillState(
                    text = "UPTREND",
                    textColor = mockBullishText,
                    iconRes = R.drawable.ic_trending_up // Assumes this icon exists from your Risk Radar!
                )
            ),
            summaryText = "Market is cooling off. Good zone for dollar-cost averaging. Strong underlying momentum persists despite minor sector rotations.",
            onClick = {}
        )
    }
}