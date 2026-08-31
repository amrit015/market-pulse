# Cross-repo contracts

The Android side has invisible dependencies on backend field/flag names — changing one side alone
breaks the other, sometimes silently.

**Status:** restructured 2026-08-31 from the prior root `ARCHITECTURE.md`/`CLAUDE.md`. Content
re-verified against source during the move. Backend equivalents are documented in the backend
repo's own `CLAUDE.md` and Notion `10 — Architecture`; the canonical inventory of flags,
collections, and producers lives there, not here.

- **Sync flag names** in `SyncManager` must match `updateSyncRegistry` calls in the backend
  engines (e.g. `stock_analysis_eod`, `market_news_updated`). Renaming one is a two-repo change.
- **Direct Firestore reads** (`market_overview`, historically `market_stocks`) rely on
  `@get:PropertyName`/`@set:PropertyName` matching backend field names exactly. Silent break if
  either side changes alone — no compiler or deserialization error, just a field that reads as
  `null`/default forever.
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
