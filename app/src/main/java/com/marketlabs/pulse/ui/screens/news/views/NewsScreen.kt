package com.marketlabs.pulse.ui.screens.news.views

import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.marketlabs.pulse.R
import com.marketlabs.pulse.storage.model.news.MarketNews
import com.marketlabs.pulse.storage.model.news.NewsArticle
import com.marketlabs.pulse.ui.theme.PulseStatusColors.BearishBg
import com.marketlabs.pulse.ui.theme.PulseStatusColors.BearishText
import com.marketlabs.pulse.ui.theme.PulseStatusColors.BullishBg
import com.marketlabs.pulse.ui.theme.PulseStatusColors.BullishText
import com.marketlabs.pulse.ui.theme.PulseStatusColors.NeutralBg
import com.marketlabs.pulse.ui.theme.PulseStatusColors.NeutralText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NewsScreen(
    data: MarketNews,
    scaffoldPadding: PaddingValues,
    onArticleClick: (String) -> Unit
) {
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val paddingLarge = dimensionResource(id = R.dimen.padding_large)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(
            top = statusBarHeight + paddingLarge,
            bottom = scaffoldPadding.calculateBottomPadding() + paddingLarge,
            start = paddingLarge,
            end = paddingLarge
        ),
        verticalArrangement = Arrangement.spacedBy(paddingLarge)
    ) {
        // Header
        item { HeaderSection(data.lastUpdated) }

        // Empty State Check (if the list of stories inside the data object is empty)
        if (data.stories.isNullOrEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillParentMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(id = R.string.news_empty_state),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            // Render Articles safely
            items(data.stories) { article ->
                NewsArticleCard(
                    article = article,
                    onClick = { url -> onArticleClick(url) }
                )
            }
        }
    }
}

@Composable
fun HeaderSection(timestamp: Long) {
    Column {
        Text(
            text = stringResource(id = R.string.news_screen_title),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.padding_medium))
        )

        val date = Date(timestamp)
        val format = SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault())

        Text(
            text = stringResource(id = R.string.analyzed_at, format.format(date)),
            style = MaterialTheme.typography.bodySmall,
            // 💡 ACTION: Replaced hardcoded Color.Gray with Theme's semantic variant text color
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun NewsArticleCard(
    article: NewsArticle,
    onClick: (String) -> Unit
) {
    val headline = article.headline ?: "Market Update"
    val source = article.source ?: "News"
    val impactSummary = article.impactSummary ?: ""
    val sentiment = article.sentiment?.uppercase() ?: "NEUTRAL"
    val url = article.url ?: ""

    val (sentimentColor, sentimentBgColor) = when (sentiment) {
        "BULLISH" -> Pair(BullishText, BullishBg)
        "BEARISH" -> Pair(BearishText, BearishBg)
        else -> Pair(NeutralText, NeutralBg)
    }
    val paddingLarge = dimensionResource(id = R.dimen.padding_large)

    Card(
        // 💡 CHANGED: Matches Market Outlook and Summary Screen backgrounds
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_card)),
        modifier = Modifier
            .fillMaxWidth()
            // 💡 ACTION: Make the entire card clickable, firing the URI handler
            .clickable(enabled = url.isNotBlank()) {
                onClick(url) // open the webpages on the app webview
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = paddingLarge, bottom = paddingLarge)
        ) {
            // --- ROW 1: Source, Time, and Sentiment Badge ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = paddingLarge, end = paddingLarge),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = source.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    article.timestamp?.let { timestamp ->
                        Text(
                            text = " • ${getTimeAgo(timestamp)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    color = sentimentBgColor,
                    shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_chip))
                ) {
                    Text(
                        text = sentiment,
                        style = MaterialTheme.typography.labelSmall,
                        color = sentimentColor,
                        modifier = Modifier.padding(
                            horizontal = dimensionResource(id = R.dimen.padding_medium),
                            vertical = dimensionResource(id = R.dimen.padding_tiny)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))

            // --- ROW 2: Headline and Chevron ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = paddingLarge, end = paddingLarge),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // --- HEADLINE ---
                Text(
                    text = headline,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    painter = painterResource(id = R.drawable.ic_chevron_forward),
                    contentDescription = "View Details",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(dimensionResource(R.dimen.padding_large))
                )

            }

            // 💡 ACTION: Faint line divider between Headline and Content
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_large)))
            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                thickness = dimensionResource(id = R.dimen.border_thin)
            )
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_large)))

            // --- ROW 3: Content Column (Summary and Tags) ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = paddingLarge, end = paddingLarge),
            ) {
                // IMPACT
                if (impactSummary.isNotBlank()) {
                    Text(
                        text = impactSummary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // TAGS
                if (!article.tags.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))
                    Row(horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_small))) {
                        article.tags.take(3).forEach { tag ->
                            Text(
                                text = tag,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 💡 Helper function to convert epoch time to "2 hours ago", "Just now", etc.
 */
@Composable
private fun getTimeAgo(timestamp: Long): String {
    val now = System.currentTimeMillis()
    return DateUtils.getRelativeTimeSpanString(
        timestamp,
        now,
        DateUtils.MINUTE_IN_MILLIS,
        DateUtils.FORMAT_ABBREV_RELATIVE
    ).toString()
}