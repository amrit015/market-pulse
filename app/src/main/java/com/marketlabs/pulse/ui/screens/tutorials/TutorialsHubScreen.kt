package com.marketlabs.pulse.ui.screens.tutorials

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import com.marketlabs.pulse.ui.components.PulseBackTitleRow
import com.marketlabs.pulse.ui.components.PulseCard
import com.marketlabs.pulse.ui.components.PulseCardStyle
import com.marketlabs.pulse.ui.components.tutorials.Mechanism
import com.marketlabs.pulse.ui.theme.LocalPulseColors
import com.marketlabs.pulse.ui.theme.MarketPulseTheme

/**
 * Settings -> Tutorials, and the "Learn about Market" target of every screen's "?" sheet: the full
 * library, in four sections -- how to read a gauge, market concepts, market mechanisms, and data
 * limitations. Market concepts splits into [ConceptSubGroup] sub-headings. Each row is its own
 * `PulseCard(DATA)`.
 */
@Composable
fun TutorialsHubScreen(
    onNavigateUp: () -> Unit,
    onNavigateToGaugeAnatomy: () -> Unit,
    onNavigateToConcept: (ConceptArticle) -> Unit,
    onNavigateToMechanism: (Mechanism) -> Unit,
    onNavigateToDataLimitations: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top + WindowInsetsSides.Bottom))
            .verticalScroll(rememberScrollState())
    ) {
        PulseBackTitleRow(title = stringResource(id = R.string.tutorials_hub_screen_title), onNavigateUp = onNavigateUp)
        Column(
            modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_medium))
        ) {
            TutorialsHubSectionHeader(text = stringResource(id = R.string.tutorials_hub_section_start))
            TutorialsHubRow(
                label = stringResource(id = R.string.tutorials_item_gauge_anatomy),
                onClick = onNavigateToGaugeAnatomy
            )

            TutorialsHubSectionHeader(text = stringResource(id = R.string.tutorials_hub_section_market_concepts), spaceAbove = true)
            ConceptSubGroup.entries.forEachIndexed { index, subGroup ->
                TutorialsHubSubHeader(text = stringResource(id = subGroup.titleRes), spaceAbove = index > 0)
                ConceptArticle.entries.filter { it.subGroup == subGroup }.forEach { article ->
                    TutorialsHubRow(
                        label = stringResource(id = article.titleRes),
                        onClick = { onNavigateToConcept(article) }
                    )
                }
            }

            TutorialsHubSectionHeader(text = stringResource(id = R.string.tutorials_hub_section_mechanisms), spaceAbove = true)
            Mechanism.entries.forEach { mechanism ->
                TutorialsHubRow(
                    label = stringResource(id = mechanism.titleRes),
                    onClick = { onNavigateToMechanism(mechanism) }
                )
            }

            TutorialsHubSectionHeader(text = stringResource(id = R.string.tutorials_hub_section_data), spaceAbove = true)
            TutorialsHubRow(
                label = stringResource(id = R.string.tutorials_item_data_limitations),
                onClick = onNavigateToDataLimitations
            )
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_xlarge)))
        }
    }
}

@Composable
private fun TutorialsHubSectionHeader(text: String, spaceAbove: Boolean = false) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = LocalPulseColors.current.accentPrimary,
        modifier = if (spaceAbove) Modifier.padding(top = dimensionResource(id = R.dimen.padding_medium)) else Modifier
    )
}

/** A small uppercase label grouping a run of rows under a [TutorialsHubSectionHeader], one rung below it. */
@Composable
private fun TutorialsHubSubHeader(text: String, spaceAbove: Boolean = false) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = LocalPulseColors.current.onSurfaceMuted,
        modifier = if (spaceAbove) Modifier.padding(top = dimensionResource(id = R.dimen.padding_small)) else Modifier
    )
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
            onNavigateToDataLimitations = {}
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
            onNavigateToDataLimitations = {}
        )
    }
}
