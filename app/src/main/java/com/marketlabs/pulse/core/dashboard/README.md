# core/dashboard

**Purpose:** live market overview — the market state plus the dashboard asset tiles (indices, sectors, other assets).

- **Screen / ViewModel:** `ui/screens/dashboard/views/DashboardRoute.kt`, `DashboardViewModel.kt`; asset detail in `ui/screens/dashboard/detail/` (`AssetDetailRoute`, `AssetDetailViewModel`).
- **Repository:** `DashboardRepository` / `DashboardRepositoryImpl`.
- **Backend dependency:** Firestore `market_overview`, read directly through a snapshot listener in `RemoteDashboardDataSourceImpl`. There is no REST call, and `SyncManager` has no flag for this domain.

`DashboardRepositoryImpl` starts the listener in its `init` and writes every snapshot into Room, so `getMarketStateStream()` and `getDashboardAssetsStream()` are Room-backed and update the UI as soon as Firestore changes; `refreshDashboard()` has nothing left to do. `DashboardViewModel` also reads `NewsRepository` and `IntradayRepository` (tracking and untracking intraday symbols for the visible tiles). This is the domain the direct-Firestore path in `docs/architecture/data-flow.md` refers to.
