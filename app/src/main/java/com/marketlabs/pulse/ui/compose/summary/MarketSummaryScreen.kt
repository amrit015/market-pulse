package com.marketlabs.pulse.ui.compose.summary

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marketlabs.pulse.storage.model.summary.DominoEffect
import com.marketlabs.pulse.storage.model.summary.MacroItem
import com.marketlabs.pulse.storage.model.summary.MarketPulse
import com.marketlabs.pulse.storage.model.summary.NewsItem
import com.marketlabs.pulse.storage.model.summary.Verdict
import com.marketlabs.pulse.storage.model.summary.enums.ReportType
import com.marketlabs.pulse.storage.model.summary.enums.TradingCall
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.lazy.items

/**
 * The main screen for displaying the Market Pulse report.
 *
 * This Composable handles the layout of the entire report using a [LazyColumn].
 * It implements a "Safe UI" pattern where sections are only rendered if the
 * corresponding data is non-null.
 *
 * @param data The [MarketPulse] domain object containing the full report.
 * All fields in this object are nullable.
 */
@Composable
fun MarketSummaryScreen(
    data: MarketPulse,
    scaffoldPadding: PaddingValues
) {

    // Action: Extract the status bar height to use in contentPadding
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(
            top = statusBarHeight + 16.dp, // Initial offset so first item isn't covered
            bottom = scaffoldPadding.calculateBottomPadding() + 16.dp,
            start = 16.dp,
            end = 16.dp
        ),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 1. HEADER SECTION
        // We only show the header if we have a valid Report Type and Timestamp.
        val type = data.reportType
        val timestamp = data.timestamp
        if (type != null && timestamp != null) {
            item {
                HeaderSection(type, timestamp)
            }
        }

        // 2. LEAD STORIES
        // A list of top news items. We check for null or empty lists to avoid rendering empty headers.
        val stories = data.leadStories
        if (!stories.isNullOrEmpty()) {
            item { SectionTitle("🚨 Lead Stories") }
            items(stories) { story ->
                NewsCard(story)
            }
        }

        // 3. MACRO MIX
        // Broader economic factors. Like stories, we only render if the list is populated.
        val macros = data.macroMix
        if (!macros.isNullOrEmpty()) {
            item { SectionTitle("🌍 Macro Mix") }
            items(macros) { macro ->
                MacroCard(macro)
            }
        }

        // 4. DOMINO EFFECT
        // Displays the cause-and-effect relationship (Trigger -> Impact).
        data.dominoEffect?.let { domino ->
            item { DominoCard(domino) }
        }

        // 5. VERDICT CARD
        // The core analysis. If the verdict object is missing, we skip this section entirely.
        data.verdict?.let { verdict ->
            item { VerdictCard(verdict) }
        }

        // 6. ACTION FOOTER
        // The final "What to do now" advice. Only shown if the action string is not blank.
        data.verdict?.action?.let { action ->
            if (action.isNotBlank()) {
                item { ActionFooter(action) }
            }
        }
    }
}

// ---------------------------------------------------------
// COMPONENT LIBRARY
// ---------------------------------------------------------

/**
 * Displays the report metadata (Title, Type, and Date).
 *
 * @param type The type of report (e.g., DAILY, WEEKLY).
 * @param timestamp The epoch timestamp (in seconds) when the report was generated.
 */
@Composable
fun HeaderSection(type: ReportType, timestamp: Long) {
    Column {
        Text(
            text = type.label.uppercase(),
            style = MaterialTheme.typography.labelSmall, // Uses Inter Bold + Spacing
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Market Pulse",
            style = MaterialTheme.typography.headlineLarge // Uses Montserrat ExtraBold
        )
        // Convert seconds to milliseconds for Date object
        val date = Date(timestamp * 1000L)
        val format = SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault())

        Text(
            text = "Updated: ${format.format(date)}",
            style = MaterialTheme.typography.bodySmall, // Uses Inter Regular
            color = Color.Gray
        )
    }
}

/**
 * The primary dashboard card showing the Market Regime, Trading Call, and Technical Setup.
 *
 * Visual Logic:
 * - Changes background color based on the [TradingCall] (Green for Buy, Red for Sell, Orange for Neutral).
 * - If essential fields (Regime, Setup, Call) are missing, this composable renders nothing.
 *
 * @param verdict The [Verdict] object containing the analysis.
 */
