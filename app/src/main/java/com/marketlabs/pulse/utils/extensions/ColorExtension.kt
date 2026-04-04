import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.marketlabs.pulse.ui.theme.PulseStatusColors
import com.marketlabs.pulse.utils.enums.SignalColor // Make sure this path matches your project structure

/**
 * Maps a generic color string from the domain/backend to our dynamic UI text/icon colors.
 */

@Composable
fun SignalColor?.toColor(): Color {
    return when (this) {
        SignalColor.GREEN -> PulseStatusColors.BullishText
        SignalColor.YELLOW -> PulseStatusColors.NeutralText
        SignalColor.RED -> PulseStatusColors.BearishText
        SignalColor.UNKNOWN, null -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}

@Composable
fun SignalColor?.toBgColor(): Color {
    return when (this) {
        SignalColor.GREEN -> PulseStatusColors.BullishBg
        SignalColor.YELLOW -> PulseStatusColors.NeutralBg
        SignalColor.RED -> PulseStatusColors.BearishBg
        SignalColor.UNKNOWN, null -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }
}