package com.marketlabs.pulse.ui.screens.news.views

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.marketlabs.pulse.R
import com.marketlabs.pulse.storage.model.news.MarketNews
import com.marketlabs.pulse.storage.model.news.NewsArticle
import com.marketlabs.pulse.ui.theme.PulseStatusColors.BearishBg
import com.marketlabs.pulse.ui.theme.PulseStatusColors.BearishText
import com.marketlabs.pulse.ui.theme.PulseStatusColors.BullishBg
import com.marketlabs.pulse.ui.theme.PulseStatusColors.BullishText
import com.marketlabs.pulse.ui.theme.PulseStatusColors.NeutralBg
import com.marketlabs.pulse.ui.theme.PulseStatusColors.NeutralText
import com.marketlabs.pulse.utils.extensions.toRelativeTimeString
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NewsScreen(
    data: MarketNews,
    scaffoldPadding: PaddingValues,
    onArticleClick: (String) -> Unit,
    highlightedArticleUrl: String? = null // Added with Claude Code assistance.
) {
    val paddingLarge = dimensionResource(id = R.dimen.padding_large)
    val listState = rememberLazyListState()

    // Added with Claude Code assistance: scroll the respective card into view when a Dashboard
    // news preview card was tapped. Item index is offset by 1 for the header item above the list.
    LaunchedEffect(highlightedArticleUrl, data.stories) {
        val targetIndex = data.stories?.indexOfFirst { it.url == highlightedArticleUrl } ?: -1
        if (targetIndex >= 0) {
            listState.animateScrollToItem(targetIndex + 1)
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(
            top = paddingLarge,
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
                    onClick = { url -> onArticleClick(url) },
                    isHighlighted = article.url != null && article.url == highlightedArticleUrl
                )
            }
        }
    }
}

/**
 * Just the "Analyzed at [time]" line now — the icon+title row that used to sit above it duplicated
 * the `TopAppBar`'s own "Market News" title once News became a pushed screen with its own toolbar,
 * so it was removed (Added with Claude Code assistance).
 */
@Composable
fun HeaderSection(timestamp: Long) {
    val date = Date(timestamp)
    val format = SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault())

    Text(
        text = stringResource(id = R.string.analyzed_at, format.format(date)),
        style = MaterialTheme.typography.bodySmall,
        // 💡 ACTION: Replaced hardcoded Color.Gray with Theme's semantic variant text color
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
fun NewsArticleCard(
    article: NewsArticle,
    onClick: (String) -> Unit,
    isHighlighted: Boolean = false // Added with Claude Code assistance.
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
    val cardShape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_card))

    Card(
        // 💡 CHANGED: Matches Market Outlook and Summary Screen backgrounds
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
        ),
        shape = cardShape,
        modifier = Modifier
            .fillMaxWidth()
            // Added with Claude Code assistance: frames the card a Dashboard news preview linked to.
            .let {
                if (isHighlighted) {
                    it.border(dimensionResource(id = R.dimen.border_medium), MaterialTheme.colorScheme.primary, cardShape)
                } else {
                    it
                }
            }
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
                            text = " • ${timestamp.toRelativeTimeString()}",
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

// --- Dashboard news preview (condensed cards embedded on the Overview screen) ---
// Added with Claude Code assistance.

/**
 * Condensed news section for the Dashboard: a "See all" chevron to the full News tab, plus up to
 * a few `NewsPreviewCard`s. Reuses the same `NewsArticle` domain model and sentiment/time
 * formatting as the full `NewsScreen` rather than duplicating a parallel preview model.
 */
@Composable
fun NewsPreviewSection(
    articles: List<NewsArticle>,
    onArticleClick: (String) -> Unit,
    onSeeAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = R.string.dashboard_section_news),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )

            IconButton(onClick = onSeeAllClick) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_chevron_forward),
                    contentDescription = stringResource(id = R.string.dashboard_news_see_all),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_small))) {
            articles.forEach { article ->
                NewsPreviewCard(article = article, onClick = onArticleClick)
            }
        }
    }
}

/**
 * A single condensed, dense news card: sentiment badge, relative timestamp, and the full
 * (unclipped, smaller-styled) headline only — no source/impact/tags, unlike `NewsArticleCard`.
 */
@Composable
fun NewsPreviewCard(
    article: NewsArticle,
    onClick: (String) -> Unit
) {
    val headline = article.headline ?: "Market Update"
    val sentiment = article.sentiment?.uppercase() ?: "NEUTRAL"
    val url = article.url ?: ""

    val (sentimentColor, sentimentBgColor) = when (sentiment) {
        "BULLISH" -> Pair(BullishText, BullishBg)
        "BEARISH" -> Pair(BearishText, BearishBg)
        else -> Pair(NeutralText, NeutralBg)
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_card)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = url.isNotBlank()) { onClick(url) }
    ) {
        Column(modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_medium))) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = sentimentBgColor,
                    shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_chip))
                ) {
                    Text(
                        text = sentiment,
                        style = MaterialTheme.typography.labelSmall,
                        color = sentimentColor,
                        modifier = Modifier.padding(
                            horizontal = dimensionResource(id = R.dimen.padding_small),
                            vertical = dimensionResource(id = R.dimen.padding_tiny)
                        )
                    )
                }

                article.timestamp?.let { timestamp ->
                    Text(
                        text = timestamp.toRelativeTimeString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_tiny)))

            // Smaller style than NewsArticleCard's titleMedium, and no maxLines/ellipsis — the
            // whole headline is shown, wrapping onto as many lines as it needs.
            Text(
                text = headline,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun PreviewNewsPreviewSection() {
    MaterialTheme {
        NewsPreviewSection(
            articles = listOf(
                NewsArticle(
                    headline = "Fed signals potential rate cut in September amid cooling inflation data",
                    sentiment = "BULLISH",
                    timestamp = System.currentTimeMillis() - (2 * 60 * 60 * 1000)
                ),
                NewsArticle(
                    headline = "Tech sector faces headwinds as chip export restrictions tighten",
                    sentiment = "BEARISH",
                    timestamp = System.currentTimeMillis() - (5 * 60 * 60 * 1000)
                )
            ),
            onArticleClick = {},
            onSeeAllClick = {},
            modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))
        )
    }
}