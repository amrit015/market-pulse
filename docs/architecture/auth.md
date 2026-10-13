# Auth (anonymous Firebase identity)

**Status:** written 2026-10-12 alongside the implementation. Verified against source at that date.

v1 has no sign-in UI and no account. Every install gets one invisible anonymous Firebase user,
created only after the current legal version has been accepted. The identity exists so the backend
can recognise an install (and, in v2, attach entitlements to it) without knowing who is behind it.
Auth must never make a request fail: every accessor degrades to "no identity yet".

## Pieces (`core/auth/`)

| Piece | Role |
|---|---|
| `AuthRepository` / `AuthRepositoryImpl` | Wraps `FirebaseAuth`. `uid` (flow), `currentUid()`, `ensureSignedIn()`, `idToken()`. The token is the SDK's cached one (`getIdToken(false)`), never a forced refresh. Failures return `false`/`null`, never throw. |
| `AnonymousSignInManager` | Started from `PulseApplication.onCreate`. Waits for `LegalRepository.acceptedVersion >= CURRENT_LEGAL_VERSION`, then signs in; on failure retries with exponential backoff (15 s → 10 min) in the application scope, so nothing in the UI waits on it. |
| `AuthModule` | `@Provides`-in-`object`, like every other module. `FirebaseAuth` itself is provided in `FirebaseModule`. |

The SDK persists the user, so a relaunch finds the same UID. Clearing app data or reinstalling
creates a new one — the privacy policy says so.

## Ordering with legal acceptance

Signing in before acceptance would create an identifier before the user has seen the policy that
discloses it. The manager therefore filters on the accepted version, not just "has accepted
something". Bumping `CURRENT_LEGAL_VERSION` (Part of the v3 change that introduced this) makes
existing installs re-accept before they get an identity. `AnonymousSignInManagerTest` pins both the
"nothing before acceptance" and "older version isn't enough" cases.

## Request header

`network/interceptor/AuthInterceptor` adds `Authorization: Bearer <idToken>` to every request on
the `MarketPulseClient`, ahead of the logging interceptors so they see (and redact) it.

- No user → the interceptor returns immediately, without blocking.
- A user but no token, any exception, or a token fetch slower than 1.5 s → the request goes out
  unchanged. The backend does not require the header in v1.
- A caller-supplied `Authorization` header is left alone.

`HeaderLoggingInterceptor` truncates `Authorization` the same way it truncates
`X-Firebase-AppCheck`, and the `HttpLoggingInterceptor` (BODY level) is configured with
`redactHeader` for both, since BODY prints every header.

Note: `AppCheckInterceptor` is currently not installed on the client (commented out in
`NetworkApiModule`), so today `Authorization` is the only credential header actually sent.

## Where the UID is shown, and where it must not go

- **Settings → About → "App ID"** (`AboutViewModel` → `AboutScreen`), selectable, with a Copy
  action. Users quote it for deletion and support requests.
- **Nowhere else.** It is deliberately not set as the Analytics user ID or the Crashlytics user ID;
  analytics stays tied to the app-instance ID only.

## Entitlements (v2 prep)

`core/entitlement/EntitlementRepository.isPro` is the single "is this user Pro?" check. v1's
`OpenEntitlementRepository` returns `true` for everyone. Summary reads it through
`SummaryAccess` / `SummaryFeature` (see `core/summary/README.md`), so a v2 gate is a change to the
repository plus the locked-state UI, not a hunt through screens.

## Tests

`AnonymousSignInManagerTest` (no sign-in before acceptance, retry/backoff) and
`AuthInterceptorTest` (header present with a token; absent with no user, no token, or a timeout;
request still succeeds) — both against `FakeAuthRepository`.

## Console prerequisite

The **Anonymous** provider must be enabled under Firebase Console → Authentication → Sign-in
method, or `ensureSignedIn()` will keep failing and retrying quietly.
