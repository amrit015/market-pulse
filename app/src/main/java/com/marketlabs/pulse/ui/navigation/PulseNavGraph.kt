package com.marketlabs.pulse.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.marketlabs.pulse.R
import com.marketlabs.pulse.ui.components.PulseWebViewScreen
import com.marketlabs.pulse.ui.screens.dashboard.detail.AssetDetailRoute
import com.marketlabs.pulse.ui.screens.dashboard.views.DashboardRoute
import com.marketlabs.pulse.ui.screens.indicators.detail.MetricDetailRoute
import com.marketlabs.pulse.ui.screens.indicators.views.IndicatorHorizonsRoute
import com.marketlabs.pulse.ui.screens.indicators.views.IndicatorsRoute
import com.marketlabs.pulse.ui.screens.insights.glossary.GlossaryDetailRoute
import com.marketlabs.pulse.ui.screens.insights.views.InsightsRoute
import com.marketlabs.pulse.ui.screens.insights.views.InsightsTab
import com.marketlabs.pulse.ui.screens.legal.LegalAcceptanceRoute
import com.marketlabs.pulse.ui.screens.legal.PrivacyPolicyScreen
import com.marketlabs.pulse.ui.screens.legal.TermsConditionsScreen
import com.marketlabs.pulse.ui.screens.news.views.NewsRoute
import com.marketlabs.pulse.ui.screens.onboarding.OnboardingCarouselScreen
import com.marketlabs.pulse.ui.screens.stocks.deepdive.DeepDiveRoute
import com.marketlabs.pulse.ui.screens.stocks.detail.StockDetailRoute
import com.marketlabs.pulse.ui.screens.stocks.detail.timeline.ResolvedCallsListRoute
import com.marketlabs.pulse.ui.screens.stocks.detail.timeline.TechnicalTimelineListRoute
import com.marketlabs.pulse.ui.screens.stocks.views.StockAnalysisRoute
import com.marketlabs.pulse.ui.screens.summary.views.MarketSummaryRoute
import com.marketlabs.pulse.ui.screens.tutorials.TutorialsDataLimitationsScreen
import com.marketlabs.pulse.ui.screens.tutorials.TutorialsGaugeAnatomyScreen
import com.marketlabs.pulse.ui.screens.tutorials.ConceptArticle
import com.marketlabs.pulse.ui.components.tutorials.Mechanism
import com.marketlabs.pulse.ui.screens.tutorials.MechanismDeckScreen
import com.marketlabs.pulse.ui.screens.tutorials.StockSetupsGlossaryScreen
import com.marketlabs.pulse.ui.screens.tutorials.TutorialsConceptScreen
import com.marketlabs.pulse.ui.screens.tutorials.TutorialsArticleScreen
import com.marketlabs.pulse.ui.screens.tutorials.TutorialsGaugesRoute
import com.marketlabs.pulse.ui.screens.tutorials.TutorialsHubScreen
import com.marketlabs.pulse.ui.settings.AboutRoute
import com.marketlabs.pulse.ui.settings.ComingUpScreen
import com.marketlabs.pulse.ui.settings.DataSyncScreen
import com.marketlabs.pulse.ui.settings.NotificationsRoute
import com.marketlabs.pulse.ui.settings.SettingsRoute
import com.marketlabs.pulse.ui.settings.ThemePickerRoute
import com.marketlabs.pulse.utils.enums.ReportType
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

private const val NavTransitionFadeDurationMs = 200

/** Store Navigation Route constants */
object PulseRoutes {
    const val MARKET_SUMMARY = "market_summary"
    const val MARKET_OVERVIEW = "market_overview"
    const val MARKET_INDICATORS = "market_indicators"
    const val MARKET_INSIGHTS = "market_insights"

    // Not a bottom-nav tab: only reachable by pushing from the Dashboard's news preview.
    const val MARKET_NEWS = "market_news"

    // The Analysis tab on the bottom bar.
    const val MARKET_ANALYSIS = "market_analysis"

    // Pushed from a StockPreviewCard tap on the Analysis tab. "symbol" is a required nav argument,
    // not a query param -- see StockDetailViewModel's SavedStateHandle read.
    const val STOCK_ANALYSIS_DETAIL = "stockAnalysis"

    // Pushed from the Deep Dive banner on Stock Detail. Its own full-screen destination, not a
    // tab on STOCK_ANALYSIS_DETAIL -- "symbol" is a required nav argument, same shape as
    // STOCK_ANALYSIS_DETAIL above.
    const val DEEP_DIVE_DETAIL = "deepDiveDetail"

