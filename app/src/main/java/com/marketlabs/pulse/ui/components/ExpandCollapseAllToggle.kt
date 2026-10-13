package com.marketlabs.pulse.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.marketlabs.pulse.R
import com.marketlabs.pulse.ui.theme.LocalPulseColors

/**
 * An "Expand all" / "Collapse all" button for a list of tap-to-expand rows. Labelled with the
 * action a tap performs ("Expand all" while collapsed) rather than the current state, so it reads
 * as a button. Used by Summary's Drivers tab and the Past Releases list.
 */
@Composable
fun ExpandCollapseAllToggle(expanded: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_small)))
            .clickable(onClick = onToggle)
            .padding(
                horizontal = dimensionResource(id = R.dimen.padding_small),
                vertical = dimensionResource(id = R.dimen.padding_tiny)
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(id = if (expanded) R.string.summary_collapse_all else R.string.summary_expand_all),
            style = MaterialTheme.typography.labelLarge,
            color = LocalPulseColors.current.accentPrimary
        )
        Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_tiny)))
        Icon(
            painter = painterResource(id = if (expanded) R.drawable.ic_arrow_up else R.drawable.ic_arrow_down),
            contentDescription = null,
            tint = LocalPulseColors.current.accentPrimary,
            modifier = Modifier.size(dimensionResource(id = R.dimen.icon_size_small))
        )
    }
}
