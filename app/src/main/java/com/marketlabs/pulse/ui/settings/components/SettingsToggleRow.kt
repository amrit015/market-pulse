package com.marketlabs.pulse.ui.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import com.marketlabs.pulse.R
import com.marketlabs.pulse.ui.components.PulseCard
import com.marketlabs.pulse.ui.components.PulseCardStyle
import com.marketlabs.pulse.ui.theme.LocalPulseColors

/** A card-wrapped title + description + switch; the whole row is the tap target. */
@Composable
fun SettingsToggleRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val pulseColors = LocalPulseColors.current
    PulseCard(style = PulseCardStyle.DATA, modifier = Modifier.fillMaxWidth()) {
        // The whole row is the tap target; the Switch itself is display-only so the pair reads as
        // one control to TalkBack.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange)
                .padding(dimensionResource(id = R.dimen.padding_large)),
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_large)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_small))
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = null,
                // Every state is set from the pulse tokens: the Material defaults for "off" (a
                // pale surface-container track) nearly vanish against the card's tinted background.
                // Off is an outlined track with a muted thumb; on is the accent fill.
                colors = SwitchDefaults.colors(
                    checkedThumbColor = pulseColors.accentOn,
                    checkedTrackColor = pulseColors.accentPrimary,
                    checkedBorderColor = pulseColors.accentPrimary,
                    uncheckedThumbColor = pulseColors.onSurfaceMuted,
                    uncheckedTrackColor = Color.Transparent,
                    uncheckedBorderColor = pulseColors.onSurfaceMuted
                )
            )
        }
    }
}

