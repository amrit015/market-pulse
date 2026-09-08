# Data flow — transport, caching, sync

How data actually moves from the backend to a screen, with one traced example.

**Status:** restructured 2026-08-31 from the prior root `ARCHITECTURE.md` §2–4, §7. Content
re-verified against source during the move.

```
Backend (Cloud Functions v2)          Android
┌─────────────────────┐               ┌──────────────────────────────────────────────────┐
│ Firestore documents │──writes──────▶│ market_stocks, market_overview, market_news, ...  │
└─────────────────────┘               └──────────────────────────────────────────────────┘
        │                                          │
        │ read via Express API                     │ read directly (client SDK)
        ▼                                          ▼
 RemoteXDataSourceImpl (Retrofit)         RemoteXDataSourceImpl (FirebaseFirestore)
        │                                          │
        └───────────────┬──────────────────────────┘
                        ▼
              LocalXDataSourceImpl (Room DAO)
                        ▼
                  XRepositoryImpl
                        ▼
                    XViewModel  (StateFlow<XUiState>)
                        ▼
                   XRoute → XScreen (Compose)
```

## A. Retrofit + Moshi, against a Cloud Functions Express API (the default)

Most domains (`indicators`, `news`, `marketRisk`, `posture`, `positioning`, `summary`,
`weeklyPlaybook`, `stocks`) call a REST API exposed by the backend's
`functions/src/api/marketPulse.ts` — an Express app deployed as a single Cloud Function, `api`.
Each route is a thin wrapper that reads one Firestore document/collection and returns its JSON
as-is.

- `network/api/<Domain>Api.kt` — a plain Retrofit interface (`@GET("indicators/synthesis")`).
- All domain `Api` interfaces share **one** `Retrofit` instance, `@Named("MarketPulseRetrofit")`,
  built once in `network/retrofit/NetworkApiModule.kt` against `Constants.MARKET_PULSE_BASE_URL`,
  with a `MoshiConverterFactory` and an `OkHttpClient` that (optionally) carries
  `AppCheckInterceptor` + logging.
- `network/model/<domain>/Network<X>.kt` — `@JsonClass(generateAdapter = true)` DTOs, `val`
  properties, `@Json(name = "snake_case")` mapping. Pure Moshi models — no Firestore annotations.
- `Remote<Domain>DataSourceImpl` calls the `Api`, catches exceptions, maps
  `Network<X> → Domain<X>` via the domain's mapper file, and returns `Result<Domain<X>>`.

A few domains fan out concurrent calls and merge them — `RemoteIndicatorsDataSourceImpl` fires 5
`async { api.get...() }` calls (the "Five Pillars") and assembles one `MarketIndicators` object;
`RemoteStockDataSourceImpl` fires one `api.getStockAnalysis(symbol)` per tracked ticker
concurrently and treats a `404 HttpException` per-symbol as "not analyzed yet" rather than
failing the whole batch.

## B. Direct Firestore client SDK reads (no backend endpoint)

`dashboard` (`market_overview` collection) has no REST endpoint — backend jobs write straight to
Firestore with nothing in front. So `RemoteDashboardDataSourceImpl` injects the shared
`FirebaseFirestore` singleton (provided once in `di/FirebaseModule.kt`) directly and either:

- keeps a live `addSnapshotListener(...)` wrapped in `callbackFlow` (dashboard does this —
  market-open/closed state and live asset prices stream in continuously), or
- does a one-shot `.get().await()` (`kotlinx.coroutines.tasks.await`) for on-demand reads.

DTOs used this way carry **both** Moshi (`@Json`) and Firestore (`@get:PropertyName`/
`@set:PropertyName`) annotations with `var` properties and no-arg-constructor-safe defaults,
because `DocumentSnapshot.toObject()` needs reflection-friendly classes. `stocks` used to work
this way — see the worked example below — but was migrated to strategy A once the backend added
`/stocks/:symbol` and `/stocks/tracked`.

## Caching — Room

Every domain except the ones intentionally left one-shot-only caches its data in a single shared
Room database, `AppDatabase` (schema **version 25** as of 2026-09-07 — `di/DatabaseModule.kt` builds
it with `DatabaseMigrations.ALL_MIGRATIONS` applied, no destructive fallback; re-check this number
before citing it, it moves often).

