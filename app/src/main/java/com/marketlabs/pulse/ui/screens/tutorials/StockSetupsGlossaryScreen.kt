package com.marketlabs.pulse.ui.screens.tutorials

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.marketlabs.pulse.R
import com.marketlabs.pulse.core.glossary.MarketGlossaryProvider
import com.marketlabs.pulse.ui.components.DocumentSectionsScreen
import com.marketlabs.pulse.ui.theme.MarketPulseTheme

/**
 * The Stock Analysis deck's "See all indicators" target: every per-stock technical setup
 * (Breakout, Mean Reversion, Range Bound, ...) with its definition, read from the same
 * `stock_setups` glossary the tappable status pills on the Analysis list open.
 */
@Composable
fun StockSetupsGlossaryScreen(onNavigateUp: () -> Unit) {
    val context = LocalContext.current
    val sections = MarketGlossaryProvider.get(context).stockSetups.map { it.term to it.definition }

    DocumentSectionsScreen(
        title = stringResource(id = R.string.tutorials_stock_setups_glossary_title),
        sections = sections,
        onNavigateUp = onNavigateUp
    )
}

// ============================================================================
// 🎨 PREVIEWS
// ============================================================================

@Preview(name = "Light", showBackground = true)
@Composable
private fun PreviewStockSetupsGlossaryScreenLight() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        StockSetupsGlossaryScreen(onNavigateUp = {})
    }
}
