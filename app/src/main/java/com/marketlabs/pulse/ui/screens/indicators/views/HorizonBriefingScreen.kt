package com.marketlabs.pulse.ui.screens.indicators.views

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.marketlabs.pulse.R
import com.marketlabs.pulse.storage.model.indicators.DomainAiSynthesis
import com.marketlabs.pulse.storage.model.indicators.DomainHorizon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HorizonBriefingsScreen(
    aiSynthesis: DomainAiSynthesis?,
    onBackClick: () -> Unit
) {
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    val tabs = listOf(
        stringResource(id = R.string.tab_short_term),
        stringResource(id = R.string.tab_medium_term),
        stringResource(id = R.string.tab_long_term)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = statusBarHeight)
    ) {
        // --- TOP APP BAR ---
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text = "Horizon Briefings",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(painter = painterResource(id = R.drawable.ic_chevron_forward), contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        )

        if (aiSynthesis == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Briefing data unavailable.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            return
        }

        // --- TABS ---
        PrimaryTabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.primary,
            indicator = {
                TabRowDefaults.PrimaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(selectedTabIndex),
                    width = dimensionResource(id = R.dimen.tab_indicator_width),
                    height = dimensionResource(id = R.dimen.tab_indicator_height),
                    shape = RoundedCornerShape(
                        topStart = dimensionResource(id = R.dimen.tab_indicator_corner),
                        topEnd = dimensionResource(id = R.dimen.tab_indicator_corner)
                    )
                )
            },
            divider = { HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)) }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                )
            }
        }

        // --- DYNAMIC AI HORIZON BRIEFING ---
        val currentHorizon = when (selectedTabIndex) {
            0 -> aiSynthesis.shortTerm
            1 -> aiSynthesis.mediumTerm
            else -> aiSynthesis.longTerm
        }

        AnimatedContent(
            targetState = currentHorizon,
            transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
            label = "horizon_animation",
            modifier = Modifier.fillMaxSize()
        ) { horizon ->
            if (horizon != null) {
                HorizonDetailView(horizon)
            }
        }
    }
}

@Composable
fun HorizonDetailView(horizon: DomainHorizon) {
    val paddingLarge = dimensionResource(id = R.dimen.padding_large)
    val paddingMedium = dimensionResource(id = R.dimen.padding_medium)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(paddingLarge)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "${stringResource(id = R.string.outlook_label)}: ${horizon.riskLevel}",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = when (horizon.riskLevel.uppercase()) {
                    "HIGH" -> MaterialTheme.colorScheme.error
                    "MODERATE" -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.secondary
                }
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "${stringResource(id = R.string.driver_label)}: ${horizon.keyDriver}",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_large)))

        Text(
            text = horizon.briefing,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.2f
        )
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_xlarge)))

        // The Playbook Action Box
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                .padding(paddingLarge)
        ) {
            Text(
                text = stringResource(id = R.string.playbook_title).uppercase(),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = horizon.whatToDo,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.2f
            )
        }
    }
}