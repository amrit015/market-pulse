package com.marketlabs.pulse.ui.screens.stocks.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.marketlabs.pulse.R
import com.marketlabs.pulse.storage.model.intraday.IntradaySeries
import com.marketlabs.pulse.storage.model.stocks.DomainConditionChip
import com.marketlabs.pulse.storage.model.stocks.StockPreview
import com.marketlabs.pulse.ui.components.PulseCard
import com.marketlabs.pulse.ui.components.PulseCardStyle
import com.marketlabs.pulse.ui.components.widgets.ChangeDirection
import com.marketlabs.pulse.ui.components.widgets.DirectionalChangePill
import com.marketlabs.pulse.ui.components.widgets.FavoriteStarToggle
import com.marketlabs.pulse.ui.components.widgets.SparklineChart
import com.marketlabs.pulse.ui.components.widgets.animateFlashColor
import com.marketlabs.pulse.ui.screens.stocks.detail.OutlinedBadge
import com.marketlabs.pulse.ui.screens.stocks.detail.sections.DeepDiveLabel
import com.marketlabs.pulse.ui.theme.LocalPulseColors
import com.marketlabs.pulse.ui.theme.MarketPulseTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import java.util.Locale
import kotlin.math.abs

/**
 * The Analysis tab's list card -- one `StockPreview` per tracked symbol. `PulseCard(DATA)`, the
 * same background every other data-display card in the app uses -- was `SYNTHESIS` (this card
 * leads with `plain_read`, an AI-generated interpretation of the day's data), but only a card
 * presenting a direct AI verdict/conclusion (Summary's `VerdictCard`, Indicators' AI Executive
 * Briefing) keeps that darker treatment now; a per-symbol list card reads like this app's other
 * list cards (Equities, News) even though its body text is AI-authored. The glyph+title treatment
 * itself (icon sized to the symbol text's own font size) is unrelated to card style and unchanged.
 *
 * Field-to-element mapping: symbol/name left, price/change pill
 * right (a small "LIVE" label above `price` while `isEquityOpen` -- same
 * `market_overview/market_state.is_equity_open` flag the Dashboard's hero badge reads -- since
 * `intradayPoller` only actually keeps `preview.price` live during regular market hours),
 * `plain_read` prose body, a muted `technical_setup` + chip-delta line, the top-4 condition
 * chips, and (only when `hasDirectNews` is true) a small NEWS footer line. A live `SparklineChart`
 * sized to `stock_preview_sparkline_space` sits between the symbol/name block and the price/change
 * block, its own fixed-width column in that header row -- `intradayStream` is fed by
 * `IntradayRepository` (backend-polled `/intraday/:symbol` bars), not the preview payload itself,
 * since `market_stock_previews` has no chart series of its own. The sparkline's baseline prefers
 * the intraday response's own `prev_close` (`IntradaySeries.previousClose`) and only falls back to
 * the preview's `previousClose` while no intraday data has loaded yet, so the dashed reference line
 * still has something to show immediately. `name` now wraps to 2 lines (was 1 + ellipsis) since the
 * symbol/name column gives up some of its width to the sparkline column next to it.
 *
 * A star toggle sits bottom-right, at the end of the footer row shared with `DeepDiveLabel` --
 * `isFavorite`/`onFavoriteClick` default to off/no-op so every existing call site (and every
 * `@Preview` below) keeps compiling without passing them. The card itself looks identical on every
 * tab (Favorites included) -- the Favorites tab's own visual distinction lives one level up, on
 * the `PulseTabRow` chip itself (`highlightedTabIndex`), not on the cards inside it.
 */
