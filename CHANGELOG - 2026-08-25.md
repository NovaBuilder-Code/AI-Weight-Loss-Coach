# Changelog — 2026-08-25 — Goals Screen Connected to Saved Profile (Task 13C)

The Profile → Goals screen no longer shows hardcoded demo goals. It now reads
the real saved profile and persists edits, completing the "Goal update flow"
from the MVP checklist for the profile-backed goals.

## What changed
- `ui/screens/profile/GoalsScreen.kt` — values seeded from the saved profile:
  - Calories goal = the personalized daily target from the Task 12C pipeline
    (CalorieCalculator), fallback 2000 when the profile is incomplete. This is
    derived from the profile (not user-overridable here), so the row is now
    read-only (no edit affordance) — the way to change it is via Edit Profile
    (13B) or onboarding.
  - Steps goal = saved `dailyStepGoal` (fallback 10,000), editable and
    persisted back to `UserProfileStore` (validated 1,000–100,000 as in
    onboarding).
  - Weight goal = saved `goalWeightKg` shown in the profile's units, editable
    and persisted back as canonical kg (lb→kg conversion when imperial). The
    row label is unit-aware (`Goal Weight (kg)` / `Goal Weight (lb)`) so an
    imperial profile never shows an lb value under a kg label.
  - Water / sleep keep their defaults (no saved source exists for them; out of
    scope, untouched).

## New files
- `app/src/main/java/com/novaai/calorietracker/data/ProfileGoals.kt` — pure
  helpers in the existing store/display style: personalized calorie target,
  saved step goal, goal-weight display text, weight-parse-to-kg.
- `app/src/test/java/com/novaai/calorietracker/data/ProfileGoalsTest.kt` —
  9 JVM tests: personalized calorie target (1814 for the 12D device profile)
  and 2000 fallback, saved/default step goal, imperial weight display, empty
  weight when unanswered, metric keep / imperial lb→kg conversion, garbage
  rejection.

## Modified files
- `ui/screens/profile/GoalsScreen.kt` — real seeded values + persistence;
  item list built per-profile so the weight label reflects the saved units.
- `app/src/main/res/values*/strings.xml` (EN, SV, NB — the locales that define
  `goals_weight`) — new `goals_weight_imperial` ("Goal Weight (lb)").

## Verification
- `assembleDebug` green; `testDebugUnitTest` green (9 new + existing suites).
- On SM-A715F: installed in place (`adb install -r`, no data wipe); Profile →
  Goals shows Daily Calories 1814 (read-only, no edit affordance), Daily
  Steps 10,000 (editable), Goal Weight (lb) 143 for the imperial profile.
  Edited Daily Steps to 8,000 → `user_profile.xml` updated to
  `daily_step_goal=8000` (proves persistence), then restored to 10,000. All
  other profile fields intact. App remains installed, launches cleanly, no
  crashes.

## Unchanged by design
Onboarding, calorie target, step goal, Profile screen (13A), Edit Profile
(13B), chat history, Cloudflare Worker, all other screens. Water goal storage
(no data source yet) remains future work.

---

# Sleep (hours) edit fix — new users start at 0, allow saving 0

Follow-up bug fix on the Goals screen: the Sleep field showed a hardcoded
default "8.0" and the Save button stayed disabled when the value was cleared
to 0 (the shared decimal validation required > 0).

## What changed
- `ui/screens/profile/GoalsScreen.kt`:
  - Sleep now seeds from the saved value with a 0-hour default (new users see
    "0", not "8.0").
  - Per-item validation: decimal goals gained `allowZero` + `maxValue`.
    Sleep allows 0 / 0.0, still rejects negatives, and has a sensible upper
    limit (24, exclusive). Water/weight keep their existing > 0 .. < 100000
    validation (water unchanged).
  - Saving Sleep persists to the new `SleepGoalStore` (0 included), so the
    value survives app close/reopen.
- `data/SleepGoalStore.kt` (new) — SharedPreferences store in the existing
  pattern; default 0 hours.
- `data/ProfileGoals.kt` — `MAX_SLEEP_GOAL`, `validSleepGoal` (0 ..< 24),
  `formatSleepGoal` (no trailing ".0").
