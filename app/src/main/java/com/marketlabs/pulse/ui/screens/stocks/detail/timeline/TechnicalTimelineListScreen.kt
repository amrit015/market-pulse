package com.marketlabs.pulse.ui.screens.stocks.detail.timeline

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.marketlabs.pulse.R
import com.marketlabs.pulse.storage.model.stocks.DomainEventLogItem
import com.marketlabs.pulse.ui.common.UiError
import com.marketlabs.pulse.ui.components.DisclaimerFooter
import com.marketlabs.pulse.ui.components.PulseCard
import com.marketlabs.pulse.ui.components.PulseCardStyle
import com.marketlabs.pulse.ui.components.PulseErrorState
import com.marketlabs.pulse.ui.components.PulseLoadingIndicator
import com.marketlabs.pulse.ui.components.uiErrorMessage
import com.marketlabs.pulse.ui.screens.stocks.detail.DataCardSectionHeader
import com.marketlabs.pulse.ui.screens.stocks.detail.sections.EventRow
import com.marketlabs.pulse.ui.theme.LocalPulseColors
import com.marketlabs.pulse.ui.theme.MarketPulseTheme

/**
 * Stateless. Same `PulseCard(DATA)` + [DataCardSectionHeader] + divider-separated [EventRow]s as
 * the capped-to-7 card on Stock Detail's Timeline tab -- just the full, unclipped (still
 * "last 30 days", per the backend's own window) list, reusing that same row composable.
 */
@Composable
fun TechnicalTimelineListScreen(
    events: List<DomainEventLogItem>,
    isLoading: Boolean,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    error: UiError? = null,
    onRetry: () -> Unit = {}
) {
    if (events.isEmpty()) {
        if (error != null && !isLoading) {
            PulseErrorState(message = uiErrorMessage(error), onRetry = onRetry, modifier = modifier)
            return
        }
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = if (isLoading) "" else stringResource(id = R.string.stock_detail_tab_nothing_yet),
                style = MaterialTheme.typography.bodyMedium,
                color = LocalPulseColors.current.onSurfaceMuted
            )
            if (isLoading) {
                PulseLoadingIndicator()
            }
        }
        return
    }

    // 💡 padding_extra_large gap before the footer -- same value every other screen's
    // DisclaimerFooter sits below (GlossaryDetailScreen/MetricDetailScreen/Dashboard/AssetDetail);
    // this LazyColumn only ever has the one content item plus the footer, so spacedBy here affects
    // just that one gap, not overall list density.
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_extra_large))
    ) {
        item {
            PulseCard(style = PulseCardStyle.DATA, modifier = Modifier.fillMaxWidth()) {
                Column {
                    DataCardSectionHeader(
                        title = stringResource(id = R.string.stock_detail_technical_timeline_title),
                        subtitle = stringResource(id = R.string.stock_detail_technical_timeline_subtitle)
                    )
                    // 💡 `animateContentSize()` here, not on `PulseCard`'s own outer modifier -- see
                    // `PulseCard`'s shadow/clip gotcha. Smooths the card's height when a refresh
                    // changes how many timeline events there are, instead of it snapping to the new size.
                    Column(modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large)).animateContentSize()) {
                        events.forEachIndexed { index, event ->
                            EventRow(event)
                            if (index != events.lastIndex) {
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                    thickness = dimensionResource(id = R.dimen.border_thin),
                                    modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.padding_medium))
                                )
                            }
                        }
                    }
                }
            }
        }
        item { DisclaimerFooter() }
    }
}

// ============================================================================
// 🎨 PREVIEWS
// ============================================================================

private val mockEvents = listOf(
    DomainEventLogItem(date = "2026-08-04", direction = "BULLISH", kind = "52W_HIGH", text = "New 52-week high at \$287.2."),
    DomainEventLogItem(date = "2026-08-04", direction = "BEARISH", kind = "RSI", text = "RSI moved from overbought to strong at 67."),
    DomainEventLogItem(date = "2026-08-03", direction = "BEARISH", kind = "RSI", text = "RSI became overbought at 72.2, hitting a high of \$287.16.")
)

@Preview(name = "Light", showBackground = true)
@Composable
private fun PreviewTechnicalTimelineListScreen() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        TechnicalTimelineListScreen(
            events = mockEvents,
            isLoading = false,
            contentPadding = PaddingValues(dimensionResource(id = R.dimen.padding_large))
        )
    }
}
