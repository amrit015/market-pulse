package com.marketlabs.pulse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.marketlabs.pulse.ui.navigation.PulseNavGraph
import com.marketlabs.pulse.ui.theme.MarketPulseTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Action: Enable edge-to-edge layout at the activity level.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Action: Enable drawing behind system bars for the "Gotham" overlay feel
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            // Action: Bind the custom PulseTheme to the activity lifecycle
            MarketPulseTheme {
                // Action: Initialize the Compose navigation controller
                PulseNavGraph()
            }
        }
    }
}