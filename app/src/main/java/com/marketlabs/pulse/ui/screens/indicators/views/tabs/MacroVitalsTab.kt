package com.marketlabs.pulse.ui.screens.indicators.views.tabs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.marketlabs.pulse.R
import com.marketlabs.pulse.storage.model.indicators.DomainMacroVitals
import com.marketlabs.pulse.storage.model.indicators.VitalItem
import com.marketlabs.pulse.ui.components.UniversalMetricCard
import com.marketlabs.pulse.ui.screens.indicators.views.AnalyzedAtText
import com.marketlabs.pulse.ui.screens.indicators.views.ContextHeaderCard
import com.marketlabs.pulse.utils.glossary.DictionaryItem
import com.marketlabs.pulse.utils.glossary.IndicatorsDictionary

@Composable
fun MacroVitalsTab(
    vitalsData: DomainMacroVitals?,
    onIndicatorClick: (DictionaryItem?) -> Unit
) {
    val guide = IndicatorsDictionary.dashboardPillars[2]

    LazyColumn(
        contentPadding = PaddingValues(dimensionResource(id = R.dimen.padding_large)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_xlarge)),
        modifier = Modifier.fillMaxSize()
    ) {
        item { ContextHeaderCard(guide) }

        if (vitalsData != null) {
            item { AnalyzedAtText(vitalsData.timestamp) }

            if (vitalsData.inflation.isNotEmpty()) {
                item { VitalsSection(stringResource(id = R.string.vitals_inflation), vitalsData.inflation, onIndicatorClick) }
            }
            if (vitalsData.labor.isNotEmpty()) {
                item { VitalsSection(stringResource(id = R.string.vitals_labor), vitalsData.labor, onIndicatorClick) }
            }
            if (vitalsData.growth.isNotEmpty()) {
                item { VitalsSection(stringResource(id = R.string.vitals_growth), vitalsData.growth, onIndicatorClick) }
            }
            if (vitalsData.policy.isNotEmpty()) {
                item { VitalsSection(stringResource(id = R.string.vitals_policy), vitalsData.policy, onIndicatorClick) }
            }
        } else {
            item { Text(stringResource(id = R.string.no_macro_vitals_data), color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    }
}

@Composable
fun VitalsSection(title: String, items: List<VitalItem>, onIndicatorClick: (DictionaryItem?) -> Unit) {
    val paddingMedium = dimensionResource(id = R.dimen.padding_medium)

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.padding_large))
        )

        items.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max), // 💡 ADDED
                horizontalArrangement = Arrangement.spacedBy(paddingMedium)
            ) {
                rowItems.forEach { item ->
                    UniversalMetricCard(
                        title = item.name,
                        value = item.displayValue,
                        changeString = item.changeString,
                        signalColor = item.signalColor,
                        dateString = item.releasedDate, // 💡 Pass the releasedDate here for the card UI
                        modifier = Modifier.weight(1f).fillMaxHeight(), // 💡 ADDED fillMaxHeight
                        onClick = {
                            val baseDef = IndicatorsDictionary.getDefinitionFor(item.name)

                            // 💡 Cleanly pass the observationDate as its own distinct property
                            if (baseDef != null) {
                                onIndicatorClick(
                                    DictionaryItem(
                                        title = baseDef.title,
                                        subtitle = baseDef.subtitle, // Kept completely original
                                        definition = baseDef.definition,
                                        howToRead = baseDef.howToRead,
                                        observationDate = item.observationDate // 💡 Passed explicitly
                                    )
                                )
                            } else {
                                onIndicatorClick(null)
                            }
                        }
                    )
                }
                if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f).fillMaxHeight()) // 💡 ADDED fillMaxHeight
            }
            Spacer(modifier = Modifier.height(paddingMedium))
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = paddingMedium))
    }
}