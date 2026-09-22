# Push notifications — build order, pipeline, verification

> **Status:** built 2026-09-21/22. Verified against source at that time. Companion to
> `@docs/architecture/cross-repo-contracts.md`'s "Push notifications (FCM topics)" section, which
> states the backend contract itself (topic/channel/payload names); this doc covers how the
> Android side is put together and how to change or re-verify it.

## Shape of the feature

Topic-based FCM, no auth, no per-device token registry. Two user-facing toggles (Daily Summary,
Stock Analysis), each opt-in, each mapped to one or more topics. There is no server round-trip for
a toggle — turning one on/off only edits local prefs and calls `subscribeToTopic`/
`unsubscribeFromTopic`.

## Build order

The pieces depend on each other in this order — useful if you're adding a third notification
preference or re-deriving why something is shaped the way it is:

1. **Persisted preferences** (`data/notifications/`) — `NotificationPreferencesRepository`, a
   DataStore-backed `Impl → Interface` pair, same shape as `LegalRepository`. Holds which
   preferences are on, and separately which preferences' "enable notifications" banner (see below)
   has already been answered.
2. **Topic mapping** (`core/notifications/NotificationTopics.kt`) — the one place topic name
   strings live, keyed by preference and `BuildConfig.DEBUG`. Everything else calls into this
   rather than hardcoding a topic string.
3. **Channels** (`core/notifications/NotificationChannels.kt`) — created idempotently in
   `PulseApplication.onCreate()`, before anything else runs. Must happen before any push can
   arrive, since a push naming a channel that doesn't exist yet silently falls back to FCM's own
   "Miscellaneous" channel and the id is unrecoverable after that first delivery.
4. **Subscription manager** (`core/notifications/PushTopicManager.kt`) — `apply()` does the actual
   `subscribeToTopic`/`unsubscribeFromTopic` calls for one preference; `reconcile()` re-derives
   every preference's desired subscription state from the repository and re-issues it. Called once
   per process start (`PulseApplication.onCreate()`) so a failed call, a reinstall, or a toggle
   flipped while offline all self-heal on the next launch, without needing a retry queue.
5. **Messaging service** (`core/notifications/PulseMessagingService.kt`) — only handles the
   foreground case (suppressed, logged) and token refresh (logged only, never stored). Background
   delivery is handled entirely by the system tray from the notification payload; this class isn't
   invoked for it.
6. **Settings screen** (`ui/settings/Notifications*.kt`) — the two toggles. Turning one on calls
   `PushTopicManager.apply(..., true)` and, on Android 13+, requests `POST_NOTIFICATIONS` in the
   same tap (`ui/components/NotificationPermission.kt`, shared with the banner below). A
   permission hint card appears whenever a preference is on but notifications aren't actually
   allowed (denied, or the user turned notifications off for the app in system settings) and links
   to the app's notification settings.
7. **Enable-notifications banner** (`ui/components/NotificationEnableBanner.kt` +
   `NotificationEnablePromptViewModel.kt`) — a pinned card on Summary (Daily Summary) and Analysis
   (Stock Analysis) for first-run discovery, since a fresh install has both preferences off and no
   other prompt to turn them on. Shows only while its preference is off *and* unanswered;
   "answered" is sticky (enabling or dismissing both count, tracked per preference in the same
   DataStore) so it never reappears once acted on, even if the toggle is later switched back off.
8. **Tap routing** (`ui/navigation/PushNavigation.kt` + `MainActivity`) — reads `data.screen` off
   the launch intent's extras (cold start in `onCreate`, warm start in `onNewIntent`, which needs
   `MainActivity`'s `launchMode="singleTop"` to fire at all). Maps to a bottom-nav tab via the
   shared `navigateToTab` helper; unknown/missing `screen` opens Overview. A push tapped while
   still on the onboarding/legal-acceptance gate is dropped rather than queued, so it can't jump
   the user past terms acceptance.

## Backend contract

Topic names, channel ids, and the exact payload shape are documented once, in
`@docs/architecture/cross-repo-contracts.md`, since that's where every other Android↔backend field
name already lives — not duplicated here. The one rule worth restating: **routing reads
`data.screen` only; `data.type` is for logging, and `notification.title`/`.body` are never parsed
or asserted on** (the backend varies title text by weekday/asset count on its own schedule).

## Decisions made, for context

- **Both toggles default off** (opt-in), so a fresh install subscribes to nothing until the user
  acts — either from Settings or from a banner.
- **A foreground push is suppressed**, not re-shown as a local notification — the screen the user
  is already looking at is live, so re-posting the same content as a tray notification would be
  redundant.
- **No token storage.** `onNewToken` is log-only; topic subscriptions are keyed to the app
  instance via `subscribeToTopic`, not to a token this app tracks itself.

## Verifying a change here

There's no local emulator for FCM delivery — checking this system means sending a real message.

1. Debug builds subscribe to both the production and `_test` topics (`daily_summary_test`,
   `stock_analysis_test`) — the backend only publishes to the `_test` pair while it's deployed with
   a topic suffix set, so a console send there never reaches production users.
2. In the app, turn the matching toggle on (Settings → Notifications, or the banner) and grant the
   permission prompt.
3. From the Firebase console: Messaging → New campaign → Notifications → target the `_test` topic
   → under Additional options set the Android notification channel (`daily_summary` /
   `stock_updates`) and custom data (`type`, `screen`) to match the real contract → Publish. Don't
   use "Send test message" — that targets a device token, not a topic.
4. Background the app (a foregrounded app suppresses the push per the decision above) before
   sending, to see the actual tray notification.
5. Check: channel assignment, tray icon/tint, tap routing (cold start — force-stop first — and
   warm start), toggle-off actually stops delivery, and the permission-denied path (deny the
   prompt, confirm the hint card appears and its settings link works).

See `@docs/architecture/cross-repo-contracts.md` for the full verification checklist (long-body
truncation, both summary titles routing identically, Android 12 vs. 13+ permission behavior).
