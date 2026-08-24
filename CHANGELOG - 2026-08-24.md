# Changelog — 2026-08-24 — Final Onboarding Questionnaire Slice (Task 12B3)

Onboarding extended from 7 to 10 steps: main goal, activity level and daily
step goal complete the questionnaire. Profile setup is now considered done
only when all 10 profile fields from Tasks 12A–12B3 are answered.

## New files
- `app/src/test/java/com/novaai/calorietracker/ui/screens/onboarding/ProfileSetupValidationTest.kt` —
  3 JVM unit tests for the step-goal constants (default 10,000 inside the
  valid 1,000–100,000 range, sensible boundaries).

## Modified files
- `ui/screens/onboarding/ProfileSetupScreen.kt` — extended from 7 to 10 steps:
  main goal (Lose / Maintain / Gain weight option cards), activity level
  (Sedentary / Lightly / Moderately / Very active option cards), daily step
  goal (numeric field pre-filled with sensible default 10,000, required,
  validated 1,000–100,000 with inline error otherwise). Each Continue merges
  the answer into UserProfileStore; Finish routes to Home. Back/Continue
  navigation and previously entered answers preserved (12B1/12B2 steps
  untouched).
- `ui/screens/onboarding/WelcomeScreen.kt` — CTA gating now also requires
  main goal, activity level and daily step goal.
- `app/src/main/res/values*/strings.xml` (all 9 locales) — 15 new
  profile_setup_* strings.

## Verification on SM-A715F
- Full 10-step onboarding completed on device. `user_profile.xml` verified via
  adb: main_goal=LOSE_WEIGHT, activity_level=MODERATELY_ACTIVE,
  daily_step_goal=10000 (plus all 7 existing fields intact).
- Invalid step goal (100,500) rejected: inline error shown, Finish disabled.
  Default 10,000 accepted.
- Force-close + reopen: onboarding does not repeat — CTA goes straight to
  Home, all 10 profile values persist.
- Nova Chat opens normally; saved chat history from 11A/11B still present.
- Build green; 9 JVM unit tests (6 conversion + 3 new) and 12 instrumented
  tests all pass.

## Unchanged by design
BMR/TDEE/calorie calculations (next task), Cloudflare Worker, OpenAI
integration, chat-history behavior, all other screens, package name,
launcher configuration.
---

# Task 12C — Personalized Calorie Target (BMR/TDEE) + Saved Step Goal. Commit (see git log).

## New files
- pp/src/main/java/com/novaai/calorietracker/data/CalorieCalculator.kt — pure
  Kotlin math: Mifflin–St Jeor BMR, standard activity multipliers, goal
  factors, full daily-target pipeline with safety clamps.
- pp/src/test/java/com/novaai/calorietracker/data/CalorieCalculatorTest.kt —
  13 JVM tests: BMR known values, all 4 multipliers, TDEE, goal factors,
  full-pipeline targets (lose/maintain/gain on a device-style profile),
  female/male floors, ceiling, input-grid clamp sweep, incomplete-profile null.

## Modified files
- ui/screens/home/HomeScreen.kt — calorie card goal now comes from
  CalorieCalculator + saved profile (fallback 2000 if profile incomplete).
- ui/screens/calories/CalorieTrackerScreen.kt — DAILY_GOAL constant removed;
  ring/chips use the personalized target.
- ui/screens/walking/WalkingTrackerScreen.kt — hero ring and weekly bars use
  the saved daily step goal from onboarding (fallback DEFAULT_STEP_GOAL 10,000).

## Formula / rules used
- BMR (Mifflin–St Jeor): 10·kg + 6.25·cm − 5·age + (5 male / −161 female)
- Activity multipliers: sedentary 1.2 · light 1.375 · moderate 1.55 · very 1.725
- TDEE = BMR × multiplier
- Goal factor: lose ×0.80 (safe ~20% deficit) · maintain ×1.00 · gain ×1.10 (controlled surplus)
- Target = round(TDEE × goal factor), clamped to floor 1500 (male) / 1200 (female) and ceiling 5000
- Incomplete profile → null → screens fall back to 2000 kcal (backward compatible)

## Verification on SM-A715F
- Onboarding completed: female 34y 175.26 cm 69.853 kg, moderately active,
  lose weight → Home and Calorie Tracker show "/ 1814 kcal" (was hardcoded 2000).
- Steps tracker shows "/ 10,000" from saved goal; edited saved goal to 8,000 →
  UI showed "/ 8,000" (proves it reads the profile, not a fixed default), then restored.
- Build green; 22 JVM unit tests (13 new + 9 existing) and 12 instrumented
  tests pass.

---

# Task 12D — Real-device verification & polish. Commit (see git log).

## What was verified on SM-A715F (no code changes required)
- Home shows the personalized target "/ 1814 kcal" (female, 34y, 175.26 cm,
  69.853 kg, moderately active, lose weight: BMR 1462.9 → TDEE 2267.5 → 1814).
- Calorie Tracker shows the exact same "/ 1814 kcal".
- Walking/Steps shows the saved daily step goal "/ 10,000".
- Force-close + reopen: both values persist; onboarding is not shown again
  (CTA goes straight to Home); all 10 profile fields intact.
- No crashes (0 FATAL EXCEPTION), no ANRs, no app-level log errors. Only
  benign system noise (Samsung Play Store existence check for the sideloaded
  debug package).
- Hardcoded-value sweep of Home / Calorie Tracker / Walking: no leftover
  2000/10000. The only remaining "2000" is GoalsScreen's pre-existing
  user-editable goals feature (out of scope, untouched).
- Profile survived the fresh install via Android Auto Backup (allowBackup),
  confirming no data loss on reinstall.

## Files changed
- None (verification-only task; no bugs found).
- CHANGELOG - 2026-08-24.md — this section.

## Tests
- 22 JVM unit tests green (13 calorie + 6 conversion + 3 onboarding);
  assembleDebug builds.
