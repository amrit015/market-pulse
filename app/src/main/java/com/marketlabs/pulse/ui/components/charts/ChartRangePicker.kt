package com.marketlabs.pulse.ui.components.charts

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import com.marketlabs.pulse.storage.model.charts.ChartRange
import com.marketlabs.pulse.ui.theme.LocalPulseColors
import com.marketlabs.pulse.ui.theme.MarketPulseTheme

/**
 * 1D/5D/1M/6M/YTD/1Y range picker for [PeriodChart] — same selected-fill / unselected-outline
 * segmented-control look `StockDetailRoute`'s `DetailPillTabRow` already established, so a chart's
 * range picker and the detail screen's tab bar read as the same visual language rather than two
 * different chip styles competing on one screen.
 *
 * [availableRanges] defaults to every range, but callers whose symbol has no live intraday feed
 * (dashboard tiles outside `DashboardIntradayEligibility`'s set -- VIX, futures, sentiment) pass a
 * list with [ChartRange.ONE_DAY] filtered out, since there'd be nothing for it to show.
 */
@Composable
fun ChartRangePicker(
    selectedRange: ChartRange,
    onRangeSelected: (ChartRange) -> Unit,
    modifier: Modifier = Modifier,
    availableRanges: List<ChartRange> = ChartRange.entries
) {
    val pulseColors = LocalPulseColors.current

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_small))
    ) {
        availableRanges.forEach { range ->
            val isSelected = range == selectedRange
            Surface(
                color = if (isSelected) pulseColors.accentPrimary else MaterialTheme.colorScheme.background,
                border = if (isSelected) null else BorderStroke(dimensionResource(id = R.dimen.border_thin), pulseColors.accentSurfaceBorder),
                shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_small)),
                modifier = Modifier
                    .weight(1f)
                    .clickable { onRangeSelected(range) }
            ) {
                Text(
                    text = range.rangeKey,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (isSelected) pulseColors.accentOn else pulseColors.onSurfaceMuted,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = dimensionResource(id = R.dimen.padding_medium))
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
private fun PreviewChartRangePickerLight() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        ChartRangePicker(selectedRange = ChartRange.FIVE_DAY, onRangeSelected = {})
    }
}

@Preview(name = "Dark", showBackground = true, backgroundColor = 0xFF0D0E12)
@Composable
private fun PreviewChartRangePickerDark() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        ChartRangePicker(selectedRange = ChartRange.ONE_YEAR, onRangeSelected = {})
    }
}
