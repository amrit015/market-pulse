# Android Content Surface Inventory

> Ground truth as of 2026-09-17, on branch `feat-compliance`. Every string below is pasted
> verbatim from source (string resource or JSON asset), not paraphrased. Item 0 is existing
> shipped copy; items 1-6 below it are per-screen structural/threshold data. Nothing here is new
> copy or new design — this is a snapshot to build on.

---

## 0. Existing content already shipped (verbatim)

### 0.1 Tutorials hub — 5 articles

Entry point: **Settings → More → Tutorials** (`SettingsScreen.kt` "More" card, last row) →
`TutorialsHubScreen.kt`, one `PulseCard` with 5 rows, each pushing its own destination.

Row labels (`TutorialsHubScreen.kt` / `strings.xml`):
- `tutorials_item_how_it_works` = "How Market Pulse Works"
- `tutorials_item_gauges` = "Understanding the Gauges"
- `tutorials_item_market_concepts` = "Market Concepts"
- `tutorials_item_ai_content` = "About AI-Generated Content"
- `tutorials_item_data_limitations` = "Data Limitations"

#### "How Market Pulse Works" (`DocumentSectionsScreen`, 3 blank-heading paragraphs)

> `tutorials_how_it_works_paragraph_1`: "Market Pulse pulls market data, macro indicators, and
> company fundamentals from a mix of public and licensed data sources, then runs it through a set
> of deterministic calculations before any AI touches it — trend direction, valuation gaps, and
> technical setups are computed in code, not guessed by a model."
>
> `tutorials_how_it_works_paragraph_2`: "Some of what you see is pure computed data (prices,
> ratios, historical values). Some is AI-generated narrative built on top of that data (daily
> digests, plain-language summaries, interpretive text). The App doesn't hide which is which
> inside this section — see \"About AI-Generated Content\" for how to tell them apart on any given
> screen."
>
> `tutorials_how_it_works_paragraph_3`: "Data refreshes on a schedule, not continuously — most
> indicators update once or a few times a day; a small set of live prices update every minute
> during market hours."

#### "Understanding the Gauges" (`TutorialsGaugesScreen`, intro + 6 categories, live from `MetricGlossaryProvider`)