    // Pushed from the "View More" row at the bottom of Stock Detail's Timeline tab (capped-to-7
    // Resolved Calls / Technical Timeline cards). "symbol" is a required nav argument,
    // same shape as STOCK_ANALYSIS_DETAIL above.
    const val RESOLVED_CALLS_LIST = "resolvedCallsList"
    const val TECHNICAL_TIMELINE_LIST = "technicalTimelineList"

    // Reached from the gear icon on the global top bar.
    const val SETTINGS = "settings"

    // Pushed from the Indicators tab's "Horizons" entry card. Was local Compose state inside
    // IndicatorsScreen.kt; promoted to a real destination so it gets the same pushed-screen
    // treatment as Settings/News/Stock Detail -- its own header, no global top bar or floating
    // nav stacked underneath it.
    const val INDICATOR_HORIZONS = "indicator_horizons"

    // Pushed from a dashboard tile tap (indices/sectors/crypto/commodities/VIX/sentiment) on the
    // Overview tab. "symbol" is a required nav argument, not a query param, same shape as
    // STOCK_ANALYSIS_DETAIL above.
    const val ASSET_DETAIL = "assetDetail"

    // Pushed from an indicator card tap on the Indicators tab. "metricId" is a required nav
    // argument, same shape as ASSET_DETAIL
    // above. Metric ids are plain snake_case (e.g. "pe_ratio"), so unlike ASSET_DETAIL's symbols
    // this doesn't need URL-encoding.
    const val METRIC_DETAIL = "metricDetail"

    // Pushed from a whole-CARD tap on the Positioning/Posture screens (a per-card rather than
    // per-value tap target).
    // "metricIds" is a comma-joined list of dotted core/glossary keys (a card can cover more than
    // one entry -- a COT contract's % OI + percentile; a short-interest instrument's days-to-cover
    // + shares + mom-change), "title"/"description"/"status" are the pushed screen's heading, its
    // "what is this card" intro text, and its live status (for band highlighting) respectively.
    // Deliberately its OWN destination rather than reusing METRIC_DETAIL: that screen's ViewModel
    // is hard-wired to IndicatorsRepository/DomainUnifiedMetric and renders a history chart
    // neither Positioning nor Posture has via the API yet. "title"/"description"/"status" are
    // `Uri.encode()`-d (not `URLEncoder`, which turns spaces into "+" and collides with
    // Navigation's own automatic percent-decode of path segments -- see
    // GlossaryDetailViewModel's doc comment); "metricIds" itself needs no encoding, since every
    // core/glossary/ id is plain lowercase/dot/underscore.
    const val GLOSSARY_DETAIL = "glossaryDetail"

    // Precedes MARKET_OVERVIEW on a cold start until acceptedVersion >=
    // LegalRepository.CURRENT_LEGAL_VERSION -- see MainActivity's startDestination computation.
    // Not reachable any other way once accepted (no back-stack entry survives it).
    const val ONBOARDING_CAROUSEL = "onboarding_carousel"
    const val LEGAL_ACCEPTANCE = "legal_acceptance"

    // Reached from Settings' own Terms & Conditions/Privacy Policy rows (no intermediate "Legal"
    // hub screen) and from the acceptance screen's own links.
    const val TERMS_CONDITIONS = "terms_conditions"
    const val PRIVACY_POLICY = "privacy_policy"

    // Reached from Settings -> Tutorials.
    const val TUTORIALS_HUB = "tutorials_hub"
    // Route-argument prefixes -- the full route is "$TUTORIALS_DECK/{mechanism}" etc.
    const val TUTORIALS_DECK = "tutorials_deck"
    const val TUTORIALS_GLOSSARY = "tutorials_glossary"
    const val TUTORIALS_CONCEPT = "tutorials_concept"
    const val TUTORIALS_DATA_LIMITATIONS = "tutorials_data_limitations"
    const val TUTORIALS_GAUGE_ANATOMY = "tutorials_gauge_anatomy"
    // One shared route for all 40 `indicator_articles.json` entries -- "$TUTORIALS_ARTICLE/{key}".
    const val TUTORIALS_ARTICLE = "tutorials_article"

    // Reached from Settings' Notifications / Data & Sync / About / Coming Up rows.
    const val SETTINGS_NOTIFICATIONS = "settings_notifications"
    const val SETTINGS_DATA_SYNC = "settings_data_sync"
    const val SETTINGS_ABOUT = "settings_about"
    const val SETTINGS_COMING_UP = "settings_coming_up"
    const val SETTINGS_THEME_PICKER = "settings_theme_picker"

