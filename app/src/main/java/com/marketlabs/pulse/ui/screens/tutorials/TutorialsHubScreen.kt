package com.marketlabs.pulse.ui.screens.tutorials

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.marketlabs.pulse.R
import com.marketlabs.pulse.core.learn.IndicatorArticlesProvider
import com.marketlabs.pulse.core.learn.LearnContentProvider
import com.marketlabs.pulse.ui.components.PulseBackTitleRow
import com.marketlabs.pulse.ui.components.PulseCard
import com.marketlabs.pulse.ui.components.PulseCardStyle
import com.marketlabs.pulse.ui.components.tutorials.GaugePatternBandBar
import com.marketlabs.pulse.ui.components.tutorials.Mechanism
import com.marketlabs.pulse.ui.components.tutorials.TutorialsHubGrid
import com.marketlabs.pulse.ui.theme.LocalPulseColors
import com.marketlabs.pulse.ui.theme.MarketPulseTheme

/** Fixed display order for the "Economic Events" tiles -- 6 of these ids are shared with
 * [TutorialsGaugesCatalog]'s own `macro_vitals` entries (the same indicator article, reached here a
 * second time as a standard release rather than as a gauge); the other 5 have no gauge at all and
 * exist only as this section's own articles. The 35 indicator articles proper live one level down,
 * inside each mechanism deck's own "See all indicators" ([TutorialsGaugesScreen]) -- not on this hub. */
private val ECONOMIC_EVENT_ARTICLE_KEYS = listOf(
    "cpi_yoy", "core_pce_yoy", "nfp", "unemployment", "real_gdp", "retail_sales",
    "ism_manufacturing_pmi", "ism_services_pmi", "ppi", "initial_jobless_claims", "fomc_rate_decision"
)

/**
 * Settings -> Tutorials, and the "Learn about Market" target of every screen's "?" sheet: the full
 * library, in five sections -- how to read a gauge, market concepts, economic events, market
 * mechanisms, and data -- separated by a hairline [TutorialsHubSectionDivider]. Market concepts
 * splits into [ConceptSubGroup] sub-headings, each its own [TutorialsHubSubSectionCard] wrapping a
 * 2-column grid of [TutorialsHubGrid] (so a sub-section reads as one card, its tiles nested cards
 * inside it). Economic events sits right after it, same section-header + subtitle treatment, but
 * (like market mechanisms) has no sub-headings of its own -- just one flat grid of
 * [ECONOMIC_EVENT_ARTICLE_KEYS] in a [TutorialsHubSubSectionCard]. Getting Started's one entry
 * (Gauge Anatomy) stays a distinct, larger featured card above everything else rather than joining
 * a grid -- it's the one thing every other row/tile in this screen assumes the reader has already
 * seen. The Data section holds just Data Limitations (a single flat row) -- the 35 per-metric
 * indicator articles don't live on this hub at all; they're reached one mechanism at a time via
 * that deck's own "See all indicators" ([TutorialsGaugesScreen]).
 *
 * Every section header's title/subtitle is looked up from `learn_content.json`'s `hub_sections` map
 * by a fixed id (`start`/`market_concepts`/`economic_events`/`mechanisms`/`data`) hardcoded at each
 * call site below -- there's no enum backing this hub's own top-level section list, so the id is
 * whatever this function itself decides to look up.
 */
@Composable
fun TutorialsHubScreen(
    onNavigateUp: () -> Unit,
    onNavigateToGaugeAnatomy: () -> Unit,
    onNavigateToConcept: (ConceptArticle) -> Unit,
    onNavigateToMechanism: (Mechanism) -> Unit,
    onNavigateToDataLimitations: () -> Unit,
    onNavigateToArticle: (String) -> Unit
) {
    val context = LocalContext.current
    val learnContent = LearnContentProvider.get(context)
    val indicatorArticles = IndicatorArticlesProvider.get(context)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top + WindowInsetsSides.Bottom))
            .verticalScroll(rememberScrollState())
    ) {
        PulseBackTitleRow(title = stringResource(id = R.string.tutorials_hub_screen_title), onNavigateUp = onNavigateUp)
        Column(
            modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_xlarge))
        ) {
            TutorialsHubSectionHeader(
                text = learnContent.hubSections["start"]?.title.orEmpty(),
                subtitle = learnContent.hubSections["start"]?.subtitle.orEmpty()
            )
            TutorialsHubHeroCard(
                title = learnContent.gaugeAnatomy.title,
                onClick = onNavigateToGaugeAnatomy
            )

            TutorialsHubSectionDivider()
            TutorialsHubSectionHeader(
                text = learnContent.hubSections["market_concepts"]?.title.orEmpty(),
                subtitle = learnContent.hubSections["market_concepts"]?.subtitle.orEmpty()
            )
            ConceptSubGroup.entries.forEach { subGroup ->
                val articles = ConceptArticle.entries.filter { it.subGroup == subGroup }
                val heading = learnContent.conceptSubgroupHeadings[subGroup.jsonKey]
                TutorialsHubSubSectionCard {
                    TutorialsHubSubHeader(
                        text = heading?.title.orEmpty(),
                        subtitle = heading?.subtitle.orEmpty()
                    )
                    TutorialsHubGrid(
                        items = articles,
                        title = { learnContent.conceptArticles[it.routeKey]?.title.orEmpty() },
                        subtitle = { learnContent.conceptArticles[it.routeKey]?.subtitle.orEmpty() },
                        onClick = onNavigateToConcept
                    )
                }
            }

            TutorialsHubSectionDivider()
            TutorialsHubSectionHeader(
                text = learnContent.hubSections["economic_events"]?.title.orEmpty(),
                subtitle = learnContent.hubSections["economic_events"]?.subtitle.orEmpty()
            )
            TutorialsHubSubSectionCard {
                TutorialsHubGrid(
                    items = ECONOMIC_EVENT_ARTICLE_KEYS,
                    title = { indicatorArticles[it]?.title.orEmpty() },
                    subtitle = { indicatorArticles[it]?.subtitle.orEmpty() },
                    onClick = onNavigateToArticle
                )
            }

            TutorialsHubSectionDivider()
            TutorialsHubSectionHeader(
                text = learnContent.hubSections["mechanisms"]?.title.orEmpty(),
                subtitle = learnContent.hubSections["mechanisms"]?.subtitle.orEmpty()
            )
            TutorialsHubSubSectionCard {
                TutorialsHubGrid(
                    items = Mechanism.entries,
                    title = { stringResource(id = it.titleRes) },
                    subtitle = { learnContent.mechanismDecks[it.routeKey]?.subtitle.orEmpty() },
                    onClick = onNavigateToMechanism
                )
            }

            TutorialsHubSectionDivider()
            TutorialsHubSectionHeader(
                text = learnContent.hubSections["data"]?.title.orEmpty(),
                subtitle = learnContent.hubSections["data"]?.subtitle.orEmpty()
            )
            TutorialsHubRow(
                label = learnContent.dataLimitations.title,
                onClick = onNavigateToDataLimitations
            )

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_xlarge)))
        }
    }
}

