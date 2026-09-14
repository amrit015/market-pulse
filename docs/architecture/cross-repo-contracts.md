# Cross-repo contracts

The Android side has invisible dependencies on backend field/flag names — changing one side alone
breaks the other, sometimes silently.

**Status:** restructured 2026-08-31 from the prior root `ARCHITECTURE.md`/`CLAUDE.md`. Content
re-verified against source during the move. Backend equivalents are documented in the backend
repo's own `CLAUDE.md` and Notion `10 — Architecture`; the canonical inventory of flags,
collections, and producers lives there, not here.

- **Sync flag names** in `SyncManager` must match `updateSyncRegistry` calls in the backend
  engines (e.g. `stocks_updated`, `market_news_updated`). Renaming one is a two-repo change.
  **2026-09-14:** `stocks_updated` replaced the old two-flag `stock_analysis_eod`/
  `stock_analysis_after_hours` pair — the backend now fires one flag from the stock-analysis hub's
  own completion check rather than per-worker. If you see the old names anywhere, they're stale.
- **8 more sync flags added 2026-09-14, all read-only pulls, not push-triggers.** Unlike the 8
  flags above (each push-triggers its own domain's `refresh...()` the moment `SyncManager`'s
  listener sees it advance), these 5+3 flags back per-*item* freshness checks — `ChartsRepositoryImpl`
  (`charts_stocks_updated`, `charts_equity_sector_updated`, `charts_sentiment_updated`,
  `charts_futures_commodities_updated`, `charts_crypto_updated`) and
  `MetricHistoryRepositoryImpl`/`InsightsHistoryRepositoryImpl` (`indicator_charts_updated`,
  `posture_charts_updated`, `positioning_charts_updated`) each pull the current value of the
  relevant flag from `SyncManager.chartSyncTimestamps` at the moment they're asked to refresh a
  specific symbol/metric, and compare it against that one item's own cached `lastSyncedTimestamp`
  — see `data-flow.md`'s "Per-item freshness" section for why this needed a different shape than
  the push model. One flag per *backend batch that writes together* (not global, not per-symbol) —
  get this grouping wrong on either side and the flag either never fires usefully or fires on
  every unrelated write.
- **`market_charts`/`market_intraday` deliberately do NOT carry a client-facing `timestamp` field**,
  despite the `last_updated`-vs-`timestamp` convention below. This was tried (`timestamp` derived
  from `last_updated` on both endpoints) and reverted 2026-09-14 — checking a per-document
  timestamp still requires the full fetch it's meant to avoid, and neither collection is in
  `firestore.rules`' open-read list anyway, so a direct client listener isn't an option either. The
  8 sync flags above replace that need. Don't re-add a per-doc `timestamp` to either endpoint
  without re-reading why this was reverted (backend's `CHARTS.md §12`).
- **Direct Firestore reads** (`market_overview`, historically `market_stocks`) rely on
  `@get:PropertyName`/`@set:PropertyName` matching backend field names exactly. Silent break if
  either side changes alone — no compiler or deserialization error, just a field that reads as
  `null`/default forever.
- **`market_overview/technical_summary` had a hard-cutover schema rewrite, 2026-09.** The old flat
  `summary: String?` (rendered as one markdown blob) and `timestamp: Long?` fields were deleted
  outright, no dual-write/transition window. Replaced with `synthesis: {headline, detail,
  generated_at, model, content_flags}` + a top-level `state: "unavailable"|"current"` (sibling to
  `synthesis`, **not** nested inside it — differs from Posture/Positioning's own synthesis field,
  where `state` sits inside the synthesis object) + an additive, nullable `daily_digest:
  {sections: [{category, heading, body}]}` (present only when something material changed that day —
  absent is the common case, not an error). `RemoteDashboardDataSourceImpl` now parses this via
  `doc.toObject(NetworkTechnicalSummary::class.java)` instead of the old manual
  `doc.getString("summary")`/`doc.getLong("timestamp")` pulls. Of `synthesis`'s fields, only
  `headline`/`detail`/`generated_at` are modeled on the Android side (`generated_at` backs the
  Dashboard hero card's "Analyzed as of" line) — `model`/`content_flags` are real fields but nothing
  in this app renders them yet (same as Posture/Positioning's own `content_flags`, captured in their
  domain models but never displayed either), so they're not modeled here; add them if a real
  consumer shows up rather than pre-emptively.
- **`market_overview/{symbol}.description` (the static per-asset blurb) was removed from every
  asset doc entirely, same 2026-09 hard cutover, no client-side field to read anymore.** The
  Android client didn't get the original backend copy for this pass — `AssetDetailScreen.kt` now
  sources this text from a bundled `assets/asset_descriptions.json` + `AssetDescriptionProvider`
  instead (see `compose-conventions.md`'s Glossary content section), covering only the fixed asset
  set Dashboard renders. If the backend's original strings are ever handed over, that's a content
  swap in the JSON file, not a schema change.
- **`market_overview/sector_rotation` was floated as a new standalone doc, then explicitly
  descoped ("optional, not required for this pass... nothing reads it today")** before the backend
  ever started writing it. A Rotation Read card and its full read/cache path were built against it
  briefly in the same 2026-09 window and then fully removed (including the Room migration that had
  added a column for it) once that was clarified — if this doc goes live later, it needs a fresh
  read path built from scratch, not a revert of that removal.
- **Retrofit response shapes** must match backend Express route bodies. Fails loud
  (deserialization error), but still a coordinated change.
- **`last_updated` vs `timestamp` on backend responses.** Several backend domains — the stocks
  domain (`/stocks/previews`, `/stocks/{symbol}/detail`) and `market_indicators/ai_synthesis`
  confirmed so far — include both a `last_updated` string (pre-formatted for
  human/Firestore-console readability, e.g. `"August 7, 2026 at 6:15:44 PM UTC-7"`) and a
  `timestamp` (epoch millis) in the same response. The app only ever consumes `timestamp` for "as
  of" display; `last_updated` is intentionally left unmodeled in the `Network*` DTOs (see the doc
  comments on `NetworkStockPreview`/`NetworkStockDetail`/`NetworkAiSynthesis`). Don't add
  `last_updated` back in — check for this same pair before modeling any new backend response.
  **2026-08-22 incident:** the indicators `schema_version 2` revamp modeled `last_updated` as a
  `String?` on `NetworkAiSynthesis` anyway (missed that this rule applied here too) and it broke
  JSON parsing against a live document — the field isn't reliably a plain JSON string. Removed;
  `timestamp` alone drives the executive hero's "Analyzed as of" display. Treat this rule as
  binding for every backend response, not just the two domains named above.
