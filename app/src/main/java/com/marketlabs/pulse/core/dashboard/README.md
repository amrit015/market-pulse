# core/dashboard

**Purpose:** live market overview — the market state plus the dashboard asset tiles (indices, sectors, other assets).

- **Screen / ViewModel:** `ui/screens/dashboard/views/DashboardRoute.kt`, `DashboardViewModel.kt`; asset detail in `ui/screens/dashboard/detail/` (`AssetDetailRoute`, `AssetDetailViewModel`).
- **Repository:** `DashboardRepository` / `DashboardRepositoryImpl`.
- **Backend dependency:** Firestore `market_overview`, read directly through a snapshot listener in `RemoteDashboardDataSourceImpl`. There is no REST call, and `SyncManager` has no flag for this domain.

`DashboardRepositoryImpl` starts the listener in its `init` and writes every snapshot into Room, so `getMarketStateStream()` and `getDashboardAssetsStream()` are Room-backed and update the UI as soon as Firestore changes; `refreshDashboard()` has nothing left to do. `DashboardViewModel` also reads `NewsRepository` and `IntradayRepository` (tracking and untracking intraday symbols for the visible tiles). This is the domain the direct-Firestore path in `docs/architecture/data-flow.md` refers to.

## Sentiment readings (Fear & Greed, Put/Call, VIX)

The change pill is decided by `ui/components/SentimentChange.kt`, shared with Indicators (see `docs/guidelines/compose-conventions.md`). On the asset detail page each of the three also lists its bands (Extreme Greed down to Extreme Fear, the same `BandRow` Indicators uses) with the current band matched to the backend's `rsi_status` by label. Fear & Greed and Put/Call read `metric_glossary.json`; VIX reads `overview_sentiment_bands.json` because Overview's VIX status is a five-level Fear/Greed read, not Indicators' Calm/Elevated/Panic. The change pill also appears beside the price there.
