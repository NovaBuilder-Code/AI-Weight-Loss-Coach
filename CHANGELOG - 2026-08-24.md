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