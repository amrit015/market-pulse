package com.marketlabs.pulse.ui.compose.news

import android.graphics.drawable.Icon
import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.marketlabs.pulse.R
import com.marketlabs.pulse.storage.model.news.MarketNews
import com.marketlabs.pulse.storage.model.news.NewsArticle
import com.marketlabs.pulse.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsScreen(
    data: MarketNews?,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    scaffoldPadding: PaddingValues
) {
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val paddingLarge = dimensionResource(id = R.dimen.padding_large)
    val paddingXLarge = dimensionResource(id = R.dimen.padding_xlarge)

    // 💡 ACTION: PullToRefreshBox is Material 3's modern swipe-to-refresh wrapper
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = statusBarHeight + paddingLarge,
                bottom = scaffoldPadding.calculateBottomPadding() + paddingLarge,
                start = paddingLarge,
                end = paddingLarge
            ),
            verticalArrangement = Arrangement.spacedBy(paddingLarge)
        ) {
            // Header
            item {
                Text(
                    text = stringResource(id = R.string.news_screen_title),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.padding_medium))
                )
            }

            // Empty State
            if (data?.stories.isNullOrEmpty()) {
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
                    NewsArticleCard(article = article)
                }
            }
        }
    }
}

@Composable
fun NewsArticleCard(article: NewsArticle) {
    val headline = article.headline ?: "Market Update"
    val source = article.source ?: "News"
    val impactSummary = article.impactSummary ?: ""
    val sentiment = article.sentiment?.uppercase() ?: "NEUTRAL"
    val url = article.url ?: ""

    // 💡 ACTION: Grab the Compose URI handler to open web links natively
    val uriHandler = LocalUriHandler.current

    val (sentimentColor, sentimentBgColor) = when (sentiment) {
        "BULLISH" -> Pair(VerdictBuyText, VerdictBuyBackground)
        "BEARISH" -> Pair(VerdictSellText, VerdictSellBackground)
        else -> Pair(VerdictNeutralText, VerdictNeutralBackground)
    }
    val paddingLarge = dimensionResource(id = R.dimen.padding_large)

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_card)),
        modifier = Modifier
            .fillMaxWidth()
            // 💡 ACTION: Make the entire card clickable, firing the URI handler
            .clickable(enabled = url.isNotBlank()) {
                try {
                    uriHandler.openUri(url)
                } catch (e: Exception) {
                    // Failsafe in case of a malformed URL
                }
            }
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(top = paddingLarge, bottom = paddingLarge)) {
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
                    // Note: If your Compose version is older and doesn't support AutoMirrored,
                    // just use Icons.Rounded.KeyboardArrowRight
                    painter = painterResource(id = R.drawable.ic_chevron_forward),
                    contentDescription = "Read full article",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )

            }

            // 💡 ACTION: Faint line divider between Headline and Content
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))
            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                thickness = dimensionResource(id = R.dimen.border_thin)
            )
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))

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
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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