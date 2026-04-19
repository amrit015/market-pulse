package com.marketlabs.pulse.ui.screens.dashboard.views

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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.marketlabs.pulse.R
import com.marketlabs.pulse.storage.model.weeklyPlaybook.WeeklyEvent
import com.marketlabs.pulse.storage.model.weeklyPlaybook.WeeklyPlaybook
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun WeeklyPlaybookSection(playbook: WeeklyPlaybook) {
    if (playbook.events.isNullOrEmpty()) return

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_medium))
    ) {

        Row(verticalAlignment = Alignment.CenterVertically) {
            // Dynamically match icon to text size
            val textStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            val iconSize = with(LocalDensity.current) { textStyle.fontSize.toDp() }

            Icon(
                painter = painterResource(id = R.drawable.ic_engine_ai_sparkles),
                contentDescription = "Analysis Engine",
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(iconSize)
            )

            Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_small)))

            // Section Title
            Text(
                text = stringResource(id = R.string.section_weekly_playbook),
                style = textStyle,
                color = MaterialTheme.colorScheme.primary
            )
        }

        playbook.events.forEach { event ->
            WeeklyEventCard(event)
        }
    }
}

@Composable
fun WeeklyEventCard(event: WeeklyEvent) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_card)),
        modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.padding_medium))
    ) {
        Column(modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))) {

            // 1. Header (Title & Formatted Date Below)
            Text(
                text = event.eventName ?: stringResource(id = R.string.unknown_event),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.fillMaxWidth()
            )

            event.date?.let { rawDate ->
                val formattedDate = remember(rawDate) { formatEventDateSafe(rawDate) }

                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))

                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))

            // 2. Data Row (Estimate & Previous horizontally)
            val showEstimate = isValueAvailable(event.estimate)
            val showPrevious = isValueAvailable(event.previous)

            if (showEstimate || showPrevious) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_large))
                ) {
                    if (showEstimate) {
                        EventDataColumn(
                            label = stringResource(id = R.string.label_estimate),
                            value = event.estimate!!
                        )
                    }

                    if (showPrevious) {
                        EventDataColumn(
                            label = stringResource(id = R.string.label_previous),
                            value = event.previous!!
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))
            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                thickness = dimensionResource(id = R.dimen.border_thin)
            )
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))

            // 3. Market Context
            Text(
                text = event.marketContext ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun EventDataColumn(label: String, value: String) {
    Column {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Safely parses API date formats into readable strings.
 * Falls back to returning the original string if parsing fails.
 */
private fun formatEventDateSafe(rawDate: String): String {
    return try {
        val isIsoWithTime = rawDate.contains("T")
        val parser = if (isIsoWithTime) {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
        } else {
            SimpleDateFormat("yyyy-MM-dd", Locale.US)
        }

        val parsed = parser.parse(rawDate)

        if (parsed != null) {
            val outputFormat = if (isIsoWithTime) {
                SimpleDateFormat("EEEE, MMM dd • h:mm a", Locale.getDefault())
            } else {
                SimpleDateFormat("EEEE, MMM dd", Locale.getDefault())
            }
            outputFormat.format(parsed)
        } else {
            rawDate
        }
    } catch (e: Exception) {
        rawDate
    }
}

/**
 * Helper function to determine if a value from the LLM represents an actual number/string,
 * or if it's just a placeholder indicating missing data.
 */
private fun isValueAvailable(value: String?): Boolean {
    if (value.isNullOrBlank()) return false
    val normalized = value.trim().lowercase(Locale.getDefault())
    return normalized !in listOf("n/a", "--", "null", "none", "unknown", "")
}