- `test/.../ProfileGoalsTest.kt` — 3 new tests: 0 accepted, negatives and
  24+ rejected, formatting.

## Verification
- `assembleDebug` green; `testDebugUnitTest` green (12 ProfileGoals tests).
- On SM-A715F: Sleep row shows 0 by default; changed to 7.5 → persisted to
  `sleep_goal.xml`; changed to 0 → Save enabled, persisted as 0.0; force-close
  + relaunch → Goals still shows 0; negative value (-1) rejected (not saved).
  Calories/steps/water/weight values unchanged. App remains installed, no
  data cleared, no crashes.

---

# Task 13D — Personal Info screen connected to the saved UserProfile

The Profile → Personal Info screen showed fake/demo data ("Alex Johnson",
"alex@email.com", 29, 175, 74.2) with a non-persisting snackbar save. It now
loads, edits and saves the real saved onboarding/Edit-Profile profile through
the existing `UserProfileStore` (single source of truth).

## What changed
- `ui/screens/profile/PersonalInfoScreen.kt` — rewritten:
  - All demo values removed (including the fake email, which does not exist in
    `UserProfile`; no invented storage was added).
  - Loads the saved profile via `UserProfileStore.load` into `rememberSaveable`
    state; avatar initial reflects the real name.
  - Edits the personal fields that exist in `UserProfile`: name, age, sex
    (Male/Female `SelectOptionCard`), height, current weight.
  - Saves with `UserProfileStore.save(context, load(context).copy(...))` then
    pops back — updates flow everywhere else that reads the profile (Profile
    header, Edit Profile, Home calorie target, Goals).
  - Unit handling matches Edit Profile: metric cm/kg vs imperial ft+in/lb,
    converted to canonical metric before saving.
  - Reuses `ProfileValidation` (validName/validAge/validHeightCm/validWeightKg)
    and the onboarding range constants/error strings — no new/conflicting
    validation.
- `test/.../ProfileValidationTest.kt` — 2 new tests tying the form's imperial
  conversions to the reused validation rules (5 ft 9 in → 175.26 cm valid;
  2 ft 11 in → 88.9 cm invalid; 154 lb → 69.85 kg valid; 700 lb → 317.5 kg
  invalid).

## Verification
- `assembleDebug` green; `testDebugUnitTest` green (7 ProfileValidation tests,
  +2 new).
- On SM-A715F: Personal Info shows the real profile (Alex, 34, Female,
  5 ft 9 in, 154 lb) — no demo data. Edited Current Weight 154 → 155 lb →
  `user_profile.xml` `current_weight_kg` updated to 70.31; Profile screen shows
  155 lb; Home calorie target recalculated 1814 → 1820 kcal (proves the change
  propagates to everything that uses the profile); force-close + relaunch →
  still 155 lb. Then restored Current Weight to 154 lb (69.85 kg). All other
  fields, Goals values (steps 10,000, goal weight, sleep 0.0) unchanged. App
  remains installed (in-place upgrade), no data cleared, no crashes.

---

# Task 14A — Notifications screen connected to saved preferences

The Profile → Notifications screen already had the correct 5-toggle UI but its
state was in-memory only (reset every launch, nothing persisted). It now reads
and writes local persisted preferences. This is the saved settings layer only —
no actual scheduling/alarms/notifications/permissions yet (future task).

## What changed
- `data/NotificationPrefsStore.kt` (new) — `NotificationPrefs` data class with
  explicit new-user defaults (meals, water, steps, motivation = on; weigh-in =
  off) and a SharedPreferences store in the existing project pattern
  (`load` / `save`; every toggle change persists immediately via apply()).
- `ui/screens/profile/NotificationsScreen.kt` — loads `NotificationPrefsStore`
  on open; each `NovaToggleRow` change is saved to the store immediately
  (`load + copy + save`) and reflected in the UI. Same screen, same strings,
  same design — no redesign, no new strings.
- `test/.../NotificationPrefsTest.kt` (new) — 2 JVM tests: explicit defaults
  for a new user; toggling one field preserves the others.

## Verification
- `assembleDebug` green; `testDebugUnitTest` green (2 new NotificationPrefs
  tests + all existing suites).
