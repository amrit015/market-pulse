package com.marketlabs.pulse.ui.screens.insights.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.marketlabs.pulse.R
import com.marketlabs.pulse.ui.screens.insights.InsightsUiState

@Composable
fun InsightsScreen(
    uiState: InsightsUiState,
    scaffoldPadding: PaddingValues
) {
    val paddingLarge = dimensionResource(id = R.dimen.padding_large)
    val paddingXLarge = dimensionResource(id = R.dimen.padding_xlarge)

    // 💡 The icon+"Insights" row that used to open this screen is gone -- the title now lives in
    // the global top bar (MainActivity resolves it per-route), so keeping this row would have said
    // the same thing twice in two places on screen at once. `scaffoldPadding`'s top component (not
    // the raw status bar inset alone) is what actually accounts for the top bar's real rendered
    // height, so content starts right below it instead of underneath it.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ==========================================
        // 📜 SCROLLING CONTENT
        // ==========================================
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = scaffoldPadding.calculateTopPadding() + paddingLarge,
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

            // --- SECTION 3: INSTITUTIONAL POSTURE (NEW) ---
            uiState.marketPosture?.let { postureData ->
                item {
                    InstitutionalPostureSection(postureData = postureData)
                }
            }
        }
    }
}