@Composable
fun StockPreviewCard(
    preview: StockPreview,
    onClick: () -> Unit,
    isEquityOpen: Boolean = false,
    isFavorite: Boolean = false,
    onFavoriteClick: () -> Unit = {},
    onTechnicalSetupClick: () -> Unit = {},
    isDeepDiveFlashing: Boolean = false,
    onDeepDiveClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    intradayStream: Flow<IntradaySeries?> = emptyFlow()
) {
    val pulseColors = LocalPulseColors.current
    val intradaySeries by intradayStream.collectAsStateWithLifecycle(initialValue = null)

    // 💡 Hoisted above `PulseCard` (was computed further down, inline for the pill only) so the
    // card's glow can reuse the exact same bullish/bearish/neutral read -- the price text's
    // refresh-flash below reuses it too, so all three never disagree.
    val direction = when {
        (preview.changePercent ?: 0.0) > 0 -> ChangeDirection.UP
        (preview.changePercent ?: 0.0) < 0 -> ChangeDirection.DOWN
        else -> ChangeDirection.FLAT
    }
    val (pillColor, textColor) = when (direction) {
        ChangeDirection.UP -> pulseColors.signalBullishPill to pulseColors.signalBullishText
        ChangeDirection.DOWN -> pulseColors.signalBearishPill to pulseColors.signalBearishText
        ChangeDirection.FLAT -> pulseColors.signalNeutralPill to pulseColors.signalNeutralText
    }
    // 💡 No pulse once the market's closed -- would otherwise read as "live" against a stale price.
    val glowColor = if (isEquityOpen && direction != ChangeDirection.FLAT) textColor else null

    PulseCard(
        style = PulseCardStyle.DATA,
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        glowColor = glowColor
    ) {
        Column(modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val symbolStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        val glyphSize = with(LocalDensity.current) { symbolStyle.fontSize.toDp() }

                        Icon(
                            painter = painterResource(id = R.drawable.ic_ai_sparkle_filled),
                            contentDescription = stringResource(id = R.string.stock_analysis_ai_glyph_content_description),
                            tint = pulseColors.accentPrimary,
                            modifier = Modifier.size(glyphSize)
                        )
                        Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_small)))
                        Text(
                            text = preview.symbol,
                            style = symbolStyle,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        // 💡 Disclosure badge -- flags a symbol that isn't a single-company stock
                        // (SPY as an ETF, ^GSPC as an index, ...) right next to the ticker, the same
                        // `OutlinedBadge` treatment `technicalSetup`/`DetailHeader` already use for
                        // "a classification, not a signal." Omitted for the plain "STOCK" case (the
                        // majority) and for a null/missing value (pre-backend-rollout cached rows) --
                        // only the exception needs flagging.
                        preview.assetType?.takeIf { it != "STOCK" }?.let { assetType ->
                            Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_small)))
                            OutlinedBadge(text = assetType)
                        }
                    }
                    preview.name?.let { name ->
                        // 💡 2 lines now (was 1 + ellipsis) -- this column gives up width to the
                        // sparkline next to it, so a long name is more likely to need the room.
                        Text(
                            text = name,
                            style = MaterialTheme.typography.bodySmall,
                            color = pulseColors.accentPrimary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_medium)))

                SparklineChart(
                    points = intradaySeries?.points.orEmpty(),
                    previousClose = intradaySeries?.previousClose ?: preview.previousClose,
                    modifier = Modifier
                        .width(dimensionResource(id = R.dimen.stock_preview_sparkline_width))
                        .height(dimensionResource(id = R.dimen.stock_preview_sparkline_space))
                )

                Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_medium)))

                Column(horizontalAlignment = Alignment.End) {
                    preview.price?.let { price ->
                        if (isEquityOpen) {
                            Text(
                                text = stringResource(id = R.string.stock_price_live_label),
                                style = MaterialTheme.typography.labelSmall,
                                color = pulseColors.onSurfaceMuted
                            )
                        }
                        Text(
                            text = "$${String.format(Locale.US, "%.2f", price)}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = animateFlashColor(
                                value = price,
                                flashColor = textColor,
                                restingColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                    preview.changePercent?.let { changePercent ->
                        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))
                        DirectionalChangePill(
                            changeText = "${String.format(Locale.US, "%.2f", abs(changePercent))}%",
                            direction = direction,
                            pillColor = pillColor,
                            contentColor = textColor
                        )
                    }
                }
            }

            preview.plainRead?.let { plainRead ->
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))
                Text(
                    text = plainRead,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // 💡 the delta counts didn't say which setup changed to which, just how many condition
            // chips shifted underneath it (a different, less direct signal). When the setup
            // itself changed since the last analysis, show "OLD → NEW" directly; otherwise, just
            // the current setup, same as before. `chipsAdded`/`chipsRemoved` still drive which
            // condition chips render as "new" in the row below
            val setupText = preview.technicalSetup?.let { setup ->
                val previousSetup = preview.previousSetup
                if (preview.setupChanged == true && previousSetup != null && previousSetup != setup) {
                    stringResource(
                        id = R.string.stock_analysis_setup_changed_format,
                        previousSetup.replace('_', ' ').uppercase(Locale.US),
                        setup.replace('_', ' ').uppercase(Locale.US)
                    )
                } else {
                    setup.replace('_', ' ').uppercase(Locale.US)
                }
            }
            // 💡 Same `OutlinedBadge` pill `DetailHeader` uses for `technicalSetup` on the Detail
            // screen (plain muted text would read as a lesser treatment for the same field). It's
            // tap-to-explain, same as Detail's own badge, opening the `stock_setups` glossary.
            setupText?.let {
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))
                OutlinedBadge(text = it, onClick = onTechnicalSetupClick)
            }

            val previewChips = preview.conditionChips?.let { chips ->
                selectPreviewChips(chips = chips, added = preview.chipsAdded.orEmpty().toSet())
            }
            if (!previewChips.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))
                ConditionChipRow(chips = previewChips)
            }

            // 💡 Always rendered (not just when a deep-dive date exists) -- the star needs a
            // consistent home on every card, bottom-right, and this footer row is it. `DeepDiveLabel`
            // renders nothing on a cold-start symbol (neither date set yet), but its `weight(1f)` box
            // still claims the row's leading space either way, so the star lands in the same spot
            // regardless of whether deep-dive text is showing next to it.
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_standard)))
            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                thickness = dimensionResource(id = R.dimen.border_thin)
            )
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_standard)))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    DeepDiveLabel(
                        deepAnalysisDate = preview.deepAnalysisDate,
                        nextDeepDiveTriggerDate = preview.nextDeepDiveTriggerDate,
                        isFlashing = isDeepDiveFlashing,
                        onClick = onDeepDiveClick
                    )
                }
                FavoriteStarToggle(
                    isFavorite = isFavorite,
                    onClick = onFavoriteClick,
                    contentDescription = stringResource(
                        id = if (isFavorite) R.string.stock_analysis_remove_favorite_content_description
                        else R.string.stock_analysis_add_favorite_content_description,
                        preview.symbol
                    ),
                    key = preview.symbol
                )
            }
        }
    }
}