    /**
     * Resolves a `PulseRoutes` constant NAME (e.g. "MARKET_INDICATORS", as authored in
     * `learn_content.json`'s `<slot>_routes` fields) to its actual route string, for Learn's
     * "open in app" chips. Scoped to just the five bottom-nav tabs -- the only routes
     * [navigateToTab] can safely land on with no required arguments; `LearnContentJsonTest`
     * validates chip route names against the full set of constants on this object, but a chip
     * pointing at an argument-requiring route (e.g. `STOCK_ANALYSIS_DETAIL`) isn't supported by
     * this lookup and won't navigate -- not a need the first curated batch of chips has.
     */
    fun tabRouteByConstantName(name: String): String? = when (name) {
        "MARKET_SUMMARY" -> MARKET_SUMMARY
        "MARKET_OVERVIEW" -> MARKET_OVERVIEW
        "MARKET_INDICATORS" -> MARKET_INDICATORS
        "MARKET_INSIGHTS" -> MARKET_INSIGHTS
        "MARKET_ANALYSIS" -> MARKET_ANALYSIS
        else -> null
    }
}

/** * 💡 UPDATED: Added a second icon resource for the 'selected' filled state
 * (You will need to ensure you have these filled versions in your res/drawable folder)
 */
sealed class BottomNavItem(val route: String, val label: String, val unselectedIconRes: Int, val selectedIconRes: Int) {
    internal object Overview :
        BottomNavItem(PulseRoutes.MARKET_OVERVIEW, "Overview", R.drawable.ic_dashboard_grid, R.drawable.ic_dashboard_grid_filled)

    internal object Indicators :
        BottomNavItem(PulseRoutes.MARKET_INDICATORS, "Indicators", R.drawable.ic_indicators, R.drawable.ic_indicators_filled)

    internal object Summary :
        BottomNavItem(PulseRoutes.MARKET_SUMMARY, "Summary", R.drawable.ic_ai_sparkle, R.drawable.ic_ai_sparkle_filled)

    internal object Insights :
        BottomNavItem(PulseRoutes.MARKET_INSIGHTS, "Insights", R.drawable.ic_insights, R.drawable.ic_insights_filled)

    // The Analysis tab's bottom-bar item.
    internal object Analysis :
        BottomNavItem(PulseRoutes.MARKET_ANALYSIS, "Analysis", R.drawable.ic_analysis_trend, R.drawable.ic_analysis_trend_filled)
}

/**
 * A top-level `val` (rather than a local inside `PulseNavGraph()` rebuilt on every recomposition)
 * since `MainActivity` also needs this exact list to drive
 * `FloatingBottomNav`.
 */
val bottomNavItems = listOf(
    BottomNavItem.Overview,
    BottomNavItem.Indicators,
    BottomNavItem.Summary,
    BottomNavItem.Insights,
    BottomNavItem.Analysis
)

/**
 * NavHost-only. The `Scaffold`, the bottom `NavigationBar`, and `navController` creation all live
 * in `MainActivity` instead, since the global top bar and floating nav are app-wide chrome that
 * wraps this graph rather than something the graph owns itself — `MainActivity` needs
 * `navController` directly to drive `FloatingBottomNav`'s selected-tab state and `AppTopBar`'s
 * gear navigation.
 */
