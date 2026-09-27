package com.marketlabs.pulse.ui.components.bottomSheet

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.ui.Alignment
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.marketlabs.pulse.R
import com.marketlabs.pulse.ui.components.tutorials.CardCarousel
import com.marketlabs.pulse.ui.components.tutorials.DeckPage
import com.marketlabs.pulse.ui.screens.stocks.detail.ViewMoreRow
import com.marketlabs.pulse.ui.theme.MarketPulseTheme

/**
 * The per-screen "?" guide: a [CardCarousel] of [pages] (one card per piece of guide text --
 * "Overview", "How to Interpret It") inside a bottom sheet. Below the cards, one row holds
 * [showMoreLabel] left-aligned (into the one matching Tutorials deck, `null` for a screen with no
 * matching deck) and "Learn about Market" right-aligned (into the Tutorials hub itself, on every
 * sheet) -- a weighted spacer between them keeps "Learn about Market" pinned to the right edge
 * even when [showMoreLabel] is absent.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenGuideSheet(
    screenTitle: String,
    pages: List<DeckPage>,
    showMoreLabel: String?,
    onShowMore: () -> Unit,
    onLearnAboutMarket: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val paddingLarge = dimensionResource(id = R.dimen.padding_large)
    val maxCardHeight = (LocalConfiguration.current.screenHeightDp * CardHeightFraction).dp

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        // 💡 Same reasoning as StockAnalysisGlossaryBottomSheet/MarketGlossaryBottomSheet's
        // identical comment -- the default whole-surface swipe-to-dismiss competes with this
        // sheet's own scrollable/swipeable content. Closing still works via the drag handle, scrim, or back.
        sheetGesturesEnabled = false,
        dragHandle = { BottomSheetDragHandle(onDismiss = onDismiss) },
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column {
            Text(
                text = screenTitle,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(start = dimensionResource(id = R.dimen.padding_extra_large), end = paddingLarge, bottom = paddingLarge)
            )
            CardCarousel(pages = pages, maxCardHeight = maxCardHeight)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = dimensionResource(id = R.dimen.padding_extra_large),
                        end = dimensionResource(id = R.dimen.padding_extra_large),
                        top = paddingLarge
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showMoreLabel != null) {
                    ViewMoreRow(text = showMoreLabel, onClick = onShowMore)
                }
                Spacer(modifier = Modifier.weight(1f))
                ViewMoreRow(text = stringResource(id = R.string.screen_guide_learn_about_market), onClick = onLearnAboutMarket)
            }
            Spacer(modifier = Modifier.height(paddingLarge))
            Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
        }
    }
}

private const val CardHeightFraction = 0.7f

@Preview(name = "Light", showBackground = true)
@Composable
private fun PreviewScreenGuideSheetLight() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        ScreenGuideSheet(
            screenTitle = "Overview",
            pages = listOf(
                DeckPage(title = "Overview", body = "A snapshot of the whole market: today's AI-written digest, sentiment gauges, futures, and your tracked assets."),
                DeckPage(title = "How to Interpret It", body = "Numbers, charts, and prices are computed directly from data -- no AI involved.")
            ),
            showMoreLabel = "Show More",
            onShowMore = {},
            onLearnAboutMarket = {},
            onDismiss = {}
        )
    }
}
