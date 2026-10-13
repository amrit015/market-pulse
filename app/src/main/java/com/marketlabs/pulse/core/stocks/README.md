# core/stocks

**Purpose:** per-stock analysis — the preview list, per-symbol detail, and the larger Deep Dive document.

- **Screens / ViewModels:** `ui/screens/stocks/views/StockAnalysisRoute.kt` and `StockAnalysisViewModel.kt` (`MARKET_ANALYSIS`); `detail/StockDetailRoute.kt` and `StockDetailViewModel.kt`; `deepdive/DeepDiveRoute.kt` and `DeepDiveViewModel.kt`; plus the timeline list screens under `detail/timeline/`.
- **Repository:** `StockAnalysisRepository` / `StockAnalysisRepositoryImpl`.
- **Backend dependency:** Retrofit `stocks/previews`, `stocks/{symbol}/detail`, `stocks/{symbol}/detail/deep`.

Previews behave like other domains: `SyncManager` refreshes them off the single `stocks_updated` flag. Detail and Deep Dive are on-demand per symbol and version-compared, meaning the repository compares the preview's `detailVersion` or `deepVersion` against the cached document and skips the fetch when they match; `force = true` bypasses this. A symbol with no deep dive has no cached row (the data source maps a 404 to `null`). This domain is the worked example in `docs/architecture/data-flow.md`; charts and intraday data come from `core/charts` and `core/intraday`.

## Reads and the Deep Dive banner

- `plain_read` now lives on the **detail** doc, not the preview. The list card's lead line is `daily_digest.headline` (hidden when null). Stock Detail shows `plainRead` in the chrome above the tabs where the technical read used to be; `technical_read` moved onto the Technicals tab below Returns. Both are nullable until a symbol's next analysis run writes the new shape. The preview's `plainRead` column is no longer read (DB v33 only adds `plainRead` to `market_stock_details`).
- The Deep Dive banner is the full card until the symbol's deep dive has been opened (`clickedDeepDiveSymbols`), then a one-line pill (first line + chevron) in the same spot. That set is per symbol, so a newer deep dive does not re-show the full banner for a symbol already opened.
- Every card on the Stock Detail tabs (Digest, Deep Study, Scenarios included) uses the `DATA` card style.
