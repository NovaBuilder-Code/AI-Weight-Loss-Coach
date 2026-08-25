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