@Composable
fun VerdictCard(verdict: Verdict) {
    // 🛡️ GUARD CLAUSE: Essential data check
    // If we don't have the core 3 enums, the card cannot be displayed meaningfully.
    val call = verdict.call ?: return
    val regime = verdict.regime ?: return
    val setup = verdict.setup ?: return

    // 🎨 Dynamic Styling
    val (bgColor, textColor) = when (call) {
        TradingCall.CONTRARIAN_BUY,
        TradingCall.ACCUMULATE ->
            Pair(Color(0xFFE8F5E9), Color(0xFF2E7D32)) // Soft Green Bg, Dark Green Text

        TradingCall.CONTRARIAN_SELL,
        TradingCall.SELL_AVOID,
        TradingCall.HEDGE_PROTECT ->
            Pair(Color(0xFFFFEBEE), Color(0xFFC62828)) // Soft Red Bg, Dark Red Text

        else ->
            Pair(Color(0xFFFFF3E0), Color(0xFFEF6C00)) // Soft Orange Bg, Dark Orange Text
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 1. Regime Badge (e.g., "HEALTHY UPTREND")
            Surface(
                color = textColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text(
                    text = regime.label,
                    color = textColor,
                    style = MaterialTheme.typography.labelSmall, // Uses Inter Bold
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            // 2. The Main Call (e.g., "ACCUMULATE")
            Text(
                text = call.label,
                style = MaterialTheme.typography.headlineSmall, // Uses Montserrat Bold
                color = textColor
            )

            // 3. Technical Setup (e.g., "OVERSOLD")
            Text(
                text = setup.label,
                style = MaterialTheme.typography.labelMedium, // Uses Inter Medium
                color = textColor.copy(alpha = 0.8f),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            HorizontalDivider(color = textColor.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(12.dp))

            // 4. Detailed Analysis Text
            // We use safe call + elvis operator to ensure we never crash on null string
            Text(
                text = verdict.analysis ?: "",
                style = MaterialTheme.typography.bodyLarge, // Uses Inter Regular for density
                color = Color.Black.copy(alpha = 0.8f)
            )
        }
    }
}

/**
 * Displays a single news item card.
 *
 * @param story The [NewsItem] to display. If the headline is missing, nothing is rendered.
 */
@Composable
fun NewsCard(story: NewsItem) {
    val headline = story.headline ?: return

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = headline,
                style = MaterialTheme.typography.titleMedium, // Uses Montserrat SemiBold
            )
            // Summary is optional
            story.summary?.let { summary ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodyMedium // Uses Inter Regular
                )
            }
        }
    }
}

/**
 * Displays the "Domino Effect" card, showing a causal relationship.
 *
 * Layout:
 * - Trigger: The event that started it.
 * - Impact: The result of that event.
 *
 * @param domino The [DominoEffect] object. Requires both 'trigger' and 'impact' to render.
 */
@Composable
fun DominoCard(domino: DominoEffect) {
    val trigger = domino.trigger ?: return
    val impact = domino.impact ?: return

    Card(
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🧩 THE DOMINO EFFECT",
                style = MaterialTheme.typography.labelLarge, // Uses Inter Medium
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            DominoRow("Trigger:", trigger)
            Spacer(modifier = Modifier.height(4.dp))
            DominoRow("Impact:", impact)
        }
    }
}

/**
 * Helper row for the DominoCard to align labels and values.
 */
@Composable
private fun DominoRow(label: String, value: String) {
    Row(verticalAlignment = Alignment.Top) {
        Text(
            text = label,
            modifier = Modifier.width(64.dp),
            style = MaterialTheme.typography.bodyMedium, // Uses Inter Bold + Spacing
            fontWeight = FontWeight.Bold
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium // Uses Inter Regular
        )
    }
}

/**
 * Displays a macro-economic factor with a tag (e.g., "Inflation", "Rates").
 *
 * @param item The [MacroItem] to display. Requires a headline to render.
 */
@Composable
fun MacroCard(item: MacroItem) {
    val headline = item.headline ?: return

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Tag Chip (only if tag exists)
        item.tag?.let { tag ->
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Text(
                    text = tag.label,
                    style = MaterialTheme.typography.labelSmall, // Uses Inter Bold
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = headline,
                style = MaterialTheme.typography.bodyLarge, // Uses Inter Regular
                fontWeight = FontWeight.SemiBold
            )
            item.summary?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium, // Uses Inter Regular
                    color = Color.Gray
                )
            }
        }
    }
}

/**
 * A prominent footer card showing the actionable advice for the user.
 *
 * @param action The action string to display.
 */
@Composable
fun ActionFooter(action: String) {
    Surface(
        color = MaterialTheme.colorScheme.primary,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("⚡", fontSize = 24.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "ACTION PLAN",
                    style = MaterialTheme.typography.labelSmall, // Uses Inter Bold + Spacing
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                )
                Text(
                    text = action,
                    style = MaterialTheme.typography.bodyLarge, // Uses Inter Regular
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * A standardized section title for the list.
 */
@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge, // Uses Montserrat Bold
        modifier = Modifier.padding(top = 8.dp)
    )
}