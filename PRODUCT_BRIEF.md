# Market Pulse — Product Brief

*Context for design work. Pair with `STYLE_SPEC.md` (current tokens) when briefing a Design session — together they're the two halves this team's workflow calls a "Design brief": the product in one paragraph, tech constraints, tokens, and a screen/component inventory.*

## What it is, in one paragraph

Market Pulse is an institutional-grade financial intelligence Android app covering US equity markets. It combines macro indicators (26 quantitative signals across valuation, macro vitals, tactical momentum, and systemic risk), per-stock analysis (the Magnificent 7 today, expanding), curated news, AI-generated market briefings, and forward-looking economic event playbooks. The premise: give a solo retail investor the kind of daily briefing an institutional trader gets from a research desk — pre-market posture, mid-day dashboard, end-of-day synthesis, weekend playbook — without asking them to assemble it from ten different sources.

## Who it's for

Retail investors who are already engaged with markets — they read financial press, they know what NAAIM exposure is, they check the VIX daily. **Not beginners, not day traders.** People who want the synthesis, not the raw feed.

## Non-goals

- **Not a broker.** No order execution, no portfolio linking.
- **Not a real-time trading terminal.** Data cadence is minutes-to-hours, not milliseconds.
- **Not a social network.** No sharing, no leaderboards, no user-generated content.
- **Not a general-purpose news reader.** Curated to markets, filtered by AI.

These matter for design as much as engineering: no ticker-tape urgency, no gamified engagement patterns, no comment threads or social proof UI.

## Guiding principles

1. **Synthesis over feeds.** Every screen answers "what does this mean," not just "what happened."
2. **Aligned narrative.** Every AI-generated piece — dashboard briefing, indicator synthesis, risk spotlight, weekly playbook — reads from the same daily macro cache, so they never contradict each other across screens.
3. **Freshness without polling.** The app reacts to backend sync signals rather than the user pulling to refresh out of doubt — which is also why a visible "last updated" timestamp matters more here than in a typical app (see *Data rhythm* below).
4. **Institutional voice.** No emoji, no hype, no "to the moon." The tone is a research analyst who respects the reader's time. This applies to UI copy as much as AI-generated text — button labels, empty states, and error messages should read the same way.

## Product surface

Five bottom-nav tabs, plus one screen reached only by drilling in from another. Each maps to a backend domain that refreshes on its own cadence (see *Data rhythm*).

| Tab | What it shows | Key components already built |
|---|---|---|
| **Overview** (Dashboard) | Live market snapshot: Fear & Greed index, RSI/SMA/MACD momentum, per-asset price cards, a sector rotation heatmap, a news preview (tapping it pushes into News) | `UnifiedScoreHeaderCard`, `UniversalMetricCard`, `SparkLineChart`, `SectorHeatmapSection`, `NewsPreviewSection` |
| **Indicators** | The 26 tracked signals, grouped into four categories — valuation, macro vitals, tactical momentum, systemic risk — plus an AI synthesis tying them together ("the Five Pillars") | Tabbed layout (`MacroVitalsTab`, `MarketActionTab`, `MarketPhaseTab`), `IndicatorDetailSheet`, `HorizonBriefingScreen` |
| **Summary** (Daily Pulse) | The flagship AI-generated market briefing — a different narrative for market-hours, after-hours, and weekend, all reading from the same daily cache | Markdown-rendered AI copy (Compose-Markdown), one of the few screens with sealed `Loading/Success/Error` UI state because it genuinely renders different layouts per state |
| **Insights** | Three sub-views: AI tail-risk spotlight, the weekly economic "event playbook" (with mid-week actuals updates), and institutional positioning (NAAIM exposure + FRED data) | `MarketRisksView`, `WeeklyPlaybookView`, `MarketPostureView`, `RiskBottomSheet` |
| **Analysis** | Per-symbol deep dive — technicals, fundamentals, and an AI "deep study" — for the Magnificent 7 today, expanding over time. No default selection; nothing renders until the user picks a symbol | `StockAnalysisComponents` (chip row + detail composables), `ScoreGauge`, `SpeedometerGauge`, `DonutScoreCard` |
| **News** *(not a tab — reached from Overview)* | Curated, AI-filtered market news. Used to be a bottom-nav tab; now only reachable by tapping a preview card on Overview, which is why it carries its own top bar and back button | `NewsScreen` with its own `Scaffold`/`TopAppBar` |

