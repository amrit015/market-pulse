package com.marketlabs.pulse.ui.screens.insights.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import com.marketlabs.pulse.R
import com.marketlabs.pulse.ui.screens.insights.InsightsUiState

@Composable
fun InsightsScreen(
    uiState: InsightsUiState,
    scaffoldPadding: PaddingValues
) {
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val paddingLarge = dimensionResource(id = R.dimen.padding_large)
    val paddingXLarge = dimensionResource(id = R.dimen.padding_xlarge)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = statusBarHeight)
    ) {
        // ==========================================
        // 📌 STICKY TOP HEADER
        // ==========================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = paddingLarge,
                    vertical = dimensionResource(id = R.dimen.padding_medium)
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val textStyle = MaterialTheme.typography.headlineMedium
            val iconSize = with(LocalDensity.current) { textStyle.fontSize.toDp() }

            Icon(
                painter = painterResource(id = R.drawable.ic_engine_ai_sparkles),
                contentDescription = "Insights",
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(iconSize)
            )
            Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_small)))
            Text(
                text = "Insights", // Consider adding to strings.xml later: stringResource(id = R.string.tab_insights)
                style = textStyle,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // ==========================================
        // 📜 SCROLLING CONTENT
        // ==========================================
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = paddingLarge, // Start slightly below the header
                bottom = scaffoldPadding.calculateBottomPadding() + paddingLarge,
                start = paddingLarge,
                end = paddingLarge
            ),
            verticalArrangement = Arrangement.spacedBy(paddingXLarge)
        ) {

            // --- SECTION 1: WEEKLY PLAYBOOK ---
            uiState.weeklyPlaybook?.let { playbook ->
                if (!playbook.events.isNullOrEmpty()) {
                    item {
                        WeeklyPlaybookSection(playbook = playbook)
                    }

                    item {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                            modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_medium))
                        )
                    }
                }
            }

            // --- SECTION 2: TAIL RISKS ---
            uiState.tailRisks?.let { risksData ->
                item {
                    TailRisksSection(risksData = risksData)
                }
            }
        }
    }
}