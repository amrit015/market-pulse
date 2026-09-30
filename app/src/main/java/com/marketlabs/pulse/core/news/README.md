# core/news

**Purpose:** the curated news feed — the latest articles plus a short window of archived days.

- **Screen / ViewModel:** `ui/screens/news/views/NewsRoute.kt`, `NewsScreen.kt`, `NewsViewModel.kt` (route `MARKET_NEWS`).
- **Repository:** `NewsRepository` / `NewsRepositoryImpl`.
- **Backend dependency:** Retrofit `news/latest` and `news/history`.

`NewsRepositoryImpl` exposes a Room-backed `getNewsStream()` and has no cache expiration: `refreshNews()` is driven by `SyncManager` (`market_news_updated`) or an explicit pull-to-refresh. `NewsApi.getNewsHistory()` sends no `limit`, relying on the backend's default of the last two archived days. `NewsViewModel` combines the news stream with `AdManager.isAdFree`, so native ads are suppressed for ad-free users; `DashboardViewModel` also reads this repository.
