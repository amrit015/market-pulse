package com.marketlabs.pulse.ui.components.bottomSheet

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.marketlabs.pulse.R
import com.marketlabs.pulse.storage.model.summary.MarketVerdict
import com.marketlabs.pulse.ui.components.widgets.CardEyebrowLabel
import com.marketlabs.pulse.ui.theme.MarketPulseTheme
import com.marketlabs.pulse.utils.enums.Conviction
import com.marketlabs.pulse.utils.enums.SignalDirection

/**
 * Same `ModalBottomSheet` shell `DriversInfoBottomSheet`/`MarketGlossaryBottomSheet` already use,
 * opened by tapping the Signal card's flash headline (`signalLine`) -- a shortcut to the same
 * analysis + posture prose `TheReadSection` renders lower on the page, for a reader who wants the
 * "why" behind the flash without scrolling past drivers/position/stories/macro/domino/watch/risks
 * to reach it. Not a replacement for `TheReadSection`, which stays put at the bottom of Summary.
 * Recaps `signalLine` itself at the top so the sheet still makes sense on its own if the reader
 * scrolled since tapping it.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketReadBottomSheet(verdict: MarketVerdict, onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val paddingExtraLarge = dimensionResource(id = R.dimen.padding_extra_large)
    val paddingLarge = dimensionResource(id = R.dimen.padding_large)
    val paddingMedium = dimensionResource(id = R.dimen.padding_medium)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = paddingExtraLarge)
        ) {
            verdict.signalLine?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = paddingLarge)
                )
            }

            verdict.analysis?.let {
                CardEyebrowLabel(
                    text = stringResource(id = R.string.market_read),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(paddingMedium))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(paddingLarge))
            }

            verdict.posture?.let {
                CardEyebrowLabel(
                    text = stringResource(id = R.string.label_posture),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(paddingMedium))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(paddingLarge))
            }

            Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
        }
    }
}

@Preview(name = "Light", showBackground = true)
@Composable
private fun PreviewMarketReadBottomSheet() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        MarketReadBottomSheet(
            verdict = MarketVerdict(
                signalLine = "Momentum Broadens Across Sectors",
                analysis = "The deterministic signal correctly identifies a completely neutral tape, requiring a continuation of the SIDEWAYS RANGE regime.",
                posture = "Capital is hiding in short-duration paper and defensive staples, refusing to commit to cyclical growth.",
                direction = SignalDirection.MIXED,
                conviction = Conviction.LOW
            ),
            onDismiss = {}
        )
    }
}
