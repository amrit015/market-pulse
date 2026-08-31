# Market Pulse

An institutional-grade financial intelligence Android app — macro indicators, per-stock analysis,
curated news, and AI-generated market briefings, brought together into a single daily read.

## Stack

Kotlin · Jetpack Compose (Material 3) · Dagger Hilt · Room · Retrofit + Moshi · Firebase Firestore
· Compose Navigation. Backend is Firebase Cloud Functions v2 (separate repo).

## Getting started

```bash
./gradlew installDebug   # build and install debug on a connected device/emulator
./gradlew assembleDebug  # just compile
./gradlew test           # unit tests
./gradlew lint           # lint
```

Requires a `local.properties` at the repo root (gitignored) with your local SDK path and any
debug secrets referenced in `app/build.gradle.kts`.

## Docs

- [`CLAUDE.md`](CLAUDE.md) — conventions and defaults for working in this codebase.
- [`docs/`](docs/) — architecture, guidelines, and the theming system, in detail.
- [`docs/product-brief.md`](docs/product-brief.md) — what the app does and who it's for.
