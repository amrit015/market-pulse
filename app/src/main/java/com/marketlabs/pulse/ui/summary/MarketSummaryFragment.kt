package com.marketlabs.pulse.ui.summary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import com.marketlabs.pulse.ui.summary.compose.MarketSummaryRoute
import com.marketlabs.pulse.ui.theme.MarketPulseTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MarketSummaryFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            // Critical for Fragments: Dispose composition when ViewLifecycle is destroyed
            // This prevents memory leaks and crashes during fragment transitions.
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
            )

            setContent {
                // Apply your App Theme
                MarketPulseTheme {
                    // Call your Route (The Manager)
                    MarketSummaryRoute()
                }
            }
        }
    }
}