package com.marketlabs.pulse.ui.screens.insights.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.marketlabs.pulse.R
import com.marketlabs.pulse.storage.model.pastReleases.PastRelease
import com.marketlabs.pulse.storage.model.pastReleases.PastReleases
import com.marketlabs.pulse.ui.components.DisclaimerFooter
import com.marketlabs.pulse.ui.components.PulseCard
import com.marketlabs.pulse.ui.components.PulseCardStyle
import com.marketlabs.pulse.ui.theme.LocalPulseColors
import com.marketlabs.pulse.ui.theme.MarketPulseTheme
import com.marketlabs.pulse.utils.extensions.toTodayOrYesterdayLabel
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Same "pushed destination, own page" list/subtitle/footer shell as Indicators' Horizons -- see
 * IndicatorHorizonsScreen.kt -- but each card is the normal DATA-style card (`WeeklyEventCard`'s
 * own look), not Horizon's SYNTHESIS treatment, since a past release is a plain data reading, not
 * an AI verdict. Reached via PastReleasesRoute.kt from the Past Releases entry card placed right
 * after the Digest in WeeklyPlaybookView.kt.
 */
@Composable
fun PastReleasesScreen(
    pastReleases: PastReleases,
    innerPadding: PaddingValues
) {
    val paddingLarge = dimensionResource(id = R.dimen.padding_large)
    val releases = remember(pastReleases) {
        pastReleases.releases?.values?.sortedByDescending { it.resolvedAtSortKey() } ?: emptyList()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        contentPadding = PaddingValues(
            start = paddingLarge,
            end = paddingLarge,
            top = paddingLarge,
            bottom = paddingLarge
        ),
        verticalArrangement = Arrangement.spacedBy(paddingLarge)
    ) {
        item {
            Text(
                text = stringResource(id = R.string.past_releases_description),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        items(releases, key = { it.id }) { release -> PastReleaseCard(release = release) }
        item { DisclaimerFooter(showAiDisclosure = true) }
    }
}

/**
 * Same PulseCard(style = DATA)/divider/boxed-impact treatment as `WeeklyEventCard`
 * (WeeklyPlaybookView.kt) -- a past release is a plain data reading, not an AI verdict, so it gets
 * this app's normal data-card look rather than a SYNTHESIS-style card. Date only (no time-of-day)
 * and Estimate/Actual only (no Previous column), unlike `WeeklyEventCard`.
 */
@Composable
private fun PastReleaseCard(release: PastRelease) {
    PulseCard(
        style = PulseCardStyle.DATA,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))) {

            Text(
                text = release.label ?: stringResource(id = R.string.unknown_event),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth()
            )

            release.date?.let { rawDate ->
                val formattedDate = remember(rawDate) { formatReleaseDateOnly(rawDate) }

                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))

                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))

            val showEstimate = isReleaseValueAvailable(release.estimate)
            val showActual = isReleaseValueAvailable(release.actual)

            if (showEstimate || showActual) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_large))
                ) {
                    if (showEstimate) {
                        ReleaseDataColumn(
                            label = stringResource(id = R.string.label_estimate),
                            value = release.estimate!!
                        )
                    }
                    if (showActual) {
                        ReleaseDataColumn(
                            label = stringResource(id = R.string.label_actual),
                            value = release.actual!!,
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

            val showImpact = isReleaseValueAvailable(release.postReleaseImpact)

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
                                text = release.postReleaseImpact ?: "",
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
private fun ReleaseDataColumn(label: String, value: String, isActual: Boolean = false) {
    Column {
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

/**
 * Date only, no time-of-day. Past-release dates always carry a timezone offset (e.g.
 * "2026-09-11T08:30:00-04:00" -- see the backend's pastReleases readme), so this still parses
 * with the offset pattern first (to land on the correct calendar day for the reader's own
 * timezone, since a late-day ET release can fall on a different date elsewhere) before falling
 * back to the offset-less/date-only patterns.
 */
private fun formatReleaseDateOnly(rawDate: String): String {
    val parsed = if (rawDate.contains("T")) {
        try {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US).parse(rawDate)
        } catch (e: ParseException) {
            try {
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).parse(rawDate)
            } catch (e: ParseException) {
                null
            }
        }
    } else {
        try {
            SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(rawDate)
        } catch (e: ParseException) {
            null
        }
    }

    return if (parsed != null) {
        parsed.toTodayOrYesterdayLabel() ?: SimpleDateFormat("EEEE, MMM dd", Locale.getDefault()).format(parsed)
    } else {
        rawDate
    }
}

/** Epoch millis of [PastRelease.date] for sort ordering (most recent release first), falling back
 *  to [PastRelease.resolvedAt] if the date can't be parsed, then to 0 so an unparseable release
 *  sorts last rather than crashing the sort. */
private fun PastRelease.resolvedAtSortKey(): Long {
    val rawDate = this.date
    if (rawDate != null) {
        val parsed = try {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US).parse(rawDate)
        } catch (e: ParseException) {
            try {
                SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(rawDate)
            } catch (e: ParseException) {
                null
            }
        }
        if (parsed != null) return parsed.time
    }
    return this.resolvedAt ?: 0L
}

private fun isReleaseValueAvailable(value: String?): Boolean {
    if (value.isNullOrBlank()) return false
    val normalized = value.trim().lowercase(Locale.getDefault())
    return normalized !in listOf("n/a", "--", "null", "none", "unknown", "")
}

// ============================================================================
// 🎨 PREVIEWS
// ============================================================================
@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun PreviewPastReleasesScreen() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        val mockRelease = PastRelease(
            id = "cpi_mm",
            label = "CPI (m/m)",
            feedTitle = "CPI m/m",
            date = "2026-09-11T08:30:00-04:00",
            estimate = "0.4%",
            previous = "0.1%",
            actual = "0.4%",
            actualConfirmed = true,
            postReleaseImpact = "In line with estimates, keeping the disinflation narrative intact.",
            resolvedAt = 1757606400000
        )

        PastReleasesScreen(
            pastReleases = PastReleases(releases = mapOf(mockRelease.id to mockRelease)),
            innerPadding = PaddingValues(0.dp)
        )
    }
}