- On SM-A715F (safe in-place `adb install -r`, no connected test suite run):
  Notifications shows defaults (Meals/Hydration/Steps/Motivation on,
  Weigh-In off). Toggled Meals off + Weigh-In on → `notification_prefs.xml`
  updated immediately (meals=false, weigh_in=true). Force-close + relaunch →
  the same toggles persisted. Then restored to defaults. App remains installed,
  no data cleared, no crashes.

## Unchanged by design
Onboarding, calorie logic, profile logic, Goals, AI chat backend, all other
screens. No notification scheduling, alarms, WorkManager, Firebase, push, or
permission prompts were added.

---

# Task 14B — Real Android notifications from the saved preferences

Turns the Task 14A toggles into actual Android notification scheduling via
WorkManager (no Firebase / backend). Settings layer and UI unchanged; this
adds the scheduling/plumbing and the notification permission handling.

## What changed
- `gradle/libs.versions.toml` + `app/build.gradle.kts` — added
  `androidx.work:work-runtime-ktx:2.9.1` (WorkManager).
- `AndroidManifest.xml` — added `POST_NOTIFICATIONS` permission.
- `data/ReminderSchedule.kt` (new) — pure, unit-testable scheduling decisions:
  meal windows (07–08 / 12–13 / 18–19), hydration daytime window (08–21),
  morning motivation slot (07–08), weekly weigh-in rule, once-per-day meal &
  motivation dedup, hydration 3-hour throttle.
- `data/ReminderDedupStore.kt` (new) — SharedPreferences dedup state so the
  recurring workers never post duplicates.
- `data/NovaNotifier.kt` (new) — creates the "nova_reminders" channel
  (idempotent) and posts notifications; tapping one opens the app (launcher
  intent).
- `data/ReminderWorkers.kt` (new) — 5 periodic workers:
  Meal (breakfast/lunch/dinner), Hydration (daytime, 3h throttle), Weigh-in
  (weekly), Motivation (morning, once/day). StepGoalReminderWorker is a
  scheduled-but-no-op placeholder (see "Step Goal Alerts" below).
- `data/ReminderScheduler.kt` (new) — isolated WorkManager enqueue/cancel with
  stable unique names (`nova_reminder_*`), REPLACE on enable, cancelUniqueWork
  on disable, and a `syncFromPrefs` reconcile on every app start — idempotent
  across restart/toggles, no duplicates.
- `NotificationPrefsStore.kt` — added a `permission_asked` flag (defaults
  untouched) so POST_NOTIFICATIONS is requested exactly once.
- `MainActivity.kt` — creates the channel and calls `syncFromPrefs` on start;
  also provides `LocalActivityResultRegistryOwner` (the app overrides
  `LocalContext` with a localized context, which otherwise breaks
  `rememberLauncherForActivityResult` — the cause of a crash on the
  Notifications screen).
- `ui/screens/profile/NotificationsScreen.kt` — requests POST_NOTIFICATIONS
  once on Android 13+ (not granted + not yet asked); each toggle change now
  also calls `ReminderScheduler.syncFromPrefs` so enable/disable takes effect
  immediately.
- `res/values/strings.xml` — notification channel + reminder text strings.
- `test/.../ReminderScheduleTest.kt` (new) — 6 JVM tests for the windows/rules
  above.

## Notification permission behavior
- Requested only on Android 13+ (API 33+, where the OS requires it), only when
  not granted and never asked before; the `permission_asked` flag prevents
  re-nagging. On older versions no dialog is shown. If denied, scheduling still
  runs but the OS suppresses the notifications (no crash).

## Step Goal Alerts (blocked part)
Scheduled/cancelled with the others, but intentionally a NO-OP: the app has no
reliable live/background step source yet, so step progress is NOT faked. Only
the scheduling plumbing is in place; real step-goal alerts need a background
step source (e.g. foreground SensorListener / Health Connect) as a future task.

## Verification
- `assembleDebug` green; `testDebugUnitTest` green (6 ReminderSchedule tests +
  all existing suites).