- **Entities** (`storage/database/entity/`) mostly mirror the domain model 1:1. Two row-shapes
  exist:
  - *Singleton-per-day/id* (`IndicatorsEntity` keyed by `dateId`, `MarketPostureEntity` keyed by
    a fixed `id`) — one row holds the whole domain's current snapshot, and only that one row is
    ever read or written.
  - *Multi-row-per-symbol* (`AssetOverviewEntity` keyed by `symbol`, `StockEntity` keyed by
    `symbol`) — used when a domain is naturally a list of independent items.
  - **`MarketPulseEntity` (keyed by `dateId`) moved from the first bucket to the second, 2026-09-07
    (Summary calendar strip).** Before, exactly one row ever existed (whichever day's report was
    most recently generated, found via `ORDER BY lastUpdated DESC LIMIT 1` and always overwritten
    on the next sync) — genuinely a singleton despite the `dateId` key. The calendar strip now
    caches one row per day the reader has actually opened (today's live row, plus every past date
    swiped to — a real report row, or a tombstone (`hasReport = false`) confirming none exists), so
    the table can hold many rows at once and every read goes through
    `SummaryDao.getByDateId(dateId)`, never a bare "latest" query. **The "latest row" query pattern
    silently stops meaning "today's row" the moment a domain like this grows a second, independent
    reason to have more than one row** — check what a `dateId`/timestamp-keyed entity is actually
    being used for before assuming "keyed by dateId" alone tells you it's still a singleton.
  - **A related latent bug this same feature made load-bearing:** `Long.toDateIdString()`
    (`utils/DateExtension.kt`) read epoch millis via `Instant.ofEpochSecond(this)` instead of
    `ofEpochMilli` — silently produced a garbage far-future date, harmless for years because
    `dateId` was only ever used as an opaque Room primary key, never queried by value or displayed.
    The calendar strip's `syncPastDate` now looks rows up BY `dateId`, so the wrong conversion
    would have broken every past-date cache check. Fixed alongside the calendar work. General
    lesson: a timestamp/date helper with no visible bug today can still be silently wrong — audit
    its actual arithmetic before a new caller starts depending on its output being correct, not
    just present.
