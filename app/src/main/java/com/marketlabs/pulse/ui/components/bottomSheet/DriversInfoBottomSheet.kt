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
import com.marketlabs.pulse.ui.theme.MarketPulseTheme

/**
 * Same `ModalBottomSheet` shell/styling `MarketGlossaryBottomSheet`/`StockAnalysisGlossaryBottomSheet`
 * already use, holding one static explanation rather than a term list -- drivers[] only has one
 * thing that needs explaining (what the color/arrow means), not a set of lookup terms.
 *
 * Exists because `drivers[].direction` changed meaning backend-side (2026-08-18): it's now the
 * model's reconciled call on a driver's net effect on equities, not a mechanical copy of the
 * underlying indicator's own reading -- see `MarketDriver`'s doc comment in `SummaryModels.kt`.
 * That means a driver's color/arrow can no longer be read as "this data point went up/down"; this
 * sheet is the one place that distinction gets explained to the user, since the drivers[] payload
 * itself carries no separate "raw data direction" field to show alongside it.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriversInfoBottomSheet(onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val paddingExtraLarge = dimensionResource(id = R.dimen.padding_extra_large)
    val paddingLarge = dimensionResource(id = R.dimen.padding_large)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        // 💡 See MarketGlossaryBottomSheet's identical comment (MarketBottomSheet.kt) -- the
        // default whole-surface swipe-to-dismiss competed with this sheet's own scrollable
        // content, reading as jumpy. Closing still works via the drag handle's tap, the scrim, or
        // back.
        sheetGesturesEnabled = false,
        dragHandle = { BottomSheetDragHandle(onDismiss = onDismiss) },
        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = paddingExtraLarge)
        ) {
            Text(
                text = stringResource(id = R.string.drivers_info_title),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = paddingLarge)
            )
            Text(
                text = stringResource(id = R.string.drivers_info_body_impact),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(paddingLarge))
            Text(
                text = stringResource(id = R.string.drivers_info_body_caveat),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(paddingLarge))
            Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
        }
    }
}

@Preview(name = "Light", showBackground = true)
@Composable
private fun PreviewDriversInfoBottomSheet() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        DriversInfoBottomSheet(onDismiss = {})
    }
}
