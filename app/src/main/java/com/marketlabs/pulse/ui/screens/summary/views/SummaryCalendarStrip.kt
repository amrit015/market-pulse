package com.marketlabs.pulse.ui.screens.summary.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.marketlabs.pulse.R
import com.marketlabs.pulse.ui.theme.LocalPulseColors
import com.marketlabs.pulse.ui.theme.MarketPulseTheme
import com.marketlabs.pulse.utils.getLastNDateIds
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

/**
 * The Summary screen's 7-day calendar strip. `dayIds` are NY-anchored `yyyy-MM-dd` (see
 * `getLastNDateIds`/`marketZone` in `utils/DateExtension.kt`), oldest first, last = today -- the
 * strip's own dates (and which ones are pillable at all) follow NY's calendar, since that's the
 * day the backend's reports are actually keyed by; only the "Today"/"Yesterday" *label* text
 * (`toRelativeDayLabel`, applied where a page renders its own date) switches to the device's local
 * date instead, so a viewer whose local clock hasn't rolled over yet still reads their own current
 * day as "Today" even once NY's newest strip pill has appeared a day ahead of it.
 *
 * Horizontal padding is applied internally (per pill, via `weight(1f)`) rather than by the
 * caller's `modifier` -- `modifier` should carry vertical padding only, so the two dividers span
 * the full width edge-to-edge while the pill row itself stays inset. Each pill gets an equal
 * `weight(1f)` share of the row rather than being sized to its own text -- otherwise the selected
 * pill's highlight box subtly changes width depending on which day's digits/label it's wrapping
 * (e.g. a single-digit vs double-digit date), which reads as an inconsistent, "jumping" marker.
 */
@Composable
fun SummaryCalendarStrip(
    dayIds: List<String>,
    selectedDateId: String,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val pulseColors = LocalPulseColors.current
    val paddingTiny = dimensionResource(id = R.dimen.padding_tiny)
    val paddingMedium = dimensionResource(id = R.dimen.padding_medium)
    val paddingLarge = dimensionResource(id = R.dimen.padding_large)

    Column(modifier = modifier.fillMaxWidth()) {

        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
            thickness = dimensionResource(id = R.dimen.border_thin)
        )

        Spacer(modifier = Modifier.height(paddingMedium))

        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = paddingLarge)) {
            dayIds.forEach { dateId ->
                val isSelected = dateId == selectedDateId
                val date = remember(dateId) { LocalDate.parse(dateId) }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = paddingTiny)
                        .clip(RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_small)))
                        .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = .2f) else Color.Transparent)
                        .clickable { onDateSelected(dateId) }
                        .padding(vertical = paddingMedium),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = date.dayOfMonth.toString(),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (isSelected) MaterialTheme.colorScheme.onSurface else pulseColors.onSurfaceMuted
                    )
                    Text(
                        text = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.US),
                        style = MaterialTheme.typography.labelSmall,
                        color = pulseColors.onSurfaceMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(paddingMedium))

        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
            thickness = dimensionResource(id = R.dimen.border_thin)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SummaryCalendarStripPreview() {
    val days = getLastNDateIds(7)
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        SummaryCalendarStrip(
            dayIds = days,
            selectedDateId = days.last(),
            onDateSelected = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
