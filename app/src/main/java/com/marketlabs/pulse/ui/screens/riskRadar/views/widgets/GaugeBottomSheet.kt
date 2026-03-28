package com.marketlabs.pulse.ui.screens.riskRadar.views.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.marketlabs.pulse.R
import com.marketlabs.pulse.ui.screens.riskRadar.views.SelectedGaugeState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GaugeEducationalBottomSheet(
    selectedState: SelectedGaugeState,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = dimensionResource(id = R.dimen.padding_large))
        ) {
            // Sheet Title
            Text(
                text = selectedState.title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))

            // Highlighted Current Status Banner
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = dimensionResource(id = R.dimen.padding_medium)),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(dimensionResource(id = R.dimen.padding_small)),
            ) {
                Row(
                    modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large)),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(id = R.string.radar_current_status),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        modifier = Modifier.padding(start = dimensionResource(id = R.dimen.padding_medium)),
                        text = selectedState.gauge.label ?: stringResource(id = R.string.radar_unknown),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_xlarge)))

            // Educational Concept Description
            Text(
                text = stringResource(id = R.string.radar_what_it_measures),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_tiny)))
            Text(
                text = selectedState.definition.whatItMeasures,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_xlarge)))

            // Evaluation Brackets Breakdown
            Text(
                text = stringResource(id = R.string.radar_how_to_read_it),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))

            selectedState.definition.brackets.forEach { (bracketName, description) ->
                Row(modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.padding_medium))) {
                    Box(
                        modifier = Modifier
                            .padding(top = dimensionResource(id = R.dimen.padding_tiny))
                            .size(dimensionResource(id = R.dimen.bullet_size))
                            .background(
                                MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(50)
                            )
                    )
                    Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_medium)))
                    Column {
                        Text(
                            text = bracketName,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}