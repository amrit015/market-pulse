package com.marketlabs.pulse.ui.screens.dashboard.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.marketlabs.pulse.R
import com.marketlabs.pulse.storage.model.weeklyPlaybook.WeeklyEvent
import com.marketlabs.pulse.storage.model.weeklyPlaybook.WeeklyPlaybook

@Composable
fun WeeklyPlaybookSection(playbook: WeeklyPlaybook) {
    if (playbook.events.isNullOrEmpty()) return

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_large))
    ) {
        // Section Title
        Text(
            text = stringResource(id = R.string.section_weekly_playbook),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.padding_medium))
        )

        playbook.events.forEach { event ->
            WeeklyEventCard(event)
        }
    }
}

@Composable
fun WeeklyEventCard(event: WeeklyEvent) {
    Card(
        colors = CardDefaults.cardColors(
            // 💡 Matches the Summary Screen / Tail Risks card backgrounds
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_card)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))) {

            // 1. Header Row (Title & Date Pill)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = event.eventName ?: stringResource(id = R.string.unknown_event),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f).padding(end = dimensionResource(id = R.dimen.padding_medium))
                )

                event.date?.let { dateString ->
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_chip))
                    ) {
                        Text(
                            text = dateString,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(
                                horizontal = dimensionResource(id = R.dimen.padding_medium),
                                vertical = dimensionResource(id = R.dimen.padding_tiny)
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))

            // 2. Data Row (Estimate & Previous)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_large))
            ) {
                EventDataColumn(
                    label = stringResource(id = R.string.label_estimate),
                    value = event.estimate ?: "--"
                )
                EventDataColumn(
                    label = stringResource(id = R.string.label_previous),
                    value = event.previous ?: "--"
                )
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