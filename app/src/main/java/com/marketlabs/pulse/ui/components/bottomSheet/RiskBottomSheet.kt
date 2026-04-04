package com.marketlabs.pulse.ui.components.bottomSheet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.marketlabs.pulse.R
import com.marketlabs.pulse.utils.glossary.GlossaryTerm
import com.marketlabs.pulse.utils.glossary.RiskGlossary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiskGlossaryBottomSheet(
    currentStatus: String? = null,
    currentTrend: String? = null,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimensionResource(id = R.dimen.padding_extra_large))
        ) {
            item {
                Text(
                    text = stringResource(id = R.string.risk_status_and_glossary),
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.padding_large))
                )
            }

            // 💡 NEW: Added the "What it measures" and Contagion Penalty explanation here!
            item {
                Text(
                    text = stringResource(id = R.string.radar_what_it_measures),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(id = R.string.risk_vulnerability_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(
                        top = dimensionResource(id = R.dimen.padding_tiny),
                        bottom = dimensionResource(id = R.dimen.padding_small)
                    )
                )

                // Warning box for the Contagion Penalty
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_card)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = dimensionResource(id = R.dimen.padding_large))
                ) {
                    Text(
                        text = stringResource(id = R.string.risk_vulnerability_contagion),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_medium))
                    )
                }
            }

            item {
                Text(
                    text = stringResource(id = R.string.current_verdict),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.padding_small))
                )

                if (currentStatus != null) {
                    RiskStatusRow(
                        stringResource(id = R.string.risk_status_label),
                        currentStatus,
                        RiskGlossary.statuses
                    )
                }
                if (currentTrend != null) {
                    RiskStatusRow(
                        stringResource(id = R.string.risk_trend_label),
                        currentTrend,
                        RiskGlossary.trends
                    )
                }

                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_large)))
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_large)))
            }

            item {
                RiskGlossarySection(
                    stringResource(id = R.string.risk_status_glossary_title),
                    RiskGlossary.statuses,
                    currentStatus
                )
            }
            item {
                RiskGlossarySection(
                    stringResource(id = R.string.risk_trend_glossary_title),
                    RiskGlossary.trends,
                    currentTrend
                )
            }

            item { Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars)) }
        }
    }
}

@Composable
private fun RiskStatusRow(label: String, value: String, dictionary: List<GlossaryTerm>) {
    val definition = dictionary.find { it.term == value }?.definition
        ?: stringResource(id = R.string.not_available_short)

    Column(modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.padding_medium))) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "$label ",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_tiny)))
        Text(
            text = definition,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun RiskGlossarySection(title: String, terms: List<GlossaryTerm>, currentVal: String?) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(
            top = dimensionResource(id = R.dimen.padding_medium),
            bottom = dimensionResource(id = R.dimen.padding_small)
        )
    )

    terms.forEach { item ->
        val isCurrent = item.term == currentVal
        val bgColor =
            if (isCurrent) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f) else Color.Transparent
        val titleColor =
            if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = bgColor,
                    shape = RoundedCornerShape(dimensionResource(id = R.dimen.padding_medium))
                )
                .padding(dimensionResource(id = R.dimen.padding_medium))
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = item.term,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = titleColor
                )
                if (isCurrent) {
                    Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_small)))
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_chip))
                    ) {
                        Text(
                            text = stringResource(id = R.string.status_current),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            // We change this from onPrimary to primary so the text matches the bold title
                            // and contrasts perfectly against the 30% opacity background in both themes.
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(
                                horizontal = dimensionResource(id = R.dimen.padding_medium),
                                vertical = dimensionResource(id = R.dimen.padding_tiny)
                            )
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))
            Text(
                text = item.definition,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))
    }
}