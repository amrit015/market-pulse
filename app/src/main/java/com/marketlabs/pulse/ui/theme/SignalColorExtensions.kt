package com.marketlabs.pulse.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.marketlabs.pulse.utils.enums.SignalColor

/**
 * 💡 THOUGHT PROCESS:
 * Replaces `utils/extensions/ColorExtension.kt`'s `SignalColor.toColor()`/`.toBgColor()` — same
 * mapping target (the domain enum), new token source (`LocalPulseColors` instead of the deleted
 * `PulseStatusColors`) and new names (`.textColor`/`.pillColor`, matching the Token Contract's own
 * `signal.*.text` / `signal.*.pill` vocabulary instead of the old ambiguous "Bg" naming).
 *
 * `UNKNOWN`/`null` fall back to `LocalPulseColors.current.signalUnknown` — a real color both
 * variants can share, rather than reaching into `MaterialTheme.colorScheme` for an unrelated
 * fallback the way the old extensions did.
 */
val SignalColor?.textColor: Color
    @Composable get() = when (this) {
        SignalColor.GREEN -> LocalPulseColors.current.signalBullishText
        SignalColor.YELLOW -> LocalPulseColors.current.signalNeutralText
        SignalColor.RED -> LocalPulseColors.current.signalBearishText
        SignalColor.UNKNOWN, null -> LocalPulseColors.current.signalUnknown
    }

val SignalColor?.pillColor: Color
    @Composable get() = when (this) {
        SignalColor.GREEN -> LocalPulseColors.current.signalBullishPill
        SignalColor.YELLOW -> LocalPulseColors.current.signalNeutralPill
        SignalColor.RED -> LocalPulseColors.current.signalBearishPill
        SignalColor.UNKNOWN, null -> LocalPulseColors.current.signalUnknown
    }