@Composable
fun PulseNavGraph(
    navController: NavHostController,
    scaffoldPadding: PaddingValues,
    // MainActivity reads LegalRepository.acceptedVersion synchronously before setContent (same
    // runBlocking-on-a-cached-DataStore-read pattern already used for the theme) and picks
    // ONBOARDING_CAROUSEL or MARKET_OVERVIEW -- computed once at cold start, not re-evaluated
    // reactively, since NavHost's own startDestination can't change after the graph is built.
    // Acceptance itself clears the onboarding back stack via popUpTo/inclusive instead.
    startDestination: String,
    // 💡 MainActivity owns the "did we arrive at Indicators via Drivers" flag -- this graph
    // reports the event up (onDriversNavigatedToIndicators) and reads the flag back down
    // (reachedIndicatorsFromDrivers) to place the BackHandler that consumes it, since that
    // handler has to live *inside* the Indicators destination's own content to take priority
    // over NavHost's own internal back handling (see the composable(MARKET_INDICATORS) block
    // below for why) -- it can't be composed up in MainActivity itself.
    onDriversNavigatedToIndicators: () -> Unit = {},
    reachedIndicatorsFromDrivers: Boolean = false,
    onIndicatorsBackHandled: () -> Unit = {},
    // 💡 Same up-reporting/read-back shape as the Drivers-to-Indicators flag above, for the Market
    // Sentiment card's jump to Insights (Posture) -- MainActivity owns this flag too, for the same
    // reason: it has to survive across this graph's own recompositions, and the BackHandler that
    // reads it has to live inside the Insights destination's own content (see the
    // composable(MARKET_INSIGHTS) block below).
    onMarketSentimentNavigatedToInsights: () -> Unit = {},
    reachedInsightsFromMarketSentiment: Boolean = false,
    onInsightsBackHandled: () -> Unit = {},
    // 💡 Same up-reporting shape as onDriversNavigatedToIndicators above -- MainActivity's top bar
    // needs the loaded ReportType to show "Daily Update"/"Weekend Update" instead of a fixed
    // "Summary", but can't reach into MarketSummaryViewModel's state directly.
    onSummaryReportTypeLoaded: (ReportType?) -> Unit = {},
    // MainActivity's brand splash is drawn over this graph, so the start destination is already
    // composed and running underneath it. Only the onboarding carousel cares: it holds its
    // first-slide text animation until the splash is gone.
    isSplashActive: Boolean = false,
    modifier: Modifier = Modifier
) {
    // One-shot signal set right before navigating to the News
    // tab from a Dashboard preview card, so NewsRoute knows which card to scroll to + highlight.
    // Hoisted here (not a nav argument) so the bottom-nav's plain "market_news" route pattern —
    // and its selected-tab matching in the bar above — stays untouched.
    var highlightedNewsArticleUrl by remember { mutableStateOf<String?>(null) }

    // Same one-shot hoisted-signal shape as highlightedNewsArticleUrl above, set right before
    // navigating to Insights from the Market Sentiment card so InsightsRoute lands on the Posture
    // tab -- not a nav argument, so PulseRoutes.MARKET_INSIGHTS's plain route string (and
    // FloatingBottomNav's route-equality tab-selected check) stays untouched.
    var initialInsightsTab by remember { mutableStateOf<InsightsTab?>(null) }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier.fillMaxSize(),
        // 💡 Applied once here (none of the routes below override it) so every push/pop in the
        // app gets the same fade instead of Navigation-Compose's raw default. Fade-only (no
        // slide) both ways.
        enterTransition = { fadeIn(tween(NavTransitionFadeDurationMs)) },
        exitTransition = { fadeOut(tween(NavTransitionFadeDurationMs)) },
        popEnterTransition = { fadeIn(tween(NavTransitionFadeDurationMs)) },
        popExitTransition = { fadeOut(tween(NavTransitionFadeDurationMs)) }
    ) {
        // Only ever reached as the graph's own startDestination on a not-yet-accepted cold start
        // (see the startDestination param above), never pushed onto an existing back stack.
        composable(PulseRoutes.ONBOARDING_CAROUSEL) {
            OnboardingCarouselScreen(
                onContinue = { navController.navigate(PulseRoutes.LEGAL_ACCEPTANCE) },
                isObscured = isSplashActive
            )
        }
        // Accepting clears the entire onboarding back stack (popUpTo the graph root, inclusive) so
        // the user can never navigate back into it, and so a later cold start reads
        // MARKET_OVERVIEW as the start destination instead.
        composable(PulseRoutes.LEGAL_ACCEPTANCE) {
            LegalAcceptanceRoute(
                onAccepted = {
                    navController.navigate(PulseRoutes.MARKET_OVERVIEW) {
                        popUpTo(navController.graph.id) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
                onNavigateToTerms = { navController.navigate(PulseRoutes.TERMS_CONDITIONS) },
                onNavigateToPrivacyPolicy = { navController.navigate(PulseRoutes.PRIVACY_POLICY) }
            )
        }
        // Reached both from the acceptance screen's own links (above) and from Settings -> Legal
        // (below) -- same two destinations either way, no acceptance action from this path.
        composable(PulseRoutes.TERMS_CONDITIONS) {
            TermsConditionsScreen(onNavigateUp = { navController.popBackStack() })
        }
        composable(PulseRoutes.PRIVACY_POLICY) {
            PrivacyPolicyScreen(onNavigateUp = { navController.popBackStack() })
        }
        composable(PulseRoutes.MARKET_SUMMARY) {
            MarketSummaryRoute(
                scaffoldPadding = scaffoldPadding,
                onReportTypeLoaded = onSummaryReportTypeLoaded,
                // 💡 Back to the same tab-preserving popUpTo/saveState/restoreState dance
                // FloatingBottomNav's onItemClick uses -- a plain push here (tried first) reached
                // Indicators through a different code path than the bottom nav ever uses for that
                // same route, and mixing "plain push" and "restoreState tab switch" navigation to
                // one destination left Navigation-Compose's saved-state bookkeeping confused:
                // tapping the Summary tab afterward silently did nothing, staying on Indicators.
                // Going back to this identical mechanism makes the Drivers jump indistinguishable
                // from an ordinary tab switch as far as the nav library is concerned. "Back should
                // return to Summary, not Overview" is handled separately now, by the BackHandler
                // inside the MARKET_INDICATORS destination below, rather than by the navigation
                // *mechanism* itself.
                onNavigateToIndicators = {
                    onDriversNavigatedToIndicators()
                    navController.navigate(PulseRoutes.MARKET_INDICATORS) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                // Same tab-preserving navigate() as onNavigateToIndicators above -- stash Posture
                // as the Insights tab to land on, report the arrival up (for the BackHandler
                // below), then push Insights exactly the way FloatingBottomNav's own tab switch
                // does.
                onNavigateToPosture = {
                    initialInsightsTab = InsightsTab.POSTURE
                    onMarketSentimentNavigatedToInsights()
                    navController.navigate(PulseRoutes.MARKET_INSIGHTS) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
        composable(PulseRoutes.MARKET_OVERVIEW) {
            DashboardRoute(
                scaffoldPadding = scaffoldPadding,
                // News is a plain push destination
                // (not a persisted bottom-nav tab), so this is a simple navigate() — no
                // popUpTo/saveState/restoreState tab-preserving dance needed.
                onNavigateToNews = {
                    navController.navigate(PulseRoutes.MARKET_NEWS)
                },
                // Stash the target article, then push News.
                onNavigateToNewsArticle = { url ->
                    highlightedNewsArticleUrl = url
                    navController.navigate(PulseRoutes.MARKET_NEWS)
                },
                // Dashboard symbols aren't all plain tickers -- `^VIX`, `GC=F`, `SI=F`, `CL=F`,
                // `HG=F`, `ES=F`, `NQ=F`, `YM=F` all reach this callback too, and `navigate(String)`
                // parses the route as a Uri internally. `^` isn't a legal URI character at all, so
                // an unencoded symbol here could crash or silently fail to match the destination --
                // same class of problem `webview/{encodedUrl}` below already works around; encoded
                // here, decoded back in `AssetDetailViewModel`'s `SavedStateHandle` read.
                onNavigateToAssetDetail = { symbol ->
                    val encodedSymbol = URLEncoder.encode(symbol, StandardCharsets.UTF_8.toString())
                    navController.navigate("${PulseRoutes.ASSET_DETAIL}/$encodedSymbol")
                }
            )
        }
        // Pushed from a dashboard tile tap. "symbol" is read
        // out of SavedStateHandle by AssetDetailViewModel itself, same as Stock Detail below.
        composable("${PulseRoutes.ASSET_DETAIL}/{symbol}") {
            AssetDetailRoute(
                scaffoldPadding = scaffoldPadding,
                onNavigateUp = { navController.popBackStack() }
            )
        }
        composable(PulseRoutes.MARKET_INDICATORS) {
            // 💡 Composed here, inside the destination's own content, not up in MainActivity --
            // NavHost registers its own internal back handling as part of composing itself, so a
            // BackHandler composed (and thus added to the back-press dispatcher) *before* NavHost
            // always loses to it; one added *after*, from inside the active destination's own
            // content, is what actually takes priority for that screen. Only enabled when Drivers
            // was the way here (see PulseNavGraph's onNavigateToIndicators above) -- an ordinary
            // tab-click arrival at Indicators keeps default back behavior (back to Overview).
            BackHandler(enabled = reachedIndicatorsFromDrivers) {
                onIndicatorsBackHandled()
                navController.navigate(PulseRoutes.MARKET_SUMMARY) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
            IndicatorsRoute(
                scaffoldPadding = scaffoldPadding,
                onNavigateToHorizons = { navController.navigate(PulseRoutes.INDICATOR_HORIZONS) },
                onNavigateToMetricDetail = { metricId ->
                    navController.navigate("${PulseRoutes.METRIC_DETAIL}/$metricId")
                }
            )
        }
        // Pushed from the Indicators tab's "Horizons" entry card -- see PulseRoutes.INDICATOR_HORIZONS.
        composable(PulseRoutes.INDICATOR_HORIZONS) {
            IndicatorHorizonsRoute(onNavigateUp = { navController.popBackStack() })
        }
        // Pushed from an indicator card tap. "metricId" is read
        // out of SavedStateHandle by MetricDetailViewModel itself, same as Asset Detail above.
        composable("${PulseRoutes.METRIC_DETAIL}/{metricId}") {
            MetricDetailRoute(
                scaffoldPadding = scaffoldPadding,
                onNavigateUp = { navController.popBackStack() },
                onNavigateToArticle = { key -> navController.navigate("${PulseRoutes.TUTORIALS_ARTICLE}/$key") }
            )
        }
        composable(PulseRoutes.MARKET_INSIGHTS) {
            // 💡 Same reasoning as the Indicators BackHandler above -- composed inside this
            // destination's own content so it takes priority over NavHost's own back handling.
            // Only enabled when Market Sentiment was the way here (see onNavigateToPosture above)
            // -- an ordinary tab-click arrival at Insights keeps default back behavior.
            BackHandler(enabled = reachedInsightsFromMarketSentiment) {
                onInsightsBackHandled()
                navController.navigate(PulseRoutes.MARKET_SUMMARY) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
            InsightsRoute(
                scaffoldPadding = scaffoldPadding,
                initialTab = initialInsightsTab,
                onInitialTabConsumed = { initialInsightsTab = null },
                onNavigateToGlossaryDetail = { metricIds, chartMetricId, title, description, status ->
                    // 💡 Uri.encode(), not URLEncoder.encode() -- see GlossaryDetailViewModel's
                    // doc comment for why the form-encoding pairing (spaces -> "+") crashed
                    // against Navigation's own automatic percent-decode of path segments.
                    // Uri.encode()'s escaping is exactly what that automatic decode reverses.
                    // `metricIds`/`chartMetricId` themselves are NOT encoded -- every
                    // core/glossary/ id and every Posture/Positioning chart metric id is plain
                    // lowercase/dot/underscore, safe as a raw path segment.
                    val encodedTitle = android.net.Uri.encode(title)
                    val encodedDescription = android.net.Uri.encode(description ?: "")
                    val encodedStatus = android.net.Uri.encode(status ?: "")
                    navController.navigate(
                        "${PulseRoutes.GLOSSARY_DETAIL}/$encodedTitle/${metricIds.joinToString(",")}/$chartMetricId/$encodedDescription/$encodedStatus"
                    )
                }
            )
        }
        // Pushed from a whole-card tap on Positioning/Posture -- see PulseRoutes.GLOSSARY_DETAIL.
        composable("${PulseRoutes.GLOSSARY_DETAIL}/{title}/{metricIds}/{chartMetricId}/{description}/{status}") {
            GlossaryDetailRoute(
                scaffoldPadding = scaffoldPadding,
                onNavigateUp = { navController.popBackStack() },
                onNavigateToArticle = { key -> navController.navigate("${PulseRoutes.TUTORIALS_ARTICLE}/$key") }
            )
        }
        composable(PulseRoutes.MARKET_ANALYSIS) {
            StockAnalysisRoute(
                scaffoldPadding = scaffoldPadding,
                onNavigateToDetail = { symbol ->
                    navController.navigate("${PulseRoutes.STOCK_ANALYSIS_DETAIL}/$symbol")
                }
            )
        }
        // Pushed from a StockPreviewCard tap. "symbol" is read out of SavedStateHandle by
        // StockDetailViewModel itself (see StockDetailViewModel.ARG_SYMBOL), not passed as a
        // composable parameter here -- hiltViewModel() auto-populates it from this route.
        composable("${PulseRoutes.STOCK_ANALYSIS_DETAIL}/{symbol}") {
            StockDetailRoute(
                scaffoldPadding = scaffoldPadding,
                onNavigateUp = { navController.popBackStack() },
                // 💡 Same in-app webview route News' article cards push to -- Direct News' cards
                // open the same way rather than each screen owning its own way of leaving the app.
                onNavigateToWebView = { url ->
                    val encodedUrl = URLEncoder.encode(url, StandardCharsets.UTF_8.toString())
                    navController.navigate("webview/$encodedUrl")
                },
                onNavigateToDeepDive = { symbol ->
                    navController.navigate("${PulseRoutes.DEEP_DIVE_DETAIL}/$symbol")
                },
                onNavigateToResolvedCalls = { symbol ->
                    navController.navigate("${PulseRoutes.RESOLVED_CALLS_LIST}/$symbol")
                },
                onNavigateToTechnicalTimeline = { symbol ->
                    navController.navigate("${PulseRoutes.TECHNICAL_TIMELINE_LIST}/$symbol")
                }
            )
        }
        // Pushed from a DeepDiveBanner tap on Stock Detail. "symbol" is read out of
        // SavedStateHandle by DeepDiveViewModel itself (see DeepDiveViewModel.ARG_SYMBOL), same
        // shape as STOCK_ANALYSIS_DETAIL above.
        composable("${PulseRoutes.DEEP_DIVE_DETAIL}/{symbol}") {
            DeepDiveRoute(
                scaffoldPadding = scaffoldPadding,
                onNavigateUp = { navController.popBackStack() }
            )
        }
        // Pushed from the "View More" row on Stock Detail's Resolved Calls / Technical Timeline
        // cards. "symbol" is read out of SavedStateHandle by each screen's own ViewModel, same
        // shape as DEEP_DIVE_DETAIL above.
        composable("${PulseRoutes.RESOLVED_CALLS_LIST}/{symbol}") {
            ResolvedCallsListRoute(
                scaffoldPadding = scaffoldPadding,
                onNavigateUp = { navController.popBackStack() }
            )
        }
        composable("${PulseRoutes.TECHNICAL_TIMELINE_LIST}/{symbol}") {
            TechnicalTimelineListRoute(
                scaffoldPadding = scaffoldPadding,
                onNavigateUp = { navController.popBackStack() }
            )
        }
        // Pushed only from the Dashboard's "Latest News" chevron or a specific preview card -- not
        // a bottom-bar tab.
        composable(PulseRoutes.MARKET_NEWS) {
            NewsRoute(
                scaffoldPadding = scaffoldPadding,
                onNavigateUp = { navController.popBackStack() },
                onNavigateToWebView = { url ->
                    val encodedUrl = URLEncoder.encode(url, StandardCharsets.UTF_8.toString())
                    navController.navigate("webview/$encodedUrl")
                },
                highlightedArticleUrl = highlightedNewsArticleUrl,
                onHighlightConsumed = { highlightedNewsArticleUrl = null }
            )
        }

        composable("webview/{encodedUrl}") { backStackEntry ->
            val encodedUrl = backStackEntry.arguments?.getString("encodedUrl") ?: ""
            val decodedUrl = URLDecoder.decode(encodedUrl, StandardCharsets.UTF_8.toString())

            PulseWebViewScreen(
                url = decodedUrl,
                bottomNavPadding = scaffoldPadding,
                onNavigateUp = { navController.popBackStack() }
            )
        }

        // Reached from the gear icon on the global top bar.
        composable(PulseRoutes.SETTINGS) {
            SettingsRoute(
                onNavigateUp = { navController.popBackStack() },
                onNavigateToThemePicker = { navController.navigate(PulseRoutes.SETTINGS_THEME_PICKER) },
                onNavigateToNotifications = { navController.navigate(PulseRoutes.SETTINGS_NOTIFICATIONS) },
                onNavigateToDataSync = { navController.navigate(PulseRoutes.SETTINGS_DATA_SYNC) },
                onNavigateToAbout = { navController.navigate(PulseRoutes.SETTINGS_ABOUT) },
                onNavigateToTerms = { navController.navigate(PulseRoutes.TERMS_CONDITIONS) },
                onNavigateToPrivacyPolicy = { navController.navigate(PulseRoutes.PRIVACY_POLICY) },
                onNavigateToTutorials = { navController.navigate(PulseRoutes.TUTORIALS_HUB) },
                onNavigateToComingUp = { navController.navigate(PulseRoutes.SETTINGS_COMING_UP) }
            )
        }
        composable(PulseRoutes.SETTINGS_NOTIFICATIONS) {
            NotificationsRoute(onNavigateUp = { navController.popBackStack() })
        }
        composable(PulseRoutes.SETTINGS_DATA_SYNC) {
            DataSyncScreen(onNavigateUp = { navController.popBackStack() })
        }
        composable(PulseRoutes.SETTINGS_ABOUT) {
            AboutRoute(
                onNavigateUp = { navController.popBackStack() },
                onNavigateToPrivacyPolicy = { navController.navigate(PulseRoutes.PRIVACY_POLICY) },
                onNavigateToTerms = { navController.navigate(PulseRoutes.TERMS_CONDITIONS) }
            )
        }
        composable(PulseRoutes.SETTINGS_COMING_UP) {
            ComingUpScreen(onNavigateUp = { navController.popBackStack() })
        }
        composable(PulseRoutes.SETTINGS_THEME_PICKER) {
            ThemePickerRoute(onNavigateUp = { navController.popBackStack() })
        }
        composable(PulseRoutes.TUTORIALS_HUB) {
            TutorialsHubScreen(
                onNavigateUp = { navController.popBackStack() },
                onNavigateToGaugeAnatomy = { navController.navigate(PulseRoutes.TUTORIALS_GAUGE_ANATOMY) },
                onNavigateToConcept = { navController.navigate("${PulseRoutes.TUTORIALS_CONCEPT}/${it.routeKey}") },
                onNavigateToMechanism = { navController.navigate("${PulseRoutes.TUTORIALS_DECK}/${it.routeKey}") },
                onNavigateToDataLimitations = { navController.navigate(PulseRoutes.TUTORIALS_DATA_LIMITATIONS) },
                onNavigateToArticle = { key -> navController.navigate("${PulseRoutes.TUTORIALS_ARTICLE}/$key") }
            )
        }
        composable("${PulseRoutes.TUTORIALS_CONCEPT}/{article}") { backStackEntry ->
            val article = ConceptArticle.fromRouteKey(backStackEntry.arguments?.getString("article"))
            if (article != null) {
                TutorialsConceptScreen(
                    article = article,
                    onNavigateUp = { navController.popBackStack() },
                    onNavigateToRoute = { routeName ->
                        PulseRoutes.tabRouteByConstantName(routeName)?.let { navController.navigateToTab(it) }
                    }
                )
            }
        }
        // Optional `group` = comma-separated mechanism keys when opened from a screen's "Show More"
        // that spans several mechanisms (a tab row then switches between them).
        composable(
            route = "${PulseRoutes.TUTORIALS_DECK}/{mechanism}?group={group}",
            arguments = listOf(navArgument("group") { type = NavType.StringType; nullable = true; defaultValue = null })
        ) { backStackEntry ->
            val mechanism = Mechanism.fromRouteKey(backStackEntry.arguments?.getString("mechanism"))
            val group = backStackEntry.arguments?.getString("group")
                ?.split(",")
                ?.mapNotNull { Mechanism.fromRouteKey(it) }
                .orEmpty()
            if (mechanism != null) {
                MechanismDeckScreen(
                    mechanism = mechanism,
                    group = group,
                    onNavigateUp = { navController.popBackStack() },
                    // Always this one mechanism's own indicators -- never the whole group's, even
                    // when [group] itself lets the deck's content tabs switch between mechanisms.
                    onSeeIndicators = { current ->
                        navController.navigate("${PulseRoutes.TUTORIALS_GLOSSARY}/${current.routeKey}")
                    },
                    onNavigateToRoute = { routeName ->
                        PulseRoutes.tabRouteByConstantName(routeName)?.let { navController.navigateToTab(it) }
                    }
                )
            }
        }
        composable(route = "${PulseRoutes.TUTORIALS_GLOSSARY}/{mechanism}") { backStackEntry ->
            val mechanism = Mechanism.fromRouteKey(backStackEntry.arguments?.getString("mechanism"))
            if (mechanism == Mechanism.STOCK_ANALYSIS) {
                StockSetupsGlossaryScreen(onNavigateUp = { navController.popBackStack() })
            } else if (mechanism != null) {
                TutorialsGaugesRoute(
                    onNavigateUp = { navController.popBackStack() },
                    onNavigateToArticle = { key -> navController.navigate("${PulseRoutes.TUTORIALS_ARTICLE}/$key") }
                )
            }
        }
        composable(PulseRoutes.TUTORIALS_GAUGE_ANATOMY) {
            TutorialsGaugeAnatomyScreen(onNavigateUp = { navController.popBackStack() })
        }
        composable(PulseRoutes.TUTORIALS_DATA_LIMITATIONS) {
            TutorialsDataLimitationsScreen(onNavigateUp = { navController.popBackStack() })
        }
        composable("${PulseRoutes.TUTORIALS_ARTICLE}/{key}") { backStackEntry ->
            val key = backStackEntry.arguments?.getString("key")
            if (key != null) {
                TutorialsArticleScreen(articleKey = key, onNavigateUp = { navController.popBackStack() })
            }
        }
    }
}
