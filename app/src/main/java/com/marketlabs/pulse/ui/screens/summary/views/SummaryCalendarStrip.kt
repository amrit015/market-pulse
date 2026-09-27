package com.marketlabs.pulse.ui.screens.summary.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.marketlabs.pulse.R
import com.marketlabs.pulse.ui.theme.LocalPulseColors
import com.marketlabs.pulse.ui.theme.MarketPulseTheme
import com.marketlabs.pulse.utils.getLastNDateIds
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

private const val SelectionAnimationMs = 250

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
 *
 * The selected pill's highlight is one shared box that slides between days rather than each pill
 * toggling its own background -- same `PulseTabRow` treatment, adapted for this row's evenly
 * `weight(1f)`-split (not content-sized) pills: [selectionPosition] is the fractional day index it
 * sits at, so a caller with a swipeable day pager (`MarketSummaryScreen`) passes
 * `pagerState.currentPage + pagerState.currentPageOffsetFraction` and the highlight (and each
 * day's text color) tracks the swipe finger-for-finger instead of waiting for it to settle; a
 * caller without a pager leaves it `null` and the highlight animates to [selectedDateId] on its
 * own, so a tap slides it too. [selectedDateId] still drives everything else (which day reports as
 * selected, the haptic tick on change).
 */
@Composable
fun SummaryCalendarStrip(
    dayIds: List<String>,
    selectedDateId: String,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    selectionPosition: (() -> Float)? = null
) {
    val pulseColors = LocalPulseColors.current
    val paddingTiny = dimensionResource(id = R.dimen.padding_tiny)
    val paddingMedium = dimensionResource(id = R.dimen.padding_medium)
    val paddingLarge = dimensionResource(id = R.dimen.padding_large)

    val selectedIndex = dayIds.indexOf(selectedDateId).coerceAtLeast(0)

    val haptic = LocalHapticFeedback.current
    // Seeded from the current index rather than 0 so opening the screen on a non-first day doesn't
    // fire a haptic tick on first composition -- same guard `PulseTabRow` uses.
    var previousSelectedIndex by remember { mutableIntStateOf(selectedIndex) }
    LaunchedEffect(selectedIndex) {
        if (selectedIndex != previousSelectedIndex) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
        previousSelectedIndex = selectedIndex
    }

    val animatedPosition by animateFloatAsState(
        targetValue = selectedIndex.toFloat(),
        animationSpec = tween(SelectionAnimationMs),
        label = "calendar_strip_selection_position"
    )
    val position = { (selectionPosition?.invoke() ?: animatedPosition).coerceIn(0f, (dayIds.size - 1).coerceAtLeast(0).toFloat()) }

    // Each pill's bounds inside the row, measured after layout -- the sliding highlight needs them
    // to know where to sit and how wide to be.
    val pillBounds = remember(dayIds.size) { mutableStateMapOf<Int, Rect>() }
    val boundsReady = pillBounds.size == dayIds.size
    val shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_small))

    Column(modifier = modifier.fillMaxWidth()) {

        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
            thickness = dimensionResource(id = R.dimen.border_thin)
        )

        Spacer(modifier = Modifier.height(paddingMedium))

        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = paddingLarge)) {
            if (boundsReady) {
                // Drawn behind the pills (which are transparent) so their text sits on top of it.
                Box(
                    modifier = Modifier
                        .offset {
                            val (left, _) = interpolatedBounds(pillBounds, position())
                            IntOffset(left.roundToInt(), pillBounds.getValue(0).top.roundToInt())
                        }
                        .layout { measurable, constraints ->
                            val (_, width) = interpolatedBounds(pillBounds, position())
                            val height = pillBounds.getValue(0).height.roundToInt()
                            val placeable = measurable.measure(Constraints.fixed(width.roundToInt().coerceAtLeast(0), height))
                            layout(placeable.width, placeable.height) { placeable.place(0, 0) }
                        }
                        .clip(shape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = .2f))
                )
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                dayIds.forEachIndexed { index, dateId ->
                    val date = remember(dateId) { LocalDate.parse(dateId) }
                    // How selected this pill looks right now: 1 when the highlight sits exactly on
                    // it, fading to 0 one day away -- tracks the swipe/animation frame by frame.
                    val selectedness = (1f - abs(position() - index)).coerceIn(0f, 1f)
                    val dayColor = lerp(pulseColors.onSurfaceMuted, MaterialTheme.colorScheme.onSurface, selectedness)

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = paddingTiny)
                            .onGloballyPositioned { coordinates ->
                                val pos = coordinates.positionInParent()
                                val bounds = Rect(pos.x, pos.y, pos.x + coordinates.size.width, pos.y + coordinates.size.height)
                                if (pillBounds[index] != bounds) pillBounds[index] = bounds
                            }
                            .clip(shape)
                            .clickable { onDateSelected(dateId) }
                            .padding(vertical = paddingMedium),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = date.dayOfMonth.toString(),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = dayColor
                        )
                        Text(
                            text = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.US),
                            style = MaterialTheme.typography.labelSmall,
                            color = pulseColors.onSurfaceMuted
                        )
                    }
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

/** Left edge and width of the highlight at fractional index [position], blended between the two pills it sits between. */
private fun interpolatedBounds(pillBounds: Map<Int, Rect>, position: Float): Pair<Float, Float> {
    val lower = position.toInt().coerceIn(0, pillBounds.size - 1)
    val upper = (lower + 1).coerceAtMost(pillBounds.size - 1)
    val fraction = (position - lower).coerceIn(0f, 1f)
    val from = pillBounds.getValue(lower)
    val to = pillBounds.getValue(upper)
    return (from.left + (to.left - from.left) * fraction) to (from.width + (to.width - from.width) * fraction)
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
