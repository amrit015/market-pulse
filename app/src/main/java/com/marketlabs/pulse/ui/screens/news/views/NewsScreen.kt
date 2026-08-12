package com.marketlabs.pulse.ui.screens.news.views

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.marketlabs.pulse.ui.components.PulseCard
import com.marketlabs.pulse.ui.components.PulseCardStyle
import com.marketlabs.pulse.ui.components.widgets.SignalPill
import com.marketlabs.pulse.ui.theme.LocalPulseColors
import com.marketlabs.pulse.ui.theme.PulseColors
import com.marketlabs.pulse.utils.extensions.toRelativeTimeString
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 💡 Both NewsArticleCard and NewsPreviewCard resolve the same sentiment -> (text, pill) pair --
 * factored out once rather than duplicated. The sentiment chip stays signal-colored (bullish/
 * bearish/neutral) even though the card around it wears the accent tint -- the card is curated
 * synthesis, the chip inside it is still a real market read, and those are different things.
 */
private fun sentimentColors(pulseColors: PulseColors, sentiment: String): Pair<androidx.compose.ui.graphics.Color, androidx.compose.ui.graphics.Color> {
    return when (sentiment) {
        "BULLISH" -> pulseColors.signalBullishText to pulseColors.signalBullishPill
        "BEARISH" -> pulseColors.signalBearishText to pulseColors.signalBearishPill
        else -> pulseColors.signalNeutralText to pulseColors.signalNeutralPill
    }
}

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

    val pulseColors = LocalPulseColors.current
    val (sentimentColor, sentimentBgColor) = sentimentColors(pulseColors, sentiment)
    val paddingLarge = dimensionResource(id = R.dimen.padding_large)
    val cardShape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_card))

    // 💡 News cards are curated + AI-filtered, not raw feed -- SYNTHESIS style, same as the AI
    // briefing card, so a reader can tell interpreted/curated content apart from raw market data at
    // a glance. The highlighted border (thicker, full accent) draws on top of PulseCard's own
    // hairline border -- Added with Claude Code assistance: frames the card a Dashboard news
    // preview linked to.
    PulseCard(
        style = PulseCardStyle.SYNTHESIS,
        shape = cardShape,
        modifier = Modifier
            .fillMaxWidth()
            .let {
                if (isHighlighted) {
                    it.border(dimensionResource(id = R.dimen.border_medium), pulseColors.accentPrimary, cardShape)
                } else {
                    it
                }
            },
        onClick = if (url.isNotBlank()) ({ onClick(url) }) else null
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
                        color = pulseColors.accentPrimary
                    )
                    article.timestamp?.let { timestamp ->
                        Text(
                            text = " • ${timestamp.toRelativeTimeString()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                SignalPill(text = sentiment, pillColor = sentimentBgColor, contentColor = sentimentColor)
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
                // 💡 Was `colorScheme.secondary` (mapped to the muted onSurfaceMuted tone) -- the
                // same fix already applied to NewsPreviewCard's headline, now applied here too.
                // Card titles are always onSurface (dark-on-light/white-on-dark) across this app.
                // 💡 Was `titleMedium` (17sp) with no weight override, sitting at the same size as
                // Equities' bold card title but one weight step lighter (semi-bold) -- read as an
                // inconsistent size at a glance even though the sp value matched. Every
                // curated/AI-content card title in the app now uses `titleSmall` (15sp,
                // semi-bold) as its own distinct, consistent tier, separate from the DATA-style
                // cards (Equities, VIX, Indicators), which keep their bold 17sp title.
                Text(
                    text = headline,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    painter = painterResource(id = R.drawable.ic_chevron_forward),
                    contentDescription = "View Details",
                    tint = pulseColors.accentPrimary,
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
                                color = pulseColors.accentPrimary.copy(alpha = 0.8f),
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
                color = LocalPulseColors.current.accentPrimary
            )

            IconButton(onClick = onSeeAllClick) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_chevron_forward),
                    contentDescription = stringResource(id = R.string.dashboard_news_see_all),
                    tint = LocalPulseColors.current.accentPrimary
                )
            }
        }

        // 💡 Was `padding_small` (4dp) -- read as cramped next to the more generously spaced
        // sections above it on the same screen. `padding_standard` (12dp) matches the gap other
        // multi-card groups on Dashboard use between siblings.
        Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_standard))) {
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

    val pulseColors = LocalPulseColors.current
    val (sentimentColor, sentimentBgColor) = sentimentColors(pulseColors, sentiment)

    // 💡 SYNTHESIS style -- matches NewsArticleCard's above.
    PulseCard(
        style = PulseCardStyle.SYNTHESIS,
        modifier = Modifier.fillMaxWidth(),
        onClick = if (url.isNotBlank()) ({ onClick(url) }) else null
    ) {
        // 💡 Was `padding_medium` (8dp) -- bumped to `padding_large` (16dp), matching the inner
        // padding every other card on this screen (AssetCard, VixFullWidthCard, etc.) already uses.
        Column(modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SignalPill(text = sentiment, pillColor = sentimentBgColor, contentColor = sentimentColor)

                article.timestamp?.let { timestamp ->
                    Text(
                        text = timestamp.toRelativeTimeString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_tiny)))

            // No maxLines/ellipsis -- the whole headline is shown, wrapping onto as many lines as
            // it needs.
            // 💡 Was `colorScheme.secondary`, an unset M3 role that falls back to Material's own
            // baseline default rather than anything this app's token set defines -- it read as a
            // washed-out, off-brand purple instead of a real headline color. Switched to `onSurface`,
            // the same dark-on-light/light-on-dark neutral every other card's title text uses.
            // 💡 Dropped the `.copy(fontWeight = Bold)` override -- `titleSmall` (15sp, semi-bold)
            // is the consistent title tier every curated/AI-content card title uses now, and this
            // one was the odd one out at bold instead of semi-bold.
            Text(
                text = headline,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
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