- **Nested objects don't get their own tables.** A domain's rich nested structure is stored as a
  single JSON text column, (de)serialized by a per-domain `*Converters.kt` class using a local
  `Moshi.Builder().add(KotlinJsonAdapterFactory())` instance. This keeps the domain model reusable
  as-is between the Entity and the app layer.
  - **Caveat (learned 2026-08-18, `market_pulse`):** a *nested field's Kotlin type* changing
    (e.g. `String` → enum) is a breaking schema change too, even when the SQL column itself
    (still just `TEXT`) doesn't need a migration — a row cached under the old type crashes
    Moshi's default enum adapter on read. Needs its own migration bump, even a no-op
    `DELETE FROM` to force a re-fetch.
  - **Recurrence (2026-09-07, `market_stock_details.setupConfirming`/`setupConflicting`):** same
    failure shape, a `List<String>` column moved onto a `List<DomainSetupSignal>` shape (see
    `card-heading-conventions.md`'s Fifteenth step) — real crash on a device with pre-change cached
    data, `JsonDataException: Expected BEGIN_OBJECT but was STRING`. Fixed with a migration that
    `UPDATE`s just the two affected columns to `NULL` (a surgical alternative to the whole-table
    `DELETE FROM` above, when only specific columns' shape changed). Treat this as the standing
    rule, not a one-off: **any type change on an existing JSON-blob column needs a migration**,
    full stop — "the SQL column type didn't change" is never sufficient reasoning to skip one.
  - **Narrower alternative (2026-08-22, `indicators` `schema_version 2`):** if the entire nested
    object is already nullable on the containing domain model, wrapping the converter's
    `fromJson` in `try/catch (JsonDataException)` that logs and returns `null` lets a
    stale-shaped row degrade to "no data yet" instead — no migration needed. Use the real
    migration bump when the field isn't nullable, or "just refetch" isn't an acceptable degrade.
- **DAOs** expose a `Flow<Entity?>` / `Flow<List<Entity>>` read stream plus `REPLACE`-conflict
  inserts, and a per-domain `getLastSyncedTimestamp()` / `updateLastSyncedTimestamp()` pair used
  purely for sync bookkeeping (below) — not cache-expiry. **There is no TTL/staleness check
  anywhere** — a cached value is shown until something explicitly triggers a refresh.
- **`LocalXDataSourceImpl`** is the only thing that touches the DAO — maps `Entity ↔ Domain` via
  the mapper file.

## Keeping the cache fresh — `SyncManager`

`core/sync/SyncManager` is a single `@Singleton` holding one Firestore `addSnapshotListener` on
`system/sync_status` — a document the backend updates via a shared
`updateSyncRegistry(db, flagName)` helper every time it finishes a job (e.g.
`market_news_updated`, `stock_analysis_eod`, `weekly_playbook_actuals_updated`).

Every screen's ViewModel calls `syncManager.startListening()` in `onStart()` and
`stopListening()` in `onStop()` — idempotent, so concurrent calls from multiple screens are safe.
On each snapshot, `SyncManager` walks a fixed list of domain blocks:

```kotlin
val newTime = snapshot.getLong("<domain>_updated") ?: 0L
val localTime = xRepository.getLastSyncedTimestamp() ?: 0L
if (newTime > localTime) {
    xRepository.refresh(force = true)
    xRepository.updateLastSyncedTimestamp(newTime)
}
```

This is what makes the app reactive without polling — the backend flags exactly what changed,
and only that domain's `refresh...()` fires.

`stocks` is the one domain with **two** flags feeding the same repository
(`stock_analysis_eod` / `stock_analysis_after_hours`), combined with `maxOf(...)` — same pattern
`weeklyPlaybook` uses for Sunday-generation vs. mid-week-actuals.

Repositories also self-trigger `refresh...(force = false)` from their owning ViewModel's
`onStart()` as a "don't wait for the next Firestore write" pre-warm — `SyncManager` handles
*ongoing* freshness, the ViewModel's own `onStart()` handles *first paint*.

## Dependency injection — Hilt

Four centralizing modules everything else plugs into:

- **`FirebaseModule`** — the one `FirebaseFirestore` singleton.
- **`NetworkApiModule`** — the one shared `Moshi`, `OkHttpClient` (`@Named("MarketPulseClient")`),
  `Retrofit` (`@Named("MarketPulseRetrofit")`), and one `provide<Domain>Api(retrofit)` per domain.
- **`DatabaseModule`** — the one `AppDatabase`, and one `provide<Domain>Dao(db)` per domain.
- **`FinnHubApiModule`** — a *separate* OkHttp/Retrofit stack (different base URL, different auth
  interceptor) plus a WebSocket client, kept intentionally isolated from the `MarketPulseRetrofit`
  stack.

Each domain's own `di/<Domain>Module.kt` wires its own bindings — binding-style conventions
(strictly `@Provides`-in-`object`, no `@Binds`) are in `@docs/guidelines/kotlin-style.md`.

## A worked example: `stocks`

Useful as a complete tour of the pattern, and the domain with the most interesting history:

1. Backend (`functions/src/scheduled/stocks/stockAnalysisEngine.ts`) computes technicals + an AI
   "deep study" per Magnificent-7 symbol, writes `market_stocks/{TICKER}`, flags
   `stock_analysis_eod`/`stock_analysis_after_hours` on `system/sync_status`.
2. Originally the Android app read `market_stocks` straight off the Firestore SDK (strategy B,
   mirroring `dashboard`). Once the backend exposed `GET /stocks/tracked` and `GET /stocks/:symbol`
   in `marketPulse.ts`, `RemoteStockDataSourceImpl` was migrated to Retrofit (strategy A) — the
   `NetworkStockDeepStudy` DTO simplified from a dual Moshi+Firestore model down to a plain Moshi
   one in the process.
3. `StockEntity` (keyed by `symbol`) + `StocksConverters` cache the result in Room;
   `StockAnalysisRepository` exposes `getTrackedStocksStream(): Flow<List<StockAnalysis>>` and
   `refreshTrackedStocks(force): Result<Unit>`.
4. `StockAnalysisViewModel` holds the chip-selection state (`selectStock(symbol)`) itself — no
   default selection; nothing renders until the user taps a chip.
5. The whole feature was originally a Dashboard-embedded widget (chip row inline, tapping a chip
   pushed a detail screen), then consolidated into its own bottom-nav tab (`Analysis`), replacing
   `News` there — `News` moved the other direction, tab → Dashboard-preview-triggered push screen.
