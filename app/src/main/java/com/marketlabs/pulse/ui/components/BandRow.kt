package com.marketlabs.pulse.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.marketlabs.pulse.R
import com.marketlabs.pulse.core.glossary.MetricGlossaryBand
import com.marketlabs.pulse.ui.components.widgets.SignalPill
import com.marketlabs.pulse.ui.theme.LocalPulseColors

/**
 * One row of a metric's "Bands" list (label + meaning), with a "Current" pill and an accent ring
 * on the band the reading falls in. Shared by Indicators' metric detail and the Overview asset
 * detail page for the sentiment readings, so both show a band list identically.
 */
@Composable
fun BandRow(band: MetricGlossaryBand, isCurrent: Boolean) {
    val paddingLarge = dimensionResource(id = R.dimen.padding_large)
    val paddingSmall = dimensionResource(id = R.dimen.padding_small)
    val accent = LocalPulseColors.current.accentPrimary

    // 💡 DATA style, same standardized "raw reading" card look every other data card in the app
    // uses -- the current-band row adds its own accent border on top of the card's own hairline
    // one (via `Modifier.border`, since `PulseCard` locks its own border color) rather than
    // replacing it, so it reads as one emphasized ring, not two stacked borders.
    val cardModifier = Modifier
        .fillMaxWidth()
        .let {
            if (isCurrent) {
                it.border(BorderStroke(1.dp, accent), RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_card_large)))
            } else {
                it
            }
        }

    PulseCard(style = PulseCardStyle.DATA, modifier = cardModifier) {
        Column(modifier = Modifier.padding(paddingLarge)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 💡 `weight(1f)` so a long label wraps inside its own share of the row instead of
                // squeezing the "CURRENT" pill into a one-letter-wide column.
                Text(
                    text = band.label,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f)
                )
                if (isCurrent) {
                    Spacer(modifier = Modifier.width(paddingSmall))
                    SignalPill(
                        text = stringResource(id = R.string.status_current),
                        pillColor = accent.copy(alpha = 0.16f),
                        contentColor = accent
                    )
                }
            }
            Text(
                text = band.meaning,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = paddingSmall)
            )
        }
    }
}