// ============================================================================
// 🎨 PREVIEWS
// ============================================================================

private val mockPreview = StockPreview(
    symbol = "AMZN",
    lastSyncedTimestamp = 0L,
    name = "Amazon.com, Inc.",
    price = 274.48,
    changePercent = 0.82,
    plainRead = "Amazon's stock sits at \$274.48, trading 16.04% above its 200-day average, while its cash flow yield of 0.78% is barely above the 0.47% risk-free rate.",
    technicalSetup = "MEAN_REVERSION",
    previousSetup = "BREAKOUT",
    setupChanged = true,
    chipsAdded = listOf("Firm momentum", "Cheaper than its own history"),
    chipsRemoved = listOf("Overbought"),
    conditionChips = listOf(
        DomainConditionChip(label = "Firm momentum", direction = "BULLISH", category = "MOMENTUM"),
        DomainConditionChip(label = "Above all moving averages", direction = "BULLISH", category = "TREND"),
        DomainConditionChip(label = "Cheaper than its own history", direction = "BULLISH", category = "VALUATION"),
        DomainConditionChip(label = "Stretched from its average", direction = "BEARISH", category = "VOLATILITY")
    ),
    hasDirectNews = true,
    topHeadline = "Amazon vs. Celsius: Which Consumer Stock Is a Better Buy in 2026?"
)

@Preview(name = "Light", showBackground = true)
@Composable
private fun PreviewStockPreviewCardLight() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        StockPreviewCard(preview = mockPreview, onClick = {}, isEquityOpen = true)
    }
}

@Preview(name = "Dark", showBackground = true, backgroundColor = 0xFF0D0E12)
@Composable
private fun PreviewStockPreviewCardDark() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        StockPreviewCard(preview = mockPreview, onClick = {}, isEquityOpen = true)
    }
}

@Preview(name = "Favorited — Light", showBackground = true)
@Composable
private fun PreviewStockPreviewCardFavoritedLight() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        StockPreviewCard(preview = mockPreview, onClick = {}, isEquityOpen = true, isFavorite = true)
    }
}

@Preview(name = "ETF disclosure badge — Light", showBackground = true)
@Composable
private fun PreviewStockPreviewCardEtfLight() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        StockPreviewCard(preview = mockPreview.copy(symbol = "SPY", assetType = "ETF"), onClick = {}, isEquityOpen = true)
    }
}
