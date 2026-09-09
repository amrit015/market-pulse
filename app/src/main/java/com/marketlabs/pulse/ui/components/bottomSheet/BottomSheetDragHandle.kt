package com.marketlabs.pulse.ui.components.bottomSheet

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.marketlabs.pulse.R
import com.marketlabs.pulse.ui.theme.MarketPulseTheme

/**
 * Every `ModalBottomSheet` in this app's own `dragHandle` slot -- the default pill (still shown,
 * centered) plus an explicit close [IconButton] at the trailing edge. Added alongside
 * `sheetGesturesEnabled = false` (see each sheet's own comment on that flag): with the whole-sheet
 * swipe-to-dismiss gone, the pill's own tap-to-dismiss was the only way to close a fully expanded
 * sheet, and tapping a plain drag handle isn't a discoverable affordance on its own -- this gives
 * every sheet an explicit, obvious close action alongside it. Sits inside `ModalBottomSheet`'s own
 * `Modifier.clickable` wrapper around whatever `dragHandle` renders (tapping the pill/whitespace
 * around this row still triggers that built-in tap-to-dismiss); the `IconButton`'s own click
 * target takes priority over it within the icon's own bounds, so tapping the X doesn't double-fire
 * both.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetDragHandle(onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = dimensionResource(id = R.dimen.padding_medium))
    ) {
        BottomSheetDefaults.DragHandle(modifier = Modifier.align(Alignment.Center))
        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = dimensionResource(id = R.dimen.padding_medium))
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_close),
                contentDescription = stringResource(id = R.string.close),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(dimensionResource(id = R.dimen.icon_size_extra_large))
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewBottomSheetDragHandle() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        BottomSheetDragHandle(onDismiss = {})
    }
}
