package com.marketlabs.pulse.ui.screens.indicators.widgets

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.marketlabs.pulse.R
import com.marketlabs.pulse.storage.model.indicators.enums.SignalColor
import com.marketlabs.pulse.ui.screens.indicators.PillarGuide
import com.marketlabs.pulse.ui.screens.indicators.views.toBgColor
import com.marketlabs.pulse.ui.screens.indicators.views.toColor

// ==========================================
// 🧩 UNIFIED UI COMPONENTS (Put in IndicatorsScreen.kt)
// ==========================================

@Composable
fun ContextHeaderCard(guide: PillarGuide) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_card)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded } // 💡 Collapsible toggle
    ) {
        Column(modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large)).animateContentSize()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = guide.timeframe,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Icon(
                    painter = painterResource(id = if (expanded) R.drawable.ic_arrow_up else R.drawable.ic_arrow_down),
                    contentDescription = "Toggle Description",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(dimensionResource(R.dimen.padding_large))
                )
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_tiny)))
                Text(
                    text = guide.purpose,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))
                Text(
                    text = guide.howToUse,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun UniversalMetricCard(
    title: String,
    value: String?,
    changeString: String? = null,
    signalText: String? = null,
    signalColor: SignalColor? = null,
    observationDate: String? = null,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bgColor = signalColor.toBgColor()
    val baseColor = signalColor.toColor()
    val notAvail = stringResource(id = R.string.not_available_short)

    Card(
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_card_large)),
        modifier = modifier.clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .padding(dimensionResource(id = R.dimen.padding_large))
                .fillMaxHeight() // 💡 Fills the height dictated by the Row
        ) {
            // Header Row (Title + Chevron)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2, // 💡 Will only take 1 line unless it forces a wrap
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    painter = painterResource(id = R.drawable.ic_chevron_forward),
                    contentDescription = "View Details",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(dimensionResource(R.dimen.padding_large)).padding(start = dimensionResource(R.dimen.padding_small))
                )
            }

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))

            // Value Row (Number + Change)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value ?: notAvail,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (!changeString.isNullOrBlank()) {
                    Text(
                        text = changeString,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = baseColor,
                        modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.padding_micro), start = dimensionResource(id = R.dimen.padding_small))
                    )
                }
            }

            // 💡 NEW: This empty weighted spacer pushes everything below it to the absolute bottom of the card!
            Spacer(modifier = Modifier.weight(1f))

            // Optional Signal Badge
            if (signalText != null) {
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))
                Surface(
                    color = baseColor.copy(alpha = 0.0f),
                    shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_chip))
                ) {
                    Text(
                        text = signalText,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = baseColor,
                        modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.padding_small), vertical = dimensionResource(id = R.dimen.padding_tiny))
                    )
                }
            }

            // 💡 MOVED: The Date is now anchored to the bottom of the card
            if (!observationDate.isNullOrBlank()) {
                val formattedDate = try {
                    val parsed = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).parse(observationDate)
                    java.text.SimpleDateFormat("MMM yyyy", java.util.Locale.US).format(parsed!!)
                } catch (e: Exception) {
                    observationDate
                }

                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))
                Text(
                    text = "As of $formattedDate",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}