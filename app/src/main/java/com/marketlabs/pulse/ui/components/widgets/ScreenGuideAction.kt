package com.marketlabs.pulse.ui.components.widgets

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.marketlabs.pulse.R
import com.marketlabs.pulse.ui.components.bottomSheet.ScreenGuideSheet
import com.marketlabs.pulse.ui.components.tutorials.DeckPage
import com.marketlabs.pulse.ui.components.tutorials.Mechanism

/**
 * Bundles one screen's guide -- its overview and how-to-interpret text, plus the market mechanisms
 * whose Tutorials decks "Show More" opens ([mechanisms] is empty for a screen that isn't tied to
 * any mechanism, in which case there is no "Show More" at all).
 */
data class ScreenGuideContent(
    val screenTitle: String,
    val overview: String,
    val howToInterpret: String,
    val mechanisms: List<Mechanism> = emptyList()
)

/**
 * Once-per-screen "?" -- opens a bottom sheet with one card for "Overview" and one for "How to
 * Interpret It", and a "Show More" link under them into the matching Tutorials deck when the screen
 * has one. A screen-level guide, distinct from a single value's own explainer or a single AI card's
 * disclosure. Self-contained sheet-visibility state, same pattern [MetricInfoAction] already
 * uses, so a call site just drops this into its top bar with no external state to thread.
 *
 * `IconButton` + `icon_size_large` (24dp), not a raw `Modifier.clickable` Icon at `icon_size_medium`
 * (20dp) -- this sits directly beside `AppTopBar`'s Settings gear (a plain, unsized `Icon` inside
 * `IconButton`, which renders at its drawable's own intrinsic 24dp), so it needs the exact same
 * icon size AND the same `IconButton` touch-target/ripple treatment to read as two equally-weighted
 * actions in the same bar, not one bigger with a real touch target and one smaller as an afterthought.
 */
@Composable
fun ScreenGuideAction(
    content: ScreenGuideContent,
    onShowMore: (List<Mechanism>) -> Unit,
    modifier: Modifier = Modifier
) {
    var showGuide by remember { mutableStateOf(false) }

    IconButton(onClick = { showGuide = true }, modifier = modifier) {
        Icon(
            painter = painterResource(id = R.drawable.ic_help),
            contentDescription = stringResource(id = R.string.screen_guide_content_description, content.screenTitle),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(dimensionResource(id = R.dimen.icon_size_large))
        )
    }

    if (showGuide) {
        ScreenGuideSheet(
            screenTitle = content.screenTitle,
            pages = listOf(
                DeckPage(title = stringResource(id = R.string.screen_guide_overview_heading), body = content.overview),
                DeckPage(title = stringResource(id = R.string.screen_guide_how_to_interpret_heading), body = content.howToInterpret)
            ),
            showMoreLabel = if (content.mechanisms.isEmpty()) null else stringResource(id = R.string.screen_guide_show_more),
            onShowMore = {
                showGuide = false
                onShowMore(content.mechanisms)
            },
            onDismiss = { showGuide = false }
        )
    }
}