@Composable
private fun TutorialsHubSectionHeader(text: String, subtitle: String) {
    Column {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = LocalPulseColors.current.accentPrimary
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = LocalPulseColors.current.onSurfaceMuted,
            modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_tiny))
        )
    }
}

/** A hairline rule between two top-level sections (Getting Started / Market Concepts / Market
 * Mechanisms / About the Data) -- `spacedBy` on the parent Column already puts a gap on both sides
 * of it, so this is just the line itself. */
@Composable
private fun TutorialsHubSectionDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_small)),
        thickness = dimensionResource(id = R.dimen.border_thin)
    )
}

/** A small uppercase label grouping a run of tiles under a [TutorialsHubSectionHeader], one rung
 * below it -- same `accentPrimary` title color as the section header above it, just one type-scale
 * step down, so a sub-section still reads as part of the same heading family. */
@Composable
private fun TutorialsHubSubHeader(text: String, subtitle: String) {
    Column(modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.padding_large))) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = LocalPulseColors.current.accentPrimary
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = LocalPulseColors.current.onSurfaceMuted,
            modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_tiny))
        )
    }
}

/** Wraps a whole [ConceptSubGroup]'s sub-header + grid in one outer `PulseCard` -- the grid's own
 * tiles stay their own individual cards inside it (nested cards), same as the per-item styling
 * already used everywhere else in this hub. */
@Composable
private fun TutorialsHubSubSectionCard(content: @Composable ColumnScope.() -> Unit) {
    PulseCard(style = PulseCardStyle.DATA, modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(dimensionResource(id = R.dimen.padding_large)),
            content = content
        )
    }
}

/** The one card in "Getting Started" -- deliberately larger/bolder than a [TutorialsHubGridTile],
 * full width, and positioned above every grid so the reader hits it first. Carries its own preview
 * strip (a small [GaugePatternBandBar], the same band-with-marker visual the article's first card
 * teaches) -- the one place in the hub that keeps a preview at all, per the grid decision that gave
 * every other tile plain title+subtitle text instead. */
@Composable
private fun TutorialsHubHeroCard(title: String, onClick: () -> Unit) {
    PulseCard(style = PulseCardStyle.DATA, onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().padding(dimensionResource(id = R.dimen.padding_xlarge))) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    painter = painterResource(id = R.drawable.ic_chevron_forward),
                    contentDescription = null,
                    tint = LocalPulseColors.current.accentPrimary
                )
            }
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_large)))
            GaugePatternBandBar(
                bullFraction = 1f / 3f,
                neutralFraction = 1f / 3f,
                bearFraction = 1f / 3f,
                markerFraction = 0.5f
            )
        }
    }
}

@Composable
private fun TutorialsHubRow(label: String, onClick: () -> Unit) {
    PulseCard(style = PulseCardStyle.DATA, onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(id = R.dimen.padding_large)),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Icon(
                painter = painterResource(id = R.drawable.ic_chevron_forward),
                contentDescription = null,
                tint = LocalPulseColors.current.accentPrimary
            )
        }
    }
}

// ============================================================================
// 🎨 PREVIEWS
// ============================================================================

@Preview(name = "Light", showBackground = true)
@Composable
private fun PreviewTutorialsHubScreenLight() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        TutorialsHubScreen(
            onNavigateUp = {},
            onNavigateToGaugeAnatomy = {},
            onNavigateToConcept = {},
            onNavigateToMechanism = {},
            onNavigateToDataLimitations = {},
            onNavigateToArticle = {}
        )
    }
}

@Preview(name = "Dark", showBackground = true)
@Composable
private fun PreviewTutorialsHubScreenDark() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        TutorialsHubScreen(
            onNavigateUp = {},
            onNavigateToGaugeAnatomy = {},
            onNavigateToConcept = {},
            onNavigateToMechanism = {},
            onNavigateToDataLimitations = {},
            onNavigateToArticle = {}
        )
    }
}