Shared cross-screen components worth knowing about before designing something new: `UniversalGaugeCard`, `UniversalMetricCard`, `PutCallHorizontalBar`, `VixFullWidthCard`, and a family of bottom sheets (`AssetBottomSheet`, `FrameworkSheet`, `IndicatorDetailSheet`, `MarketBottomSheet`, `RiskBottomSheet`) used for progressive disclosure instead of pushing a new screen.

## Data rhythm

Different domains refresh on genuinely different cadences — this should inform how prominently each screen surfaces a timestamp, and whether a stale value needs its own visual state:

| Domain | Refresh cadence |
|---|---|
| Dashboard prices | Every 15 min during market hours |
| Indicators (26 signals) | End-of-day (cost-gated; hourly is the target) |
| Indicator AI synthesis | Once at market close |
| News | 3× per weekday (8 AM, 12 PM, 4 PM ET) |
| Stock technicals (Analysis) | Every 15 min during core hours, plus EOD and after-hours passes |
| Fundamentals | Weekly (Sunday) + mid-week (Wednesday) |
| Weekly playbook | Sunday 10 AM, then mid-week actuals updates |
| Market risks | Weekday close + Sunday |
| Institutional posture | Weekday 6 PM |

A screen showing a stale value with no visible "as of" timestamp is a real UX gap here, not a nice-to-have — the whole premise is trustworthy synthesis, and synthesis without a timestamp reads as guessing.

## Domain vocabulary a designer will hit

- **Signal color** — the app's own domain concept: `GREEN` (bullish) / `YELLOW` (neutral or warning) / `RED` (bearish) / `UNKNOWN` (data missing). Every status color in the UI ultimately resolves to one of these four, never a raw green/red.
- **VIX** — 30-day forward volatility expectation implied by options pricing. The "fear gauge."
- **NAAIM Exposure Index** — a weekly survey of active manager net long exposure, roughly -200 to +200.
- **Fear & Greed Index** — CNN's composite sentiment score, shown on Overview.
- **The Five Pillars** — the five indicator endpoints (AI synthesis, valuation, macro vitals, tactical momentum, systemic risk) that together make up the Indicators tab.

## Tech constraints for design

- **Material 3 / Jetpack Compose.** Mockups need to map to real composables, not just look right — think in terms of `Card`, `Row`/`Column` nesting, `Scaffold`, bottom sheets, not arbitrary layout.
- **Every screen is already split stateful/stateless** (`XRoute` + `XScreen`) — a redesign changes `XScreen` composition; it doesn't need to touch data-loading code.
- **Existing components should be reused or evolved, not duplicated.** The gauge/sparkline/card component family above already covers most data-display needs — check it before proposing a net-new pattern.
- **Dark and light mode both matter.** The app is dual-theme today (see `STYLE_SPEC.md` for exactly what that looks like currently, including where it currently falls short — a real light-mode contrast issue and a flat, shadow-less card system are both documented there).

## Status

- **Backend:** in production — Cloud Functions v2, ~20 deployed functions, running against real market data.
- **Android:** in active development. Core screens shipped (Overview, Indicators, Insights, News, Summary, Analysis).
- **Design:** pre-branding. This doc plus `STYLE_SPEC.md` are the starting brief for that work.

## Further reading

- `STYLE_SPEC.md` — current color, type, spacing, and shape tokens as implemented today, with gaps flagged.
- `ARCHITECTURE.md` — how data flows through the Android app (Remote → Room → Repository → ViewModel → Screen).
- `CLAUDE.md` — conventions for anyone writing code in this repo.
- Notion brain (`marketPulse-brain`) — `00 — Product Brief`, `60 — Handoff`, and `70 — Data & Domain` were the primary sources for this document; `70` in particular carries a fuller data-provider and market-calendar glossary than reproduced here.