> `tutorials_gauges_intro`: "Every gauge and indicator across Market Pulse follows the same idea: a
> raw value gets bucketed into a plain-language read (e.g. \"Extreme Fear,\" \"Overbought,\"
> \"Neutral\") using thresholds defined elsewhere, not judged case by case. The definitions below
> come from the same glossary data the rest of the app uses — tap any gauge in the app itself for
> the fuller breakdown, including its current band and any gotchas specific to that metric."

Category grouping is static curation in `TutorialsGaugesCatalog.kt`; each gauge's title is a
static string, but its **body text (`whatItIs`) is NOT a string resource** — it's read live at
runtime from `metric_glossary.json`'s `what_it_is` field via `MetricGlossaryProvider.getAll()`
(same bundle `GlossaryDetailScreen`/`MetricDetailScreen` use). The 6 categories, in order, with
their member metric ids (title strings only shown — full `what_it_is` text is in §2 below, sourced
from the JSON, not duplicated here):

1. **Tactical Momentum** (`tutorials_gauges_category_tactical_momentum`) — Fear & Greed Index,
   Put/Call Ratio, SPY RSI (14-Day), SMA Extension (200-Day)
2. **Systemic Risk** (`_systemic_risk`) — VIX, Yield Curve (10Y–02Y), Credit Spreads, MOVE Index,
   Copper/Gold Ratio, Consumer Rotation (XLY/XLP), Market Breadth (RSP/SPY), U.S. Dollar Index
   (DXY), Oil Price
3. **Valuation** (`_valuation`) — P/E Ratio, P/B Ratio, Equity Risk Premium, Dividend Yield
4. **Macro Vitals** (`_macro_vitals`) — CPI (YoY), Core PCE (YoY), Core PCE (MoM), Unemployment
   Rate, Nonfarm Payrolls, Real GDP, Retail Sales, Fed Funds Rate, 10-Year Treasury Yield
5. **Positioning** (`_positioning`) — AAII Bull-Bear Spread, CFTC COT Net % of Open Interest, CFTC
   COT Percentile, Short Interest (Days to Cover / Shares / MoM Change)
6. **Posture** (`_posture`) — NAAIM Exposure Index, Dark Pool Index (DIX), Net Liquidity

A metric id present in the JSON but absent from this catalog is silently omitted (currently: none
are missing — all 35 JSON entries are covered by the 6 categories above, 32 shown + 3 short
interest sub-metrics under Positioning; note `positioning.short_interest_shares` has empty
`"bands": []` in the JSON, so it browses with no bands list).

#### "Market Concepts" (`DocumentSectionsScreen`, 4 headed sections)

> **`tutorials_market_concepts_sentiment_title`**: "Sentiment & Positioning"
> `..._sentiment_body`: "Markets move on more than fundamentals — how investors feel about risk
> shifts prices too, sometimes faster than the underlying data changes. A handful of indicators try
> to measure that feeling directly, mostly by looking at what investors are actually doing with
> their money rather than what they say.\n\nFear & Greed and the Put/Call ratio (CNN) gauge
> short-term market mood — extreme fear has historically coincided with periods where selling
> pressure was overdone, and extreme greed with periods of elevated risk-taking. Neither is a
> timing signal on its own; both can stay at an extreme for a long time before anything
> changes.\n\nThe NAAIM Exposure Index tracks how invested active fund managers actually are, week
> to week — a direct measure of professional positioning rather than a survey response.\n\nThe
> AAII Sentiment Survey measures individual investor sentiment (bullish/neutral/bearish) — a widely
> watched but famously unreliable contrarian indicator: extreme readings in either direction are
> sometimes read as a sign that a consensus view has gotten crowded, not as confirmation the
> consensus is right.\n\nThe CFTC Commitment of Traders (COT) report and FINRA short interest data
> show what large futures traders and short sellers are positioned for — institutional-level
> positioning that moves on a different timescale than retail sentiment.\n\nIn Market Pulse: this
> whole family appears as the Posture and Positioning gauges. Posture combines NAAIM, SqueezeMetrics'
> Dark Index (DIX), and net liquidity signals; Positioning combines AAII, COT futures data, and
> FINRA short interest. Each is one input among several — none is used alone anywhere in the app."
>
> **`..._macro_title`**: "Macro & Interest Rates"
> `..._macro_body`: "Interest rates set the \"cost of money\" for the entire economy, so changes to
> them ripple into nearly everything else. When the Federal Reserve raises rates, borrowing gets
> more expensive for companies and consumers alike — which can slow spending and earnings growth,
> and tends to make already-established cash flows (like bonds, or stocks priced heavily on future
> growth) relatively more attractive than they were before. Rate cuts generally work in the
> opposite direction. Neither effect is instant or guaranteed — markets often move on the
> expectation of a rate change well before the change itself happens.\n\nThe 10-year and 2-year
> Treasury yields, and the spread between them, are widely watched because an inverted yield curve
> (short-term yields above long-term) has historically preceded economic slowdowns — though the
> timing and reliability of that relationship varies a lot across cycles.\n\nInflation data (CPI,
> core PCE) matters because it's a primary input into what the Fed does next — hotter-than-expected
> inflation readings tend to raise the odds of tighter policy, and vice versa.\n\nIn Market Pulse:
> this appears in the Macro Vitals gauge, built from FRED data — the 10-year and 2-year Treasury
> yields, core inflation (CPI and core PCE), unemployment, and the Fed funds rate, updated from the
> same source the Fed itself publishes."
>
> **`..._technical_title`**: "Technical Concepts"
> `..._technical_body`: "Technical analysis looks at price and volume patterns themselves, on the
> theory that price action reflects everything the market currently knows or believes, even before
> that shows up elsewhere.\n\nMoving averages smooth out day-to-day noise to show the underlying
> trend — the 50-day and 200-day are the most widely watched. When a stock's price is above its
> 200-day moving average, that's commonly read as a sign the longer-term trend is up; below it, the
> opposite. A \"golden cross\" (50-day crossing above the 200-day) and \"death cross\" (the reverse)
> are watched as longer-term trend-change signals, though both can lag the actual turn by
> weeks.\n\nSupport and resistance describe price levels where a stock has repeatedly stopped
> falling or stopped rising in the past — the idea being that enough market participants remember
> those levels that they influence behavior again next time price approaches them. Neither is a
> hard floor or ceiling; both get broken regularly.\n\nIn Market Pulse: this is the SMA card and the
> momentum/trend classification on each stock's detail screen — computed directly from price
> history, not from an AI's read of a chart. The forward calls feature tracks how a classified setup
> (e.g. a momentum or mean-reversion read) actually played out over the following trading sessions —
> a historical record of the app's own calls, not a prediction of what comes next."
>
> **`..._valuation_title`**: "Valuation Basics"
> `..._valuation_body`: "Valuation asks a different question than sentiment or technicals: is the
> price being paid reasonable relative to what the company actually earns? The P/E ratio (price
> divided by earnings per share) is the most common starting point — a \"high\" P/E means the
> market is paying more per dollar of current earnings, often because it expects faster future
> growth; a \"low\" P/E can mean the opposite, or can mean the market has real concerns the price
> hasn't priced out.\n\nComparing a stock's P/E to its own sector, rather than to the market as a
> whole, controls for the fact that some industries structurally trade at higher multiples than
> others (growth software vs. utilities, for example) — a stock that looks \"cheap\" against the
> whole market can be expensive against its actual peers, and vice versa.\n\nA low valuation is
> not, by itself, a signal that a stock is underpriced — it's frequently a sign the market has
> already priced in a real risk. Valuation is one lens among several, not a standalone
> verdict.\n\nIn Market Pulse: this is the sector-relative P/E metric on each stock's fundamentals —
> the stock's own P/E measured against its sector's median, computed from the same underlying data
> as the headline P/E figure, not a separate estimate."

#### "About AI-Generated Content" (`DocumentSectionsScreen`, intro + 3 headed sections)

> `tutorials_ai_content_intro`: "Parts of Market Pulse — daily digests, plain-language summaries,
> deep-dive write-ups, and the short explainer text near some charts — are written by an AI model,
> not a human analyst."
>
> **`_limitations_title`**: "Limitations Worth Knowing"
> `_limitations_body`: "AI models are good at summarizing patterns in data quickly, but they have
> real limitations worth knowing about:\n• They can misread or misweight a data point, especially
> when signals conflict.\n• They can state something with more confidence than the underlying data
> actually supports.\n• They don't know anything about your personal financial situation, goals, or
> risk tolerance — their output isn't written for you specifically.\n• Their training has a cutoff;
> very recent events may not be reflected in how they frame a story, even when the numbers on
> screen are current."
>
> **`_mitigations_title`**: "What Market Pulse Does to Reduce This"
> `_mitigations_body`: "• Every number you see (prices, ratios, chart values) is computed in code
> first — AI never invents a figure, it only writes about figures that were already calculated.\n•
> AI-generated text is automatically screened for advice-sounding language (\"you should buy,\"
> \"good entry point,\" etc.) before it's shown — if flagged content is caught, it's removed rather
> than shown anyway."
>
> **`_what_you_should_do_title`**: "What You Should Do"
> `_what_you_should_do_body`: "• Treat AI-written summaries as a starting point for your own
> research, not a conclusion.\n• Cross-check anything that would influence a real decision against
> the underlying data or an independent source.\n• Remember: none of this is personalized to you,
> and none of it is financial advice."

#### "Data Limitations" (`DocumentSectionsScreen`, 1 headed section)

> `tutorials_data_limitations_title`: "A Few Things Worth Knowing About the Data Itself"
> `tutorials_data_limitations_body`: "• Market holidays and half-days: some indicators may show a
> stale \"last close\" value rather than a fresh one.\n• After-hours vs. regular-hours pricing: a
> price shown outside market hours may reflect after-hours trading, which is typically
> lower-volume and more volatile than regular-hours pricing.\n• Halted or delisted symbols: data
> for a halted or delisted symbol may be missing or frozen at its last known value.\n• Delayed
> data: most market data is not real-time; treat displayed prices as approximate, not tradeable
> quotes."

### 0.2 Onboarding carousel — 3 slides (`OnboardingCarouselScreen.kt`)

No Skip; last slide's button switches from "Next" to "Continue" (`onboarding_next` /
`onboarding_continue`). **Visual/placeholder: none** — each slide is plain centered title + body
text directly on the screen background (no illustration, icon, or image asset), a row of page-
indicator dots below the pager, and the Next/Continue button. Purely typographic.

> **Slide 1** (`onboarding_slide1_title`): "Institutional-grade market intelligence, without the
> noise."
> (`onboarding_slide1_body`): "Market Pulse tracks US equities, macro indicators, and market
> sentiment in one place — built for investors who want the real data, not just headlines."
>
> **Slide 2** (`onboarding_slide2_title`): "Two kinds of content, always labeled."
> (`onboarding_slide2_body`): "Numbers, charts, and prices are computed directly from market data —
> no AI involved.\n\nSummaries and narrative reads are written by AI based on that data. Look for
> the ⓘ icon on any AI-written card to see exactly how it was generated."
>
> **Slide 3** (`onboarding_slide3_title`): "One important thing first."
> (`onboarding_slide3_body`): "Market Pulse is a research tool, not financial advice. The next
> screen covers what that means — it'll take a minute to read, and it matters."

**⚠ Stale reference caught by this inventory pass**: Slide 2's body text says "Look for the ⓘ icon
on any AI-written card to see exactly how it was generated" — that's the *original* Phase 3 badge
design (a clickable badge opening a what-this-means/how-generated sheet). Since the most recent
redesign, the AI tag is a plain non-interactive grey label (§4 below) and the ⓘ icon is gone; the
only icon near AI content now is the unrelated "?" screen-guide affordance in the top bar. This
slide's copy no longer describes what's actually on screen.

### 0.3 Legal Acceptance screen (`LegalAcceptanceScreen.kt`) — 4 disclosure paragraphs

`legal_acceptance_paragraph_1` through `_4` (already shipped Phase 1 content, unchanged this
pass — not re-pasted here since it predates and is outside this round's scope; see git history for
Phase 1's commit `591c8a6` if needed).

### 0.4 Screen Guide ("?" affordance) — the new per-screen explainer (§2026-09-17 addition)

This is the "per-screen what this is / how to interpret" system the user asked about — it exists
today, wired for the 5 main tabs only, via `ScreenGuideAction`/`ScreenGuideSheet` in
`AppTopBar.kt`, content resolved in `MainActivity.kt`'s `screenGuideContentFor(route)`. Two
sections per screen: **Overview** and **How to Interpret It** (`screen_guide_overview_heading` /
`screen_guide_how_to_interpret_heading`).

> **Market Overview** (`screen_guide_overview_overview`): "A snapshot of the whole market: today's
> AI-written digest, sentiment gauges, futures, and your tracked assets, all in one scrollable
> feed."
> (`screen_guide_overview_how_to_interpret`): "Numbers, charts, and prices are computed directly
> from data — no AI involved. The Daily Digest card at the top is the one AI-written section on
> this screen, labeled \"AI-generated · Not advice.\" Gauges like Fear & Greed and the Put/Call
> Ratio show where a reading currently sits on its own scale — the colored band tells you which
> zone it's in, not what to do about it."
>
> **Indicators** (`screen_guide_indicators_overview`): "Every tracked indicator grouped into four
> pillars — Tactical Momentum, Systemic Risk, Valuation, and Macro Vitals — plus a cross-pillar AI
> read at the top and a Horizons view of how it plays out over time."
> (`screen_guide_indicators_how_to_interpret`): "Each pillar's scorecard shows an AI-written
> one-line summary of what its indicators mean together — the AGREEMENT and STANCE pills above it
> are computed directly from data, not AI-judged. Individual metric cards show a raw value and a
> plain-language status (e.g. NEUTRAL, OVERSOLD) based on fixed thresholds, not a model's opinion."
>
> **Summary** (`screen_guide_summary_overview`): "A day-by-day narrative view of the market — swipe
> between days to see that day's AI-written read, scheduled economic events, and any notable risks,
> alongside the Weekly Playbook and Market Sentiment cards."
> (`screen_guide_summary_how_to_interpret`): "The Weekly Playbook and Market Sentiment cards are
> AI-written syntheses of this app's own data — look for the \"AI-generated · Not advice\" label in
> their header. Economic event cards show scheduled releases with their prior/estimate/actual
> values; the market-context text around them is descriptive, not a prediction of how the market
> will react."
>
> **Insights** (`screen_guide_insights_overview`): "Four tabs — Events (Weekly Playbook), Risks,
> Posture, and Positioning — each pairing a deterministic gauge with an AI-written synthesis of
> what it currently shows."
> (`screen_guide_insights_how_to_interpret`): "Every tab follows the same shape: computed gauges and
> data cards first, then one AI-written summary card below them. Posture and Positioning each
> combine several underlying signals (NAAIM/DIX/net liquidity for Posture; AAII/COT/short interest
> for Positioning) into one gauge — tap a card for the fuller breakdown of what feeds into it."
>
> **Market Analysis** (`screen_guide_analysis_overview`): "Tracked stocks, indices, and ETFs with a
> live price, a short AI-written read of their technical setup, and status pills (e.g. Overbought,
> Undervalued) computed from this app's own data."
> (`screen_guide_analysis_how_to_interpret`): "Status pills (RANGE BOUND, Undervalued, etc.) are
> computed directly from price/fundamental data using fixed rules — the sentence above them is the
> one AI-written element on each card. Tap into a symbol for the full detail screen, including a
> longer Deep Dive analysis where available."

Not covered yet (no `screenGuideContentFor` case, so `guideContent == null` and the "?" icon
doesn't render): Stock Detail, Deep Dive, News, Settings, Tutorials (hub + all 5 articles), Legal
Acceptance, Terms & Conditions, Privacy Policy, Asset Detail (Dashboard drill-in), Metric Detail
(Indicators drill-in), Glossary Detail (Insights drill-in), Indicator Horizons.

---

## 1-3. Per-screen data source, gauges/thresholds, and existing explainer text

Transport note before the per-screen breakdown: **most domains do NOT read Firestore directly on
device.** Per `docs/architecture/data-flow.md`'s two-transport-strategy split, only the Dashboard
uses a live Firestore listener; everything else goes through a Retrofit `@GET` call to a Cloud
Functions v2 HTTP endpoint (see each screen's "Source" line below for the exact path/collection).

### Market Overview (Dashboard tab)
- **Source**: Firestore listener, `firestore.collection("market_overview")`
  (`RemoteDashboardDataSourceImpl.kt`) — the one domain that IS a direct Firestore stream.
- **Gauges**: Fear & Greed Index, Put/Call Ratio — bands in §2 below (`fear_and_greed`,
  `put_call_ratio` in `metric_glossary.json`).
- **Existing explainer**: none dedicated beyond the new Screen Guide (§0.4) — no `*_explainer_text`
  string exists for this screen specifically; the Daily Digest card uses `AiGeneratedLabel` only
  (no what-this-means text on the card itself, per the redesign in §4).
- **AI tag**: `DailyDigestHeroCard.kt:108` — `AiGeneratedLabel()`, once per digest card.

### Indicators tab (4 pillars + AI synthesis + Horizons)
- **Source**: `IndicatorsApi` — `GET indicators/synthesis`, `GET indicators/tactical`,
  `GET risk/latest`, `GET indicators/valuation`, `GET indicators/vitals` (fetched concurrently,
  merged into one `MarketIndicators` domain object per `dateId`), plus `.../history` variants for
  each pillar backing `MetricDetailScreen`'s chart.
- **Gauges**: all 22 individual metrics across Tactical Momentum/Systemic Risk/Valuation/Macro
  Vitals — full bands in §2 (`fear_and_greed` through `yield_10y` in `metric_glossary.json`).
- **Pillar-level rollup** (`PillarScorecardCard` in `IndicatorsScreen.kt:892-940`): `agreement`
  (ALIGNED/MIXED/DIVERGENT) and `stance` (GREEN/YELLOW/RED `SignalColor`) are **not client
  thresholds** — both are backend-computed fields on `pillar_scorecard[]`, rendered as-is. There is
  no client-side band logic to document for these two pills; the doc comment at
  `IndicatorsScreen.kt:885-890` confirms this explicitly.
- **Existing explainer**: none screen-level beyond Screen Guide (§0.4); per-metric explainers exist
  via tap-through to `MetricDetailScreen` (backed by the same `metric_glossary.json`).
- **AI tag**: `IndicatorsScreen.kt:594` ("Today's Read" card) and `:912` (`PillarScorecardCard`,
  once per pillar — 4 instances).

### Summary tab (day-by-day narrative)
- **Source**: `MarketPulseApi` — `GET pulse/v3/latest` and `GET pulse/v3/{dateId}`
  (`RemoteSummaryDataSourceImpl.kt`).
- **Gauges**: Market Sentiment card reuses the Dashboard's Fear & Greed/Put-Call read; regime pill
  uses `directions` (RISK ON/RISK OFF/MIXED) from `market_glossary.json`.
- **Existing explainer**: `positioning_explainer_text`/`posture_explainer_text` do NOT live here
  (those are Insights tab strings, see below) — Summary's own explainer text is the Weekly Playbook
  description (`weekly_playbook_description`, quoted in §Insights below, since the same card
  appears here too) plus `market_glossary.json`'s `directions`/`regimes` via `MarketBottomSheet`.
- **AI tag**: `SummaryScreen.kt:1384` (Market Sentiment card).

### Insights tab — 4 sub-tabs (Events/Weekly Playbook, Risks, Posture, Positioning)
- **Source**:
  - Weekly Playbook: `WeeklyPlaybookApi` — `GET dashboard/playbook`
  - Risks: `MarketRiskApi` — `GET risk/tail-risks`
  - Posture: `MarketPostureApi` — `GET insights/posture` (+ `/history`)
  - Positioning: `MarketPositioningApi` — `GET insights/positioning` (+ `/history`)
- **Gauges**: Posture = NAAIM Exposure, Dark Pool Index (DIX), Net Liquidity; Positioning = AAII
  Bull-Bear Spread, CFTC COT (net %OI + percentile), Short Interest (days-to-cover/shares/MoM) —
  full bands in §2 (`posture.*`/`positioning.*` in `metric_glossary.json`). Risks tab has no fixed
  numeric bands — it's a curated list with `statuses`/`trends` glossary terms (`risk_glossary.json`,
  see §2).
- **Existing explainer strings** (verbatim, all AI-disclosure-suffixed per spec §5):
  > `positioning_explainer_text`: "Positioning shows where retail sentiment (AAII), large
  > speculators (CFTC COT futures positioning), and short sellers (FINRA short interest) currently
  > stand — three different lenses on who's leaning which way. A crowded reading in either direction
  > has historically preceded a reversal as that positioning unwinds. None of these are predictions
  > or recommendations; they describe current positioning, which can shift or reverse without
  > warning. This interpretation is generated by an AI model and may not reflect current market
  > conditions."
  >
  > `posture_explainer_text`: "Posture tracks how professional money managers, institutional order
  > flow, and system-wide liquidity are positioned beneath the surface of daily price moves — active
  > manager equity exposure (NAAIM), dark pool accumulation (DIX), and the Fed's net liquidity
  > backdrop. Like Positioning, this describes current conditions, not a forecast or recommendation.
  > This interpretation is generated by an AI model and may not reflect current market conditions."
  >
  > `risk_assessment_description`: "Risk Assessment is an AI-identified list of potential risks that
  > could derail the current market narrative, each scored by potential impact and explained against
  > today's macro regime. It refreshes several times a day and closes with a one-sentence read on
  > overall market fragility. This describes potential threats, not a forecast or recommendation.
  > This interpretation is generated by an AI model and may not reflect current market conditions."
  >
  > `weekly_playbook_description`: "The Weekly Playbook is an AI-curated shortlist of the economic
  > releases most likely to move markets this week, drawn from the upcoming economic calendar and
  > regenerated every Sunday. Each event's context explains how it could confirm or challenge the
  > current macro outlook; actual figures and their market impact are added mid-week as each release
  > happens. This is scheduling context, not a trading signal or recommendation. This interpretation
  > is generated by an AI model and may not reflect current market conditions."
  >
  > Per-metric descriptions also exist: `posture_naaim_description`, `posture_dix_description`,
  > `posture_net_liquidity_description`, `positioning_retail_sentiment_description`,
  > `positioning_institutional_description`, `positioning_short_interest_description` (all quoted
  > in full in the grep output this inventory was built from — omitted here for length, all
  > non-AI/factual metric descriptions, not synthesis explainers).
  >
  > Pillar-level descriptions (Indicators tab, not Insights, but same pattern):
  > `pillar_tactical_momentum_description`, `pillar_systemic_risk_description`,
  > `pillar_valuation_description`, `pillar_macro_vitals_description`.
- **AI tag**: `SynthesisHeroCard.kt:101`, one shared composable used by all 4 of
  `MarketPositioningView.kt`/`MarketPostureView.kt`/`MarketRisksView.kt`/`WeeklyPlaybookView.kt`.
- **Gap**: Insights is the one of the 5 main tabs whose Screen Guide "how to interpret" text (§0.4)
  is the most compressed relative to how much distinct content it holds (4 sub-tabs, 12 gauges, 2
  merged glossary cards) — worth revisiting if you extend Screen Guide content later, not a missing
  feature.

### Market Analysis tab (stock/index/ETF list)
- **Source**: `StocksApi` — `GET stocks/previews` (list), `GET stocks/{symbol}/detail`,
  `GET stocks/{symbol}/detail/deep`.
- **Gauges**: per-row status pills (RANGE BOUND, Undervalued, Overbought, etc.) — these are the
  `stock_setups` values in `market_glossary.json` (§2): BREAKDOWN/BREAKOUT/MEAN REVERSION/
  ACCUMULATION/DISTRIBUTION/RANGE BOUND.
- **Existing explainer**: `deep_dive_card_description` ("Structural read: profile, standing, risks,
  outlook, consensus, so on.") is Deep Dive's, not this list's.
- **AI tag**: none directly on this screen — the AI-written one-line technical read shown per card
  has no `AiGeneratedLabel` of its own at list-row scale (it inherits the Screen Guide's disclosure
  instead, per `screen_guide_analysis_how_to_interpret`).
- **⚠ Gap (item 6)**: confirmed via `StockAnalysisScreen.kt` — the only `onClick` on each row is
  `onCardClick` (navigates to Stock Detail). The status pill's definition (`stock_setups` glossary)
  is **not tappable from this list** — it's only reachable via `MarketBottomSheet`, which this
  screen doesn't open directly. A user sees "RANGE BOUND" on a card here with no in-place way to
  learn what it means without first opening the stock's detail screen and finding that glossary
  sheet elsewhere.

### Stock Detail (pushed from Market Analysis)
- **Source**: `StocksApi.getStockDetail(symbol)` → `GET stocks/{symbol}/detail`.
- **Gauges/values**: dozens of fundamentals/technicals (ATR, RSI, MACD, SMA20/50/200, P/E, P/B,
  EV/EBITDA, PEG, ROIC, ROE, margins, FCF yield, Beta, short interest, etc.) — every one of these
  has a definition in `stock_analysis_glossary.json` (§2), surfaced via
  `StockAnalysisGlossaryBottomSheet`, reachable per-section (Key Levels, Fundamentals, Macro,
  Returns, SMA, Setup Reasoning, Momentum & Trend, Headline Metrics).
- **Header badges**: `technicalSetup` (BREAKDOWN/BREAKOUT/ACCUMULATION/DISTRIBUTION/
  MEAN_REVERSION/RANGE_BOUND) and `regimeAtAnalysis` (risk_on/risk_off/neutral) are BOTH
  tap-to-explain (`DetailHeader.kt:68-78`), opening `MarketGlossaryBottomSheet` with
  `currentStockSetup`/`currentDirection` — confirmed NOT a gap, unlike the list screen above.
- **Existing explainer**: `TechnicalRead` section carries its own AI-written paragraph (no
  additional explainer string beyond the AI tag itself).
- **AI tag**: `TechnicalRead.kt:57` (technical read paragraph), `DigestCard.kt:61` (digest/summary
  section).

### Deep Dive (pushed from Stock Detail)
- **Source**: `StocksApi.getStockDeepDive(symbol)` → `GET stocks/{symbol}/detail/deep`.
- **Existing explainer**: `deep_dive_card_description` = "Structural read: profile, standing,
  risks, outlook, consensus, so on."
- **AI tag**: `DeepDiveRoute.kt:134` — once per page (not per-section), per the file's own comment
  explaining this is deliberate (the whole write-up is one AI pass, not independently-generated
  sections).
- **Gap**: no Screen Guide "?" wired (route not in `screenGuideContentFor`).

### Settings
- **Source**: none (local `SettingsUiState`, theme preference via DataStore) — not a data screen.
- **Rows**: Notifications, Data & Sync, About, Terms & Conditions, Privacy Policy, Tutorials (all
  real destinations). Now card-wrapped (§ prior work this session).
- **Gap**: no Screen Guide "?" wired — arguably doesn't need one (no data to interpret).

### Legal Acceptance / Terms & Conditions / Privacy Policy
- **Source**: none — static legal copy (`LegalAcceptanceScreen.kt`, `DocumentSectionsScreen.kt`).
- **Gap**: no Screen Guide "?" wired.

---

## 4. AI-generated tag — final placement/wording as shipped

- **Component**: `AiGeneratedLabel.kt` — plain `Text`, `labelSmall` typography at 10sp,
  `LocalPulseColors.current.onSurfaceMuted` (grey), **not clickable, no background, no border, no
  state**. Text: `ai_generated_badge_label` = **"AI-generated · Not advice"**.
- **Placement**: top-right of each card's own header row (`Row(horizontalArrangement = Arrangement.End)`
  or the card's existing header row's trailing slot) — confirmed consistent across all 8 wired
  sites:
  1. `IndicatorsScreen.kt:594` — "Today's Read" card
  2. `IndicatorsScreen.kt:912` — `PillarScorecardCard` (×4 pillar instances)
  3. `DeepDiveRoute.kt:134` — once per Deep Dive page
  4. `TechnicalRead.kt:57` — Stock Detail's technical read section
  5. `DigestCard.kt:61` — Stock Detail's digest section
  6. `DailyDigestHeroCard.kt:108` — Dashboard's Daily Digest hero card
  7. `SynthesisHeroCard.kt:101` — shared by Positioning/Posture/Risks/Weekly Playbook (Insights tab)
  8. `SummaryScreen.kt:1384` — Market Sentiment card
- **Confirmed removed**: the old clickable badge (`AiGeneratedBadge.kt`) and its per-card
  what-this-means/how-generated bottom sheet (`SynthesisExplainerSheet.kt`) — both deleted this
  session. Per-card `_what_this_means`/`_how_generated` strings removed from `strings.xml`.
  **The per-card info affordance the user asked to confirm removed IS removed** — replaced
  conceptually by the per-*screen* (not per-card) "?" Screen Guide described in §0.4, which is a
  different, coarser-grained concept (explains the screen, not the individual card).

---

## 5. Tutorials entry point + GlossaryDetailScreen sourcing

- **Entry point**: `Settings → More → Tutorials` row only. **Not** in any top bar, not reachable
  from the 5 main tabs directly — a user has to go through Settings.
- **`GlossaryDetailScreen` sourcing**: pushed route
  `glossaryDetail/{title}/{metricIds}/{chartMetricId}/{description}/{status}`
  (`GlossaryDetailViewModel.kt`). `metricIds` (comma-joined) are looked up synchronously in
  `MetricGlossaryProvider` at `init` time (no async fetch for content — content is static/bundled);
  only the history chart (`InsightsHistoryRepository.getHistoryStream`) is a live async stream.
  Bands are merged across ALL metric ids passed for a card (`.flatMap { it.bands }.distinctBy {
  it.label }`) since e.g. a short-interest instrument's overall status draws on two different
  fields' thresholds at once. Only 9 metric ids have a static label mapping for this merged-card
  view (`labelResFor` in `GlossaryDetailViewModel.kt:176-186`) — the 9 Posture/Positioning ids;
  Indicators' own `MetricDetailScreen` (not shown in full here, same underlying provider) handles
  the other 26.

---

## 6. Screens rendering a value with NO accompanying explainer today

1. **Market Analysis list row status pills** (`StockAnalysisScreen.kt`) — see the ⚠ Gap noted
   under that screen above. The single biggest concrete gap found: a glossary (`stock_setups`)
   exists and is used elsewhere, but this screen has no tap path to it.
2. **Deep Dive, Settings, Tutorials (hub + 5 articles), Legal Acceptance, Terms & Conditions,
   Privacy Policy, Asset Detail, Metric Detail, Glossary Detail, Indicator Horizons, News** — none
   of these have a Screen Guide "?" wired (only the 5 main tabs do, per §0.4). Most of these
   (Deep Dive, Stock Detail's own sections, Asset/Metric/Glossary Detail) already have their OWN
   per-value glossary sheets though, so the gap there is specifically the screen-level "what is
   this page" framing, not per-value definitions.
3. **Onboarding slide 2** — references a UI element (the ⓘ icon) that no longer exists after this
   session's redesign; flagged in §0.2 above as stale copy, not a missing-explainer gap per se.
