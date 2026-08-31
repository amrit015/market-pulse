# Architecture overview

How the Android app is put together: module shape, package layout, tech stack, and the boundary
to the backend. Package root: `com.marketlabs.pulse`.

**Status:** restructured 2026-08-31 from the prior root `ARCHITECTURE.md`, content re-verified
against source during the move.

## Tech stack

Kotlin, Jetpack Compose (Material 3), Dagger Hilt, Room, Retrofit + Moshi, Firebase Firestore
(client SDK for some domains, backing store for all of them), Compose Navigation, Vico (charts),
Compose-Markdown. Single Gradle module (`app/`) — no multi-module split. `minSdk = 26`,
`compileSdk`/`targetSdk = 36`, Kotlin/Java 17.

## The shape of a domain

Every feature ("domain") — `dashboard`, `indicators`, `news`, `marketRisk`, `posture`,
`positioning`, `summary`, `weeklyPlaybook`, `stocks` — is vertically sliced across five package
roots:

```
network/model/<domain>/      Network<X>.kt        — Retrofit/Moshi response DTOs
network/api/<Domain>Api.kt                        — Retrofit @GET interface
network/store/<domain>/      Remote<Domain>DataSource(Impl).kt
storage/model/<domain>/      Domain<X>.kt         — clean, UI-facing domain models
storage/model/<domain>/mappers/                   — Network→Domain, Domain↔Entity
storage/database/entity/     <Domain>Entity.kt    — Room table(s)
storage/database/dao/        <Domain>Dao.kt
storage/database/converters/ <Domain>Converters.kt — Moshi (de)serializers for nested JSON columns
storage/store/<domain>/      Local<Domain>DataSource(Impl).kt — wraps the DAO
core/<domain>/               <Domain>Repository(Impl).kt      — the ViewModel-facing façade
di/<Domain>Module.kt                              — Hilt @Provides bindings
ui/screens/<domain>/         <Domain>ViewModel.kt, <Domain>UiState.kt
ui/screens/<domain>/views/   <Domain>Route.kt, <Domain>Screen.kt
```

Data flows in one direction: **Remote → Local (Room) → Repository → ViewModel → Screen.** Full
mechanics (transport strategies, caching, sync) in `@docs/architecture/data-flow.md`.

**When the full 5-layer scaffold isn't needed:** for a one-shot, non-cached read, a lighter
2-file `core/<domain>` repository talking straight to `FirebaseFirestore` is acceptable and
already precedented — don't force the full stack where nothing needs caching.

## The external boundary — the backend

Backend is Firebase Cloud Functions v2 (not Cloud Run), writing to Firestore. Two transport
strategies exist depending on whether a backend Express route fronts a given Firestore
collection — see `@docs/architecture/data-flow.md` §A/B. The Android side has several invisible
dependencies on backend field/flag names that break silently if only one side changes — see
`@docs/architecture/cross-repo-contracts.md`.

## Presentation layer, navigation, theming

- ViewModel/UiState shape, Route/Screen split, DI conventions, navigation graph shape →
  `@docs/architecture/android.md`
- Card system, color tokens, light/dark presets → `@docs/theming-system/theming-spec.md`
