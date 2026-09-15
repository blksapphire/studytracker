# StudyTracker

Android app (Kotlin + Jetpack Compose) that tracks study/focus time by
detecting when the phone isn't being used for social media or other
distracting apps.

## Architecture

```
presentation/  Compose screens + ViewModels (MVVM)
domain/        Models, repository interfaces, use cases — no Android deps
data/          Room, DataStore, the tracking Service, repository impls
widget/        Glance home-screen widget
di/            Hilt modules
```

Clean-architecture split: `domain` has zero Android imports, so tracking
logic and business rules are testable without an emulator.

## How tracking works

1. `TrackingService` (foreground service) polls `UsageStatsManager` every
   7s for `MOVE_TO_FOREGROUND` events.
2. `AppClassifier` checks the foreground package against a user-editable
   "distracting apps" list (DataStore-backed, ships with a sane default).
3. Non-distracting foreground time accumulates into a live `SessionEntity`
   in Room, upserted on every tick (not just on session end) so a killed
   service doesn't lose data.
4. A `GRACE_WINDOW_MS` (8s) buffer means a quick check of a distracting
   app doesn't kill an in-progress session outright.
5. A separate WorkManager job rolls live sessions into `daily_stats` for
   Dashboard/History/Widget/Insights to read cheaply.

## Required manual setup (per install)

- **Usage access**: `PACKAGE_USAGE_STATS` is special-access — no runtime
  dialog. Onboarding must deep-link to
  `Settings.ACTION_USAGE_ACCESS_SETTINGS` (see `UsagePermissionHelper`).
- **Battery optimization exemption**: several OEMs (Xiaomi/MIUI, Huawei,
  some Samsung builds) kill foreground services aggressively regardless
  of the above. Prompt for this as a clearly-explained *second* step,
  not bundled into the first ask — `UsagePermissionHelper.requestIgnoreBatteryOptimizationsIntent`.

## Design system (colors, type, shapes)

Palette: playful/gamified anchored on teal.

- **Teal** (`TealPrimary`) — brand color, CTAs, progress rings, nav selection.
- **Coral** (`StreakCoral`) — streaks specifically, high-urgency "don't break the chain" moments.
- **Amber** (`XpAmber`) — badges/milestones/level-up moments — kept separate from coral so streak and achievement signals don't visually collide.
- Neutrals are warm-cool grays, not stark black/white, so the bright accents don't tip into "childish."

Typography: **Poppins** (bundled as static-weight `.ttf` files in `res/font/`) for headlines and big numbers — rounded, energetic. **Inter** (bundled as a single variable font, weight driven via `FontVariation`) for body/list text — neutral and legible at small sizes. Both are Google's fonts under the OFL license; license text is kept in `assets/font-licenses/` for attribution.

Shapes: noticeably rounder than Material defaults (see `Shape.kt`) — cards/chips/buttons read as friendlier without going fully pill-shaped.

All of this lives in `presentation/theme/` (`Color.kt`, `Type.kt`, `Shape.kt`, `Theme.kt`) — change values there and it propagates everywhere; no per-screen color code to hunt down.


**Scaffolded**: Gradle setup, manifest + permissions, Room schema
(sessions + daily_stats), the tracking service and polling loop, app
classifier, Hilt DI wiring, `DailyRollupWorker` (live sessions →
daily_stats, runs every 15 min + immediately on session end),
`CalculateStreakUseCase` and `CalculateAdaptiveGoalUseCase` (pure,
unit-testable domain logic), Glance widget stub, and now **all seven
screens wired into a working nav graph with bottom nav + top bar**:

- **Onboarding** — two-step permission ask (usage access, then battery
  exemption), each with its own explanation.
- **Dashboard** — today's time, progress ring toward the adaptive goal,
  streak stats, live-session indicator.
- **Session** — real-time elapsed time while a session is live, subject
  tagging via `FilterChip`s.
- **History** — per-day list from `daily_stats`.
- **App Library** — lists all non-system installed apps, toggle switch
  per app to mark it distracting (backed by `AppClassifier`/DataStore).
- **Insights** — weekly totals, average, best day, most-studied tag.
- **Goals** — adaptive goal display + exam countdown (DataStore-backed;
  date picker is currently a "+14 days" placeholder button).
- **Settings** — permission status/fix actions, battery exemption,
  nudges toggle (not yet wired to a real notification worker), data
  export (not yet built).

Everything above uses Material3 defaults — this is the wiring/structure
pass, not the visual design pass. Once colors/typography are locked in,
none of this logic changes, only `presentation/theme/Theme.kt` and
whatever custom composables replace the plain `ElevatedCard`/`ListItem`
defaults.

**Not yet built**:
1. Lock-screen nudge notifications (needs a small worker/receiver that
   watches for "distracting app opened within N minutes of a session
   ending" — the toggle exists in Settings but isn't wired up).
2. Real date picker for exam date (currently a placeholder button).
3. Peak-focus-hours insight — needs hourly-bucketed data the current
   schema doesn't retain; cheap to add to the rollup worker later.
4. Real color palette, typography, and motion.


## Not yet decided

- Whether "Focus Mode" app-blocking makes it into v1 or ships later —
  it needs either `AccessibilityService` (heavier Play Store scrutiny)
  or a much simpler "just notify, don't block" version first.
