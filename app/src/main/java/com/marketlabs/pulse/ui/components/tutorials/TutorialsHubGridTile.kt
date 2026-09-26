package com.marketlabs.pulse.ui.components.tutorials

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.marketlabs.pulse.R
import com.marketlabs.pulse.ui.components.PulseCard
import com.marketlabs.pulse.ui.components.PulseCardStyle
import com.marketlabs.pulse.ui.theme.LocalPulseColors
import com.marketlabs.pulse.ui.theme.MarketPulseTheme

/**
 * One tile in a [TutorialsHubGrid] -- title + one-line subtitle, text only (no preview image, per
 * the Learn hub grid decision). Same `PulseCard(DATA)` + chevron affordance as the flat
 * `TutorialsHubRow` it replaces, just stacked vertically instead of one row, since two columns
 * leaves less width per item than a full-width row did. `DATA` is deliberately borderless
 * everywhere else in the app (its own shadow is what separates it from the page), but a grid tile
 * sitting inside a [TutorialsHubSubSectionCard] shares that exact same background color, so with no
 * border the two visually merge into one shape -- a hairline is what actually separates a tile from
 * its sub-section's card here, at the exact same `border_thin` + `accentSurfaceBorder` treatment
 * `PulseCardStyle.DATA_SPARKLINE` already uses (the Dashboard's equity cards), not a new stroke style.
 */
@Composable
fun TutorialsHubGridTile(title: String, subtitle: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val tileShape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_card_large))
    PulseCard(
        style = PulseCardStyle.DATA,
        onClick = onClick,
        shape = tileShape,
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .border(
                border = BorderStroke(dimensionResource(id = R.dimen.border_thin), LocalPulseColors.current.accentSurfaceBorder),
                shape = tileShape
            )
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(dimensionResource(id = R.dimen.padding_large))) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Icon(
                    painter = painterResource(id = R.drawable.ic_chevron_forward),
                    contentDescription = null,
                    tint = LocalPulseColors.current.accentPrimary
                )
            }
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = LocalPulseColors.current.onSurfaceMuted,
                modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_small))
            )
        }
    }
}

/**
 * A 2-column grid of [TutorialsHubGridTile]s -- when [items] is odd, the last tile spans both
 * columns instead of pairing with an empty gap (the "odd-item-full-row" behavior the Learn hub
 * grid spec calls for). No `LazyVerticalGrid`: every section here is a short, fixed list already
 * rendered inside the hub's own `verticalScroll` column, so a lazy grid would just add nested-
 * scrolling complexity for no benefit.
 */
@Composable
fun <T> TutorialsHubGrid(
    items: List<T>,
    title: @Composable (T) -> String,
    subtitle: @Composable (T) -> String,
    onClick: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    val pairedCount = if (items.size % 2 == 1) items.size - 1 else items.size
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_medium))) {
        items.take(pairedCount).chunked(2).forEach { pair ->
            // `IntrinsicSize.Min` gives the row a real height (its tallest child's), so
            // `fillMaxHeight()` on each tile below has something concrete to fill -- otherwise a
            // shorter tile (fewer subtitle lines) would stay its own natural height instead of
            // matching its row-mate, the same-row-same-size behavior asked for here.
            Row(
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_medium))
            ) {
                pair.forEach { item ->
                    TutorialsHubGridTile(
                        title = title(item),
                        subtitle = subtitle(item),
                        onClick = { onClick(item) },
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    )
                }
            }
        }
        if (items.size % 2 == 1) {
            val last = items.last()
            TutorialsHubGridTile(title = title(last), subtitle = subtitle(last), onClick = { onClick(last) })
        }
    }
}

// ============================================================================
// 🎨 PREVIEWS
// ============================================================================

@Preview(name = "Light", showBackground = true)
@Composable
private fun PreviewTutorialsHubGridTile() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        TutorialsHubGridTile(
            title = "Tactical Momentum",
            subtitle = "Short-term sentiment and momentum — how fast the market's moving.",
            onClick = {}
        )
    }
}

@Preview(name = "Odd grid", showBackground = true, widthDp = 360)
@Composable
private fun PreviewTutorialsHubGridOdd() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        TutorialsHubGrid(
            items = listOf("One", "Two", "Three"),
            title = { it },
            subtitle = { "A one-line description of $it." },
            onClick = {}
        )
    }
}
