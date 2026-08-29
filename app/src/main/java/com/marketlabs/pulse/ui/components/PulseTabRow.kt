package com.marketlabs.pulse.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.marketlabs.pulse.R
import com.marketlabs.pulse.ui.theme.LocalPulseColors
import com.marketlabs.pulse.ui.theme.MarketPulseTheme

/**
 * The one tab-bar design for every screen in this app that switches between a handful of sibling
 * sections -- a horizontally-scrolling row of segmented-control-style chips: a solid `accentPrimary`
 * fill (`accentOn` text) for the selected chip, an outlined `accentSurfaceBorder` hairline border
 * (`onSurfaceMuted` text) for the rest, `corner_radius_small` shape. `horizontalScroll` rather than
 * a fixed-width `Row` since chip width follows each label's own content instead of splitting the
 * row into N equal columns -- appropriate for tab labels that vary a lot in length (e.g.
 * "Technicals" vs "News", or "Positioning" vs "Risks").
 *
 * Established on the Stock Analysis detail screen (was `StockDetailRoute`'s private
 * `DetailPillTabRow`) and pulled out here once Insights needed the identical pattern, so every
 * future screen with page-level tabs reaches for this instead of hand-rolling a third
 * near-duplicate. `ChartRangePicker` shares this same visual language (fill/outline treatment,
 * `corner_radius_small`, `labelMedium` bold) but deliberately stays its own component -- it's an
 * evenly-weighted range selector (`Modifier.weight(1f)` per button, no scrolling), a different
 * layout shape than tabs that size to their own label content, not a page-tab bar itself.
 *
 * Pattern for callers (see `StockDetailViewModel`/`StockDetailScreen` or
 * `InsightsViewModel`/`InsightsScreen` for full worked examples): define a per-screen `enum class
 * XTab(val labelRes: Int)`, keep the selected index as a `MutableStateFlow<Int>` in the ViewModel
 * with an `onTabSelected(index: Int)` setter, pass `XTab.entries.map { stringResource(it.labelRes) }`
 * as `tabs` here, and branch the screen's content on `XTab.entries[selectedTabIndex]`.
 */
@Composable
fun PulseTabRow(
    tabs: List<String>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val pulseColors = LocalPulseColors.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(
                horizontal = dimensionResource(id = R.dimen.padding_large),
                vertical = dimensionResource(id = R.dimen.padding_medium)
            ),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_small))
    ) {
        tabs.forEachIndexed { index, label ->
            val isSelected = index == selectedTabIndex
            Surface(
                color = if (isSelected) pulseColors.accentPrimary else MaterialTheme.colorScheme.background,
                border = if (isSelected) null else BorderStroke(dimensionResource(id = R.dimen.border_thin), pulseColors.accentSurfaceBorder),
                shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_small)),
                modifier = Modifier.clickable { onTabSelected(index) }
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (isSelected) pulseColors.accentOn else pulseColors.onSurfaceMuted,
                    modifier = Modifier.padding(
                        horizontal = dimensionResource(id = R.dimen.padding_large),
                        vertical = dimensionResource(id = R.dimen.padding_medium)
                    )
                )
            }
        }
    }
}

// ============================================================================
// 🎨 PREVIEWS
// ============================================================================

@Preview(name = "Light", showBackground = true)
@Composable
private fun PreviewPulseTabRowLight() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        PulseTabRow(tabs = listOf("Playbook", "Risks", "Posture", "Positioning"), selectedTabIndex = 0, onTabSelected = {})
    }
}

@Preview(name = "Dark", showBackground = true, backgroundColor = 0xFF0D0E12)
@Composable
private fun PreviewPulseTabRowDark() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        PulseTabRow(tabs = listOf("Technicals", "Fundamentals", "Thesis", "Timeline", "News"), selectedTabIndex = 2, onTabSelected = {})
    }
}
