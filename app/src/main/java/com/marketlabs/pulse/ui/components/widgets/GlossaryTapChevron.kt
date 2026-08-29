package com.marketlabs.pulse.ui.components.widgets

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import com.marketlabs.pulse.R

/**
 * Small forward chevron marking a value as tappable-for-definition -- a public sibling of
 * Summary's private `GlossaryChevron` (`SummaryScreen.kt`), same icon/size/purpose, pulled out
 * shared since Positioning/Posture (2026-08-27 interpretive-layer spec) need the identical
 * affordance on several independent values per card (a COT contract's % OI value and its
 * percentile badge, a short-interest instrument's days-to-cover/shares/mom-change), not just one
 * pill per card the way Summary's regime/setup/cycle-zone chips are. `contentDescription` is null
 * (decorative) for the same reason as Summary's: the value text next to it is what a screen reader
 * already announces for this tap target.
 */
@Composable
fun GlossaryTapChevron(tint: Color, modifier: Modifier = Modifier) {
    Icon(
        painter = painterResource(id = R.drawable.ic_chevron_forward),
        contentDescription = null,
        tint = tint,
        modifier = modifier.size(dimensionResource(id = R.dimen.icon_size_small))
    )
}
