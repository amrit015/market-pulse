package com.marketlabs.pulse.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.marketlabs.pulse.R
import com.marketlabs.pulse.ui.components.widgets.ChangeDirection
import com.marketlabs.pulse.ui.components.widgets.DirectionalChangePill
import com.marketlabs.pulse.ui.components.widgets.SignalPill
import com.marketlabs.pulse.ui.theme.LocalPulseColors
import com.marketlabs.pulse.ui.theme.pillColor
import com.marketlabs.pulse.ui.theme.textColor
import com.marketlabs.pulse.utils.enums.SignalColor
import com.marketlabs.pulse.utils.glossary.PillarGuide
import java.text.SimpleDateFormat
import java.util.Locale

// ==========================================
// 🧩 UNIFIED UI COMPONENTS
// ==========================================

@Composable
fun ContextHeaderCard(guide: PillarGuide) {
    var expanded by remember { mutableStateOf(false) }

    // 💡 Explanatory/glossary content, not raw data -- SYNTHESIS, same as the AI briefing and news
    // cards, replacing the old `surfaceVariant.copy(alpha = 0.5f)` leftover from before this app
    // had its own token system.
    PulseCard(
        style = PulseCardStyle.SYNTHESIS,
        modifier = Modifier.fillMaxWidth(),
        onClick = { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large)).animateContentSize()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = guide.timeframe,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = LocalPulseColors.current.accentPrimary
                )
                Icon(
                    painter = painterResource(id = if (expanded) R.drawable.ic_arrow_up else R.drawable.ic_arrow_down),
                    contentDescription = "Toggle Description",
                    tint = LocalPulseColors.current.accentPrimary,
                    modifier = Modifier.size(dimensionResource(R.dimen.padding_large))
                )
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_tiny)))
                Text(
                    text = guide.purpose,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))
                // 💡 Explainer text is always onSurface, matching the title/value above -- was
                // `onSurfaceVariant` (muted), reserved for genuine metadata like dates, not
                // descriptive content.
                Text(
                    text = guide.howToUse,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun UniversalMetricCard(
    title: String,
    value: String?,
    changeString: String? = null,
    signalText: String? = null,
    signalColor: SignalColor? = null,
    dateString: String? = null,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    // 💡 Price cards are uniform/non-directional now (an accent-washed neutral, regardless of
    // signal) -- direction lives only in the sparkline stroke and the pill below, both keyed off
    // signalColor.textColor/.pillColor. A grid of price cards used to flip its own background
    // color per card based on direction, which made a down day look alarming at a glance; keeping
    // the container neutral and the direction in smaller details reads calmer.
    //
    // DATA style -- the same outer shell Equities' price cards use (AssetCard in
    // DashboardScreen.kt), so a change to how price cards look updates both at once.
    val baseColor = signalColor.textColor
    val notAvail = stringResource(id = R.string.not_available_short)

    PulseCard(
        style = PulseCardStyle.DATA,
        modifier = modifier,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .padding(dimensionResource(id = R.dimen.padding_large))
                .fillMaxHeight()
        ) {
            // Header Row (Title + Chevron)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    // 💡 Card titles are always onSurface (dark-on-light/white-on-dark) across
                    // this app -- was `onSurfaceVariant` (muted), the one card whose title read
                    // grey next to Equities' AssetCard title using the same role.
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    painter = painterResource(id = R.drawable.ic_chevron_forward),
                    contentDescription = "View Details",
                    tint = LocalPulseColors.current.accentPrimary,
                    modifier = Modifier.size(dimensionResource(R.dimen.padding_large)).padding(start = dimensionResource(R.dimen.padding_small))
                )
            }

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))

            // Value Row (Number + Change)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value ?: notAvail,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                // 💡 The change percent used to be plain colored text -- direction was only implied
                // by the sign character. A pill with a triangle makes direction read as a shape, not
                // just a color, at a glance on a dense grid of cards. Since the triangle already
                // carries the sign, the leading "+"/"-" is stripped from the caller's pre-formatted
                // string so direction is not stated twice. An exact 0% reading gets the neutral
                // signal tone and a flat-bar icon instead of either triangle -- it did not move, so
                // it should not visually claim it went up or down.
                if (!changeString.isNullOrBlank()) {
                    val trimmedChange = changeString.trim()
                    val isNegative = trimmedChange.startsWith("-")
                    val displayChange = trimmedChange.removePrefix("+").removePrefix("-")
                    val numericChange = displayChange.trimEnd('%').toDoubleOrNull()
                    val isFlat = numericChange == 0.0
                    val direction = when {
                        isFlat -> ChangeDirection.FLAT
                        isNegative -> ChangeDirection.DOWN
                        else -> ChangeDirection.UP
                    }
                    val pulseColors = LocalPulseColors.current
                    // 💡 Gap from the value text bumped from `padding_small` to `padding_medium` --
                    // the pill sitting almost flush against the value read cramped once it grew.
                    DirectionalChangePill(
                        changeText = displayChange,
                        direction = direction,
                        pillColor = if (isFlat) pulseColors.signalNeutralPill else signalColor.pillColor,
                        contentColor = if (isFlat) pulseColors.signalNeutralText else baseColor,
                        modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.padding_micro), start = dimensionResource(id = R.dimen.padding_medium))
                    )
                }
            }

            // Pushes bottom elements down
            Spacer(modifier = Modifier.weight(1f))

            // Optional Signal Badge
            if (signalText != null) {
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))
                SignalPill(
                    text = signalText.uppercase(),
                    pillColor = signalColor.pillColor,
                    contentColor = baseColor
                )
            }

            // 💡 NEW: Safely parses and displays the Release Date
            if (!dateString.isNullOrBlank()) {
                val formattedDate = try {
                    val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(dateString)
                    SimpleDateFormat("MMM yyyy", Locale.US).format(parsed!!)
                } catch (e: Exception) {
                    dateString
                }

                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))
                Text(
                    text = "Released: $formattedDate",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}