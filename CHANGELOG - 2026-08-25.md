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
(13B), chat history, Cloudflare Worker, all other screens. Water/sleep goal
storage (no data source yet) and device-side verification are future work.
