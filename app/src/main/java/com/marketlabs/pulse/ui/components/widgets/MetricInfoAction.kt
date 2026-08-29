package com.marketlabs.pulse.ui.components.widgets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import com.marketlabs.pulse.R
import com.marketlabs.pulse.ui.theme.MarketPulseTheme

/**
 * Small "what is this metric" affordance for the Posture/Positioning cards -- each gauge already
 * carries its own backend-authored `description` string (that's what it exists for), so this is
 * just a tap target to surface it rather than a new glossary entry. Deliberately NOT routed
 * through `core/glossary/MetricGlossaryProvider` -- that's the Indicators domain's 26-metric
 * glossary (2026-08-22 revamp) and none of these gauges (NAAIM, DIX, net liquidity, AAII, COT,
 * FINRA short interest) are in it.
 *
 * A plain sized+clickable `Icon`, not `IconButton` -- `IconButton` forces Material3's 48dp minimum
 * touch target, which is far taller than the title-row text it sits next to and inflated the whole
 * row (and so every card's outer height) with dead space above/below. Matches the same fix
 * Summary's `GlossaryChevron` already uses for its own inline per-term tap icon.
 */
@Composable
fun MetricInfoAction(title: String, description: String?, modifier: Modifier = Modifier) {
    if (description.isNullOrBlank()) return
    var showDialog by remember { mutableStateOf(false) }

    Icon(
        painter = painterResource(id = R.drawable.ic_info),
        contentDescription = stringResource(id = R.string.insights_metric_info_action, title),
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
            .padding(start = dimensionResource(id = R.dimen.padding_tiny))
            .size(dimensionResource(id = R.dimen.icon_size_small))
            .clickable(role = Role.Button) { showDialog = true }
    )

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(text = title) },
            text = { Text(text = description) },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(text = stringResource(id = R.string.insights_metric_info_dismiss))
                }
            }
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun PreviewMetricInfoAction() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        MetricInfoAction(
            title = "NAAIM Exposure",
            description = "Tracks the average equity exposure of active money managers."
        )
    }
}
