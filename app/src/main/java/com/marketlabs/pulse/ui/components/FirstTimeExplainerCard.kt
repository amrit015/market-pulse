package com.marketlabs.pulse.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.marketlabs.pulse.R
import com.marketlabs.pulse.ui.theme.MarketPulseTheme

/**
 * One-time, dismissible intro card pinned above a screen's gauges (2026-08-27 interpretive-layer
 * spec, Layer 3) -- not a blocking modal, just a normal card in the scroll flow that the reader can
 * dismiss permanently via "Got it." Persistence lives in the caller (`InsightsUiStateRepository`
 * via `InsightsViewModel`), not here -- this composable is pure display, same "no data of its own"
 * shape every other stateless screen-level composable in this app follows.
 */
@Composable
fun FirstTimeExplainerCard(text: String, onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    PulseCard(style = PulseCardStyle.DATA, modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onDismiss) {
                    Text(text = stringResource(id = R.string.insights_explainer_dismiss))
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun PreviewFirstTimeExplainerCard() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        FirstTimeExplainerCard(
            text = "Positioning shows where retail sentiment, large speculators, and short sellers currently stand — three different lenses on who's leaning which way. None of these are predictions or recommendations; they describe current positioning, which can shift or reverse without warning.",
            onDismiss = {}
        )
    }
}
