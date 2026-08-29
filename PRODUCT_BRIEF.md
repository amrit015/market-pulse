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
| **Insights** | Four `PulseTabRow` tabs (2026-08-27, was one long stacked scroll): the weekly economic "event playbook" (with mid-week actuals updates), an AI tail-risk spotlight, institutional **posture** (NAAIM manager exposure, dark pool accumulation, Fed net liquidity), and market **positioning** (AAII retail sentiment, CFTC COT futures positioning across ES/DIA/NQ/RTY, FINRA short interest across SPY/DIA/QQQ/RSP/IWM/MAGS) — posture and positioning are two distinct backend domains, split out once positioning shipped | `WeeklyPlaybookView`, `MarketRisksView`, `MarketPostureView`, `MarketPositioningView`, `RiskBottomSheet`, whole-card taps push a merged glossary-detail page (`GlossaryDetailScreen`) instead of a bottom sheet |
| **Analysis** | Per-symbol deep dive — technicals, fundamentals, and an AI "deep study" — for the Magnificent 7 today, expanding over time. No default selection; nothing renders until the user picks a symbol. Detail screen uses the same `PulseTabRow` tabs Insights does | `StockAnalysisComponents` (chip row + detail composables), `ScoreGauge`, `SpeedometerGauge`, `DonutScoreCard` |
| **News** *(not a tab — reached from Overview)* | Curated, AI-filtered market news. Used to be a bottom-nav tab; now only reachable by tapping a preview card on Overview, which is why it carries its own top bar and back button | `NewsScreen` with its own `Scaffold`/`TopAppBar` |
| **Settings** *(not a tab — reached from the top bar)* | New since the redesign shell landed. Full-screen (own `Scaffold`, no bottom nav), houses the 10-preset theme picker | Preset gallery/swatch grid (`PresetSwatchCard`) |

Shared cross-screen components worth knowing about before designing something new: `UniversalGaugeCard`, `UniversalMetricCard`, `PutCallHorizontalBar`, `VixFullWidthCard`, and a family of bottom sheets (`AssetBottomSheet`, `IndicatorDetailSheet`, `MarketBottomSheet`, `RiskBottomSheet`) used for progressive disclosure instead of pushing a new screen. (`FrameworkSheet` — an Indicators "methodology" sheet — was removed 2026-08-27: its only trigger button had been deleted from `IndicatorsScreen` months earlier, leaving it permanently unreachable dead code.)

