package com.marketlabs.pulse.ui.components.tutorials

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.marketlabs.pulse.R
import com.marketlabs.pulse.core.learn.LearnRouteChip
import com.marketlabs.pulse.ui.theme.LocalPulseColors
import com.marketlabs.pulse.ui.theme.MarketPulseTheme

/**
 * "Open in app" chips under a card that names real screens by name (e.g. Bull & Bear Markets'
 * In Market Pulse card naming Market Posture/Tactical Momentum/Systemic Risk/Overview) -- the
 * first use of Material3's `AssistChip` anywhere in this app, so its look is set once here rather
 * than as a one-off. [onNavigateToRoute] receives [LearnRouteChip.route] exactly as authored in
 * `learn_content.json` (a `PulseRoutes` constant NAME, e.g. "MARKET_INDICATORS") -- resolving that
 * name to an actual navigable route is the caller's job (see `PulseNavGraph`'s wiring), since this
 * component has no navigation dependency of its own.
 */
@Composable
fun LearnRouteChips(routes: List<LearnRouteChip>, onNavigateToRoute: (String) -> Unit, modifier: Modifier = Modifier) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_small)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_small))
    ) {
        routes.forEach { chip ->
            AssistChip(
                onClick = { onNavigateToRoute(chip.route) },
                label = { Text(text = chip.label, style = MaterialTheme.typography.labelMedium) },
                trailingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_chevron_forward),
                        contentDescription = null,
                        modifier = Modifier.size(AssistChipDefaults.IconSize)
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = LocalPulseColors.current.accentSurface,
                    labelColor = LocalPulseColors.current.accentPrimary,
                    trailingIconContentColor = LocalPulseColors.current.accentPrimary
                ),
                border = null
            )
        }
    }
}

// ============================================================================
// 🎨 PREVIEWS
// ============================================================================

@Preview(name = "Light", showBackground = true)
@Composable
private fun PreviewLearnRouteChips() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        LearnRouteChips(
            routes = listOf(
                LearnRouteChip(route = "MARKET_INSIGHTS", label = "Market Posture"),
                LearnRouteChip(route = "MARKET_INDICATORS", label = "Tactical Momentum"),
                LearnRouteChip(route = "MARKET_INDICATORS", label = "Systemic Risk"),
                LearnRouteChip(route = "MARKET_OVERVIEW", label = "Overview")
            ),
            onNavigateToRoute = {}
        )
    }
}
