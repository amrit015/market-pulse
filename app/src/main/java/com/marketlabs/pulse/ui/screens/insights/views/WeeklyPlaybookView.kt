package com.marketlabs.pulse.ui.screens.insights.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.border
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.marketlabs.pulse.R
import com.marketlabs.pulse.storage.model.weeklyPlaybook.WeeklyEvent
import com.marketlabs.pulse.storage.model.weeklyPlaybook.WeeklyPlaybook
import com.marketlabs.pulse.ui.components.PulseCard
import com.marketlabs.pulse.ui.components.PulseCardStyle
import com.marketlabs.pulse.ui.components.widgets.MetricInfoAction
import com.marketlabs.pulse.ui.theme.LocalPulseColors
import com.marketlabs.pulse.ui.theme.MarketPulseTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WeeklyPlaybookSection(playbook: WeeklyPlaybook) {
    if (playbook.events.isNullOrEmpty()) return

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // 💡 ALREADY PERFECT: Matches TailRisks exactly
        Row(verticalAlignment = Alignment.CenterVertically) {
            val textStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            val iconSize = with(LocalDensity.current) { textStyle.fontSize.toDp() }

            Icon(
                painter = painterResource(id = R.drawable.ic_ai_sparkle_filled),
                contentDescription = "Analysis Engine",
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(iconSize)
            )

            Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_small)))

            Text(
                text = stringResource(id = R.string.section_weekly_playbook),
                style = textStyle,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_small)))

            MetricInfoAction(
                title = stringResource(id = R.string.section_weekly_playbook),
                description = stringResource(id = R.string.weekly_playbook_description)
            )
        }

        val date = playbook.lastUpdated?.let { Date(it) } ?: Date()
        val format = SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault())

        Text(
            text = stringResource(id = R.string.analyzed_at, format.format(date)),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(
                top = dimensionResource(id = R.dimen.padding_micro),
                bottom = dimensionResource(id = R.dimen.padding_large)
            )
        )

        Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_medium)))

        playbook.events.forEach { event ->
            WeeklyEventCard(event)
        }
    }
}

@Composable
fun WeeklyEventCard(event: WeeklyEvent) {
    // 💡 DATA style -- was SYNTHESIS (calendar events with AI-written market context, grouped
    // with the briefing/news cards as "curated content"). This app's darker SYNTHESIS background
    // is now reserved for the one AI briefing/verdict hero card per screen (Dashboard's Technical
    // Briefing, Indicators' AI Executive Briefing, Summary's VerdictCard); an events list reads
    // with the same background every other data-display card in the app uses. Replaces the old
    // `secondaryContainer.copy(alpha = 0.4f)` leftover from before this app had its own token
    // system.
    PulseCard(
        style = PulseCardStyle.DATA,
        modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.padding_medium))
    ) {
        Column(modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))) {

            Text(
                text = event.eventName ?: stringResource(id = R.string.unknown_event),
                // 💡 titleSmall (15sp, semi-bold), not titleMedium.Bold (17sp, bold) -- the
                // consistent title tier every curated/AI-content card title uses now, separate
                // from DATA-style cards (Equities, VIX, Indicators), which keep their bold 17sp
                // title.
                style = MaterialTheme.typography.titleSmall,
                // 💡 Card titles are always onSurface (dark-on-light/white-on-dark) across this
                // app -- same treatment as Equities/AI/News cards.
                color = MaterialTheme.colorScheme.onSurface,
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

            val showEstimate = isValueAvailable(event.estimate)
            val showPrevious = isValueAvailable(event.previous)
            val showActual = isValueAvailable(event.actual)

            if (showEstimate || showPrevious) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_large))
                ) {
                    if (showPrevious) {
                        EventDataColumn(
                            label = stringResource(id = R.string.label_previous),
                            value = event.previous!!
                        )
                    }

                    if (showEstimate) {
                        EventDataColumn(
                            label = stringResource(id = R.string.label_estimate),
                            value = event.estimate!!
                        )
                    }

                    if (showActual) {
                        EventDataColumn(
                            label = stringResource(id = R.string.label_actual),
                            value = event.actual!!,
                            isActual = true
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

            if (isValueAvailable(event.marketContext)) {
                // 💡 Both the label and the explainer text below it are always onSurface -- was
                // `onSurfaceVariant` for the label, and conditionally muted for the text itself
                // once the "Actual" figure came in. Neither is a date, so neither should be grey.
                Text(
                    text = stringResource(id = R.string.label_market_context).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))
                Text(
                    text = event.marketContext ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            val showImpact = isValueAvailable(event.postReleaseImpact)

            AnimatedVisibility(
                visible = showImpact,
                enter = fadeIn() + expandVertically()
            ) {
                Column(modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_medium))) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_small)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = dimensionResource(id = R.dimen.border_thin),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_small))
                            )
                    ) {
                        Column(modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_medium))) {
                            Text(
                                text = stringResource(id = R.string.label_post_release_impact).uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))
                            Text(
                                text = event.postReleaseImpact ?: "",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EventDataColumn(label: String, value: String, isActual: Boolean = false) {
    Column {
        // 💡 Was `onSurfaceVariant` for the non-actual case -- a plain data-column label, not a
        // date, so it follows the same onSurface rule as everything else. The "Actual" case stays
        // signal-colored on purpose (a real market read, not just a label).
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = if (isActual) LocalPulseColors.current.signalBullishText else MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = if (isActual) LocalPulseColors.current.signalBullishText else MaterialTheme.colorScheme.onSurface
        )
    }
}

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

private fun isValueAvailable(value: String?): Boolean {
    if (value.isNullOrBlank()) return false
    val normalized = value.trim().lowercase(Locale.getDefault())
    return normalized !in listOf("n/a", "--", "null", "none", "unknown", "")
}

// ============================================================================
// 🎨 PREVIEWS
// ============================================================================
@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
fun PreviewWeeklyPlaybookSection() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        val mockEvent = WeeklyEvent(
            eventName = stringResource(id = R.string.preview_event_name),
            date = "2026-06-12",
            estimate = "0.3%",
            previous = "0.2%",
            actual = "0.4%",
            marketContext = stringResource(id = R.string.preview_event_context),
            postReleaseImpact = stringResource(id = R.string.preview_event_impact)
        )

        val mockPlaybook = WeeklyPlaybook(
            weekStarting = "2026-06-08",
            events = listOf(mockEvent)
        )

        Column(modifier = Modifier.padding(16.dp)) {
            WeeklyPlaybookSection(playbook = mockPlaybook)
        }
    }
}