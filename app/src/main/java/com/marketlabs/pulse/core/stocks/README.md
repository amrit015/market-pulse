# core/stocks

**Purpose:** per-stock analysis — the preview list, per-symbol detail, and the larger Deep Dive document.

- **Screens / ViewModels:** `ui/screens/stocks/views/StockAnalysisRoute.kt` and `StockAnalysisViewModel.kt` (`MARKET_ANALYSIS`); `detail/StockDetailRoute.kt` and `StockDetailViewModel.kt`; `deepdive/DeepDiveRoute.kt` and `DeepDiveViewModel.kt`; plus the timeline list screens under `detail/timeline/`.
- **Repository:** `StockAnalysisRepository` / `StockAnalysisRepositoryImpl`.
- **Backend dependency:** Retrofit `stocks/previews`, `stocks/{symbol}/detail`, `stocks/{symbol}/detail/deep`.

Previews behave like other domains: `SyncManager` refreshes them off the single `stocks_updated` flag. Detail and Deep Dive are on-demand per symbol and version-compared, meaning the repository compares the preview's `detailVersion` or `deepVersion` against the cached document and skips the fetch when they match; `force = true` bypasses this. A symbol with no deep dive has no cached row (the data source maps a 404 to `null`). This domain is the worked example in `docs/architecture/data-flow.md`; charts and intraday data come from `core/charts` and `core/intraday`.