**Card, pill, and tab vocabulary are now consolidated app-wide.** Every content card renders through one shared `PulseCard(style: PulseCardStyle)` — `DATA` for raw price/metric cards (Equities, Indicators, VIX, Fear & Greed, Put/Call, Insights' Posture/Positioning cards), `SYNTHESIS` for AI-written or curated content (briefings, news, verdicts, risk/playbook cards). Every small colored badge — sentiment tags, impact levels, the up/down % change indicators — renders through `SignalPill` (and its `DirectionalChangePill` wrapper for directional change specifically). Every screen that switches between a handful of sibling sections (Stock Analysis detail, Insights) renders its tab bar through one shared `PulseTabRow` (2026-08-27) — a horizontally-scrolling row of segmented-control-style chips, solid-fill for the selected one. A new card, pill, or tab pattern should extend one of these, not introduce a fourth. Full token mapping in `STYLE_SPEC.md §6–7, §15`.

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
| Institutional posture (NAAIM/DIX/net liquidity) | Weekday 6 PM |
| Institutional positioning (AAII/COT/short interest) | Weekday 8 PM |

A screen showing a stale value with no visible "as of" timestamp is a real UX gap here, not a nice-to-have — the whole premise is trustworthy synthesis, and synthesis without a timestamp reads as guessing.

## Domain vocabulary a designer will hit

- **Signal color** — the app's own domain concept: bullish / bearish / neutral / warning, plus an as-yet-unresolved `unknown` (data missing) state. This is a fixed four-tier read on the market, locked identically across every theme preset — a themed accent color can never be mistaken for a signal. Every status color in the UI ultimately resolves to one of these tiers, never a raw green/red. See `STYLE_SPEC.md §2`.
- **VIX** — 30-day forward volatility expectation implied by options pricing. The "fear gauge."
- **NAAIM Exposure Index** — a weekly survey of active manager net long exposure, roughly -200 to +200. Part of Insights' **Posture** tab, alongside dark pool accumulation (DIX) and Fed net liquidity.
- **Fear & Greed Index** — CNN's composite sentiment score, shown on Overview.
- **The Five Pillars** — the five indicator endpoints (AI synthesis, valuation, macro vitals, tactical momentum, systemic risk) that together make up the Indicators tab.
- **Positioning** (Insights' fourth tab, added after Posture) — three separate lenses on who's leaning which way, each a genuinely different data source: AAII's weekly retail bull/bear sentiment survey, CFTC COT futures positioning (large speculators' net position + trailing-year percentile, across four index futures — ES/DIA/NQ/RTY), and FINRA short interest (days-to-cover + month-over-month change, across six ETF proxies — SPY/DIA/QQQ/RSP/IWM/MAGS). A crowded reading in either direction is framed as historically preceding a reversal, not a buy/sell signal.

## Tech constraints for design

- **Material 3 / Jetpack Compose.** Mockups need to map to real composables, not just look right — think in terms of `Card`, `Row`/`Column` nesting, `Scaffold`, bottom sheets, not arbitrary layout.
- **Every screen is already split stateful/stateless** (`XRoute` + `XScreen`) — a redesign changes `XScreen` composition; it doesn't need to touch data-loading code.
- **Existing components should be reused or evolved, not duplicated.** The gauge/sparkline/card component family above already covers most data-display needs — check it before proposing a net-new pattern.
- **Theming is a 10-preset system, not a dark/light toggle.** Five light presets (Plum, Navy, Fuchsia, Graphite, Teal), five dark (Lilac, Sky, Sand, Rose, Aqua) — the user picks a preset, which fixes both the appearance mode and the accent together; there's no separate "follow system" switch. A design only needs to work in one mode at a time, but should be checked against at least one light and one dark preset, since only the accent varies between presets in the same mode — everything else (signal colors, neutral surfaces) is shared. See `STYLE_SPEC.md §1–5` for the full token architecture, including the two derived/blended tokens that give price cards vs. AI-content cards their different backgrounds.
- **Flat, shadow-less by convention — one deliberate exception.** Cards, sheets, and surfaces don't use elevation/shadow to differentiate. `FloatingBottomNav` is the one intentional exception (a small `shadowElevation`); it's not license to add shadows elsewhere.

## Status

- **Backend:** in production — Cloud Functions v2, ~20 deployed functions, running against real market data.
- **Android:** in active development. Core screens shipped (Overview, Indicators, Insights, News, Summary, Analysis, Settings).
- **Design:** past pre-branding — a full visual pass has landed. The 10-preset theme system, a consolidated card/pill/tab component vocabulary (`PulseCard`, `SignalPill`, `DirectionalChangePill`, `PulseTabRow`), a collapsing/blending top bar, and a typography pass across every card title are all shipped. What's left is closer to refinement than a from-scratch brief: `STYLE_SPEC.md §14` tracks the specific open items (an unresolved `signal.unknown` color, unbundled type weights, a couple of un-tokenized literals) rather than a wholesale redesign backlog.
- **Glossary content:** standardized 2026-08-27 on bundled JSON (`core/glossary/`, one provider per glossary) — was a mix of hardcoded Kotlin objects and raw `strings.xml` entries across Market/Risk/Stock Analysis/Dashboard glossaries. No user-facing change; content and wording are unchanged, only where it's sourced from.

## Further reading

- `STYLE_SPEC.md` — current color, type, spacing, and shape tokens as implemented today, with gaps flagged.
- `ARCHITECTURE.md` — how data flows through the Android app (Remote → Room → Repository → ViewModel → Screen).
- `CLAUDE.md` — conventions for anyone writing code in this repo.
- Notion brain (`marketPulse-brain`) — `00 — Product Brief`, `60 — Handoff`, and `70 — Data & Domain` were the primary sources for this document; `70` in particular carries a fuller data-provider and market-calendar glossary than reproduced here.
