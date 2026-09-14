package com.marketlabs.pulse.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.marketlabs.pulse.R
import com.marketlabs.pulse.ui.common.UiError
import com.marketlabs.pulse.ui.theme.MarketPulseTheme

/**
 * Full-screen "fetch failed and there's no cache to fall back on" state: an error message plus a
 * retry button. Same Column/Text/Button shape IndicatorsRoute/InsightsRoute/NewsRoute each already
 * hand-roll for this one condition -- centralized here so every screen that was missing this state
 * doesn't become one more copy of the same few lines.
 */
@Composable
fun PulseErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    // Overridable for the rare case where the recovery action isn't a re-fetch -- e.g.
    // AssetDetail/MetricDetail have no fetch of their own to retry, so an unresolved id's action
    // is "go back", not "retry".
    actionLabel: String = stringResource(id = R.string.action_retry)
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(dimensionResource(id = R.dimen.padding_large)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        Button(
            onClick = onRetry,
            modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_large))
        ) {
            Text(actionLabel)
        }
    }
}

/**
 * Localized copy for a typed [UiError] -- same three strings `StockAnalysisScreen`'s own
 * (private, list-specific) `errorMessageFor` already reads, shared here for every other screen
 * that tracks fetch failures as a [UiError] rather than a plain string.
 */
@Composable
fun uiErrorMessage(error: UiError): String = when (error) {
    is UiError.Network -> stringResource(id = R.string.stock_analysis_error_network)
    is UiError.Server -> stringResource(id = R.string.stock_analysis_error_server)
    is UiError.Unknown -> stringResource(id = R.string.stock_analysis_error_unknown)
}

@Preview(name = "Light", showBackground = true)
@Composable
private fun PulseErrorStateLightPreview() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        PulseErrorState(
            message = "Can't reach the network. Check your connection and try again.",
            onRetry = {}
        )
    }
}

@Preview(name = "Dark", showBackground = true, backgroundColor = 0xFF0D0E12)
@Composable
private fun PulseErrorStateDarkPreview() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        PulseErrorState(
            message = "Can't reach the network. Check your connection and try again.",
            onRetry = {}
        )
    }
}
