package com.marketlabs.pulse.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.marketlabs.pulse.R
import com.marketlabs.pulse.ui.components.widgets.ChangeDirection
import com.marketlabs.pulse.ui.components.widgets.DirectionalChangePill
import com.marketlabs.pulse.ui.components.widgets.FavoriteStarToggle
import com.marketlabs.pulse.ui.components.widgets.SignalPill
import com.marketlabs.pulse.ui.theme.LocalPulseColors
import com.marketlabs.pulse.ui.theme.MarketPulseTheme
import com.marketlabs.pulse.ui.theme.pillColor
import com.marketlabs.pulse.ui.theme.textColor
import com.marketlabs.pulse.utils.enums.SignalColor
import java.text.SimpleDateFormat
import java.util.Locale

// ==========================================
// 🧩 UNIFIED UI COMPONENTS
// ==========================================

/**
 * `isFavorite`/`onFavoriteClick` default to false/null so every existing call site (including the
 * dead files under the `tabs` package still referencing this composable) keeps compiling without
 * passing them -- the star only renders when a real `onFavoriteClick` is supplied, bottom-right of
 * the card (last element, after the optional signal badge/release date).
 */
@Composable
fun UniversalMetricCard(
    title: String,
    value: String?,
    changeString: String? = null,
    signalText: String? = null,
    signalColor: SignalColor? = null,
    dateString: String? = null,
    isFavorite: Boolean = false,
    onFavoriteClick: (() -> Unit)? = null,
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

            // 💡 Everything below the header lives in this `weight(1f)` box -- when a row's sibling
            // card is taller (a 2-line title, or a title/value combo that just wraps more), each
            // `UniversalMetricCard` in that row is stretched to match it (`IntrinsicSize.Max` +
            // `fillMaxHeight()` at the call site). That extra height has to go somewhere; it used to
            // go into a `Spacer(weight(1f))` sitting *between* the value row and the signal badge,
            // which visibly shoved the badge/date down toward the bottom of the taller card instead
            // of leaving it sitting right under the value. Now the value/badge/date block is a plain
            // top-aligned `Column` (no internal weight spacer of its own) inside this one shared box,
            // so it always reads as one compact group regardless of how much extra height the row
            // stretch adds -- the leftover space collects below/around it inside the box instead.
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Value Row (Number + Change)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = value ?: notAvail,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        // 💡 The change percent used to be plain colored text -- direction was only
                        // implied by the sign character. A pill with a triangle makes direction read
                        // as a shape, not just a color, at a glance on a dense grid of cards. Since
                        // the triangle already carries the sign, the leading "+"/"-" is stripped from
                        // the caller's pre-formatted string so direction is not stated twice. An exact
                        // 0% reading gets the neutral signal tone and a flat-bar icon instead of
                        // either triangle -- it did not move, so it should not visually claim it went
                        // up or down.
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
                            // 💡 A plain Spacer for the gap, not padding baked into the pill's own
                            // modifier -- a bottom-padded modifier on a `Alignment.Bottom` row shifts
                            // the pill's own visible content up off the value text's baseline by that
                            // padding amount, which is what made the pill look raised out of line with
                            // the value. Centering the row instead (matching AssetDetailScreen's
                            // price-row + pill pattern) keeps both aligned regardless of the pill's
                            // own height.
                            Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_medium)))
                            DirectionalChangePill(
                                changeText = displayChange,
                                direction = direction,
                                pillColor = if (isFlat) pulseColors.signalNeutralPill else signalColor.pillColor,
                                contentColor = if (isFlat) pulseColors.signalNeutralText else baseColor
                            )
                        }
                    }

                    // Optional Signal Badge -- right after the value now, fixed gap, never pushed by leftover stretch height.
                    if (signalText != null) {
                        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))
                        SignalPill(
                            text = signalText.uppercase(),
                            pillColor = signalColor.pillColor,
                            contentColor = baseColor
                        )
                    }

                    // 💡 Safely parses and displays the Release Date
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

                // 💡 Vertically centered in the leftover space this box actually has (natural
                // content height on an unstretched card; real extra room on one stretched to match a
                // taller sibling), right-aligned -- never the header row above (a separate, untouched
                // sibling in the outer Column, so the chevron is never at risk), and clear of the
                // value/badge/date text above since those are left-anchored, not full-width.
                if (onFavoriteClick != null) {
                    FavoriteStarToggle(
                        isFavorite = isFavorite,
                        onClick = onFavoriteClick,
                        contentDescription = stringResource(
                            id = if (isFavorite) R.string.stock_analysis_remove_favorite_content_description
                            else R.string.stock_analysis_add_favorite_content_description,
                            title
                        ),
                        key = title,
                        modifier = Modifier.align(Alignment.CenterEnd)
                    )
                }
            }
        }
    }
}

@Preview(name = "Light", showBackground = true)
@Composable
private fun PreviewUniversalMetricCard() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        UniversalMetricCard(
            title = "CPI YoY",
            value = "3.1%",
            changeString = "-0.2%",
            signalText = "Cooling",
            signalColor = SignalColor.GREEN,
            dateString = "2026-08-01",
            onClick = {}
        )
    }
}

@Preview(name = "Dark", showBackground = true, backgroundColor = 0xFF0D0E12)
@Composable
private fun PreviewUniversalMetricCardDark() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        UniversalMetricCard(
            title = "Unemployment Rate",
            value = "4.6%",
            changeString = "+0.3%",
            signalText = "Weakening",
            signalColor = SignalColor.RED,
            dateString = "2026-08-01",
            onClick = {}
        )
    }
}