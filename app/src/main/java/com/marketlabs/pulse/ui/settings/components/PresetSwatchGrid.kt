package com.marketlabs.pulse.ui.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import com.marketlabs.pulse.R
import com.marketlabs.pulse.ui.theme.MarketPulseTheme

/**
 * 2-column grid for ONE mode's presets (all light, or all dark) -- the single-section counterpart
 * to [ThemePickerGrid]'s "light column | dark column" layout, used now that the picker screen
 * (ThemePickerScreen.kt) shows Light and Dark as two separately labeled, vertically stacked
 * sections instead of one combined grid. Same plain `Row`-per-pair technique, chunking a flat list
 * by 2 -- correct here (unlike the bug [ThemePickerGrid]'s own doc comment describes) since every
 * item in [presets] is already the same mode, so there's no light/dark interleaving to account for.
 */
@Composable
fun PresetSwatchGrid(
    presets: List<MarketPulseTheme>,
    selectedTheme: MarketPulseTheme,
    onPresetSelected: (MarketPulseTheme) -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = dimensionResource(id = R.dimen.padding_medium)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
        presets.chunked(2).forEach { rowPresets ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing)
            ) {
                rowPresets.forEach { preset ->
                    PresetSwatchCard(
                        preset = preset,
                        isSelected = preset == selectedTheme,
                        onClick = { onPresetSelected(preset) },
                        modifier = Modifier.weight(1f)
                    )
                }
                // Odd count (5 presets -> last row has 1) -- keep the lone card from stretching to
                // fill the row by giving the empty slot equal weight, same fix ThemePickerGrid used.
                if (rowPresets.size == 1) {
                    Row(modifier = Modifier.weight(1f)) {}
                }
            }
        }
    }
}

// ============================================================================
// 🎨 PREVIEWS
// ============================================================================

private val lightPresets = MarketPulseTheme.entries.filter { !it.isDark }
private val darkPresets = MarketPulseTheme.entries.filter { it.isDark }

@Preview(name = "Light presets", showBackground = true)
@Composable
private fun PreviewPresetSwatchGridLight() {
    PresetSwatchGrid(
        presets = lightPresets,
        selectedTheme = MarketPulseTheme.NAVY,
        onPresetSelected = {}
    )
}

@Preview(name = "Dark presets", showBackground = true, backgroundColor = 0xFF0D0E12)
@Composable
private fun PreviewPresetSwatchGridDark() {
    PresetSwatchGrid(
        presets = darkPresets,
        selectedTheme = MarketPulseTheme.LILAC,
        onPresetSelected = {}
    )
}
