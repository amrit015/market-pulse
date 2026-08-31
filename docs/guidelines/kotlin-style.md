# Kotlin style & DI conventions

Naming, dependency injection, and null-handling conventions for this repo. Framework/Compose-
specific conventions are in `@docs/guidelines/compose-conventions.md`.

**Status:** restructured 2026-08-31 from root `CLAUDE.md`'s Conventions section. Re-verified
against source during the move.

## Dependency injection (Hilt)

**100% `@Provides`-in-`object` style — no `@Binds`/abstract modules anywhere.** Three-tier
domains get exactly three providers (remote data source, local data source, repository). Always
`Impl → Interface`, always `@Singleton`.

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DashboardModule {
    @Provides
    @Singleton
    fun provideRemoteDashboardDataSource(
        remoteDataSourceImpl: RemoteDashboardDataSourceImpl
    ): RemoteDashboardDataSource = remoteDataSourceImpl

    @Provides
    @Singleton
    fun provideDashboardRepository(
        dashboardRepositoryImpl: DashboardRepositoryImpl
    ): DashboardRepository = dashboardRepositoryImpl
}
```

Never introduce `@Binds` — it'd be inconsistent with every existing module (9+ domain modules, as
of the last count, all `@Provides`-in-`object`).

Lighter domains (no Room caching, no remote/local split — see
`@docs/architecture/overview.md`'s "when the full 5-layer scaffold isn't needed") still keep the
`Impl → Interface` + `@Provides`-in-`object` shape, just with fewer providers.

## Null handling

Default fields to `null`, not arbitrary defaults. The exception is Firestore DTOs consumed via
`toObject()`, which need a no-arg constructor — those use `var` properties with cheap defaults
only where a primitive genuinely can't be null (`var symbol: String = ""`,
`var price: Double = 0.0`); everything else is `null`. Domain models are always immutable `val`,
nullable default `null`.

Only add a default where it's genuinely necessary, and flag it before doing so if you're adding
new code — an unexplained default is easy to mistake for "this can never actually be null."

## Naming

- **Resource IDs** — `snake_case`, loosely `<screen_or_context>_<purpose>` (e.g.
  `news_screen_title`, `radar_vulnerability_score`). Full resource conventions (strings,
  dimensions) in `@docs/guidelines/compose-conventions.md`.
- **Domain/network/entity models** — `Domain<X>` / `Network<X>` / `<X>Entity`, one per layer, per
  the package layout in `@docs/architecture/overview.md`. Never reuse one class across layers.