- On SM-A715F (Android 13, safe in-place `adb install -r`): app stays
  installed; POST_NOTIFICATIONS dialog shown once on first opening Notifications
  → granted (`granted=true`, `permission_asked=true` persisted, no re-nagging).
  WorkManager scheduled 4 periodic jobs with defaults on; jobscheduler shows
  4-job run batches on background. Toggled Meals OFF → batches drop to 3 (work
  cancelled); toggled back ON → batches back to 4 (rescheduled). Notification
  prefs persist after force-close/reopen; user profile/Goals/sleep data
  untouched; no crashes. Note: first run exposed a pre-existing
  `No ActivityResultRegistryOwner` crash on the Notifications screen caused by
  the localized-context override — fixed via `LocalActivityResultRegistryOwner
  provides this` in MainActivity.

## Unchanged by design
Onboarding, calorie logic, profile logic, Goals, AI chat backend, all other
screens. No AlarmManager, Firebase, push, or backend. Defaults from 14A
untouched.

---

# 14B.1 — Center Nova welcome-screen portrait only

Layout-only fix on the onboarding welcome screen: the Nova coach portrait
rendered too far to the left. The hero asset itself places Nova's portrait
left-of-centre, so the containing Box/Image (already full-width centred) could
not fix it.

## What changed
- `ui/screens/onboarding/WelcomeScreen.kt` — the hero image is now uniformly
  zoomed about its top-left corner by `0.5 / HERO_PORTRAIT_CENTER` (measured
  portrait centre ≈ 0.355 of the asset width). This puts the portrait exactly
  at screen centre while the left edge stays covered (no gap/seam) and the
  right side (including the green glow) overflows and is clipped. A vertical
  compensation keeps the top of Nova's head at its original height (no
  cropping of the face/head). Everything is a fraction of the box width, so it
  stays centred across screen widths/aspect ratios.
- No asset, text, feature chips, CTA, colours, fonts or other screens changed.

## Verification
- `assembleDebug` green; `testDebugUnitTest` green (unchanged suites).
- On SM-A715F (`adb install -r`, no uninstall, no data cleared): screenshot
  pixel analysis before/after — portrait head/face moved from ~335–406 px
  (screen centre 540) to ~490–561 px, head top exactly at screen centre, glow
  still visible, no crashes. App remains installed.

---

# 14B.2 — Smaller Nova hero portrait on Welcome screen

Follow-up layout tweak on the welcome screen: after 14B.1 centred the portrait,
it was too large. This makes it visibly smaller while keeping it centred and
showing more of the original art.

## What changed (`ui/screens/onboarding/WelcomeScreen.kt`)
- Hero zoom reduced from the 14B.1 value (0.5 / centre ≈ 1.41×) to a fixed
  `HERO_ZOOM = 1.15×` — the portrait is ~18 % smaller (within the requested
  15–25 %), so more of the original portrait is visible instead of a tight
  crop.
- Horizontal centring is now done with an explicit right shift
  (`translationX = width × (0.5 − centre × zoom)`), so the face stays exactly
  at screen centre at the smaller size. Because the smaller portrait no longer
  reaches the box's left edge, a soft navy horizontal fade covers that margin
  (matches the existing bottom fade — no hard edge).
- Head-height compensation kept (`translationY`), so the full hair/head stays
  visible at its original height; more shoulders/upper body are now visible
  (the bottom crop is much smaller).
- `HERO_IMAGE_HEIGHT` 470 → 500 dp: extra breathing room between the portrait
  fade and the NOVA AI COACH text below.
- Everything remains fractions of the box width (BoxWithConstraints), so the
  layout stays centred and responsive across screen sizes. Asset, logo,
  headline, chips, CTA, colours, fonts and other screens unchanged.

## Verification
- `assembleDebug` green; `testDebugUnitTest` green (unchanged suites).
- On SM-A715F (`adb install -r`, no uninstall, no data cleared): screenshot
  pixel analysis — head still exactly centred (539 px vs screen centre 540),
  face rows within ~485–523 px; portrait ~18 % smaller; full hair/head and
  more shoulders visible; green glow still present on the right; left margin
  blends smoothly (no hard edge); navy breathing room below the portrait before
  the logo text; content below the hero intact; no crashes. App remains
  installed.
