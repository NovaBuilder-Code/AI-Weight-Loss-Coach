# Changelog — 2026-08-26 — Photo Calorie Scanner: real AI food recognition (Task 14A)

The Scan Food screen already captured/selected a photo but analyzed nothing.
It is now connected end-to-end to real AI food recognition: the existing
camera/gallery flow is untouched; the selected photo is downscaled, compressed
to JPEG, sent to the existing Cloudflare Worker backend, and the Worker runs
it through the same OpenAI key (gpt-4o-mini, image input) that already powers
Nova Chat — no key ever ships in the APK.

## What changed
- `nova-ai-backend/worker.js` — new `POST /scan-food` endpoint (existing
  Worker, same OPENAI_API_KEY secret, same Responses API):
  - Accepts `{ "image": "<base64 jpeg>", "mime": "image/jpeg" }` (≤ ~9 MB
    guard, 413 otherwise); never logs the image or the key.
  - Sends the image to `gpt-4o-mini` (image input) with strict-JSON
    instructions and returns the sanitized schema:
    `{ foods: [{ name, estimatedPortion, calories, proteinG, carbsG, fatG }],
       totalCalories, confidence: high|medium|low, disclaimer }`.
  - Model output is fenced-JSON-stripped and validated server-side:
    numbers clamped ≥ 0, missing fields defaulted, `foods` capped at 10,
    `totalCalories` falls back to the sum of the foods, empty `foods` for
    "no food in photo"; unparseable model output → 502 (never forwarded raw).
  - `/chat` endpoint untouched.
- `app/.../data/FoodScanResult.kt` (new) — `FoodItem` / `FoodScanResult` data
  classes plus `FoodScanJson`, a small dependency-free JSON parser with
  tolerant defaults so a slightly-off response degrades to safe values and
  malformed responses surface as a server error. Pure Kotlin, JVM-testable.
- `app/.../data/FoodScanService.kt` (new) — mirrors the existing
  `NovaChatService` (HttpURLConnection, same Worker host, no new
  dependencies): base64-encodes the JPEG, posts to `/scan-food`, maps
  timeout / network / server / malformed responses to `FoodScanOutcome`.
- `app/.../ui/screens/foodscan/FoodScanScreen.kt` — existing photo flow
  unchanged; new "Analyze with Nova AI" button appears once a photo is
  selected; loading state while analyzing; result card lists each detected
  food (name, portion, kcal, P/C/F macros), total kcal, a confidence badge,
  and the disclaimer — clearly an AI estimate. Empty `foods` → "No food
  detected" card; network/timeout/server failures → snackbar (same pattern
  as ChatScreen). Nothing is saved to Food Logging (separate next task).
- `res/values/strings.xml` — new `scan_food_*` strings (EN; other locales
  fall back to the default, no new locale files touched).
- `app/.../test/.../FoodScanJsonTest.kt` (new) — 12 JVM tests: full schema,
  multi-food, no-food, defaults for missing fields, total-from-sum, negative
  clamping, 10-food cap, escaped quotes/unicode, garbage rejection,
  non-object rejection, fenced-JSON rejection, raw parser maps/lists.

## Security
All AI credentials stay server-side (Worker secret `OPENAI_API_KEY`); the
APK contains no key and logs no key; uploads go over HTTPS; the Worker
rejects oversized images and never echoes the image back.

## Verification
- `assembleDebug` green; `testDebugUnitTest` green (12 new FoodScanJson tests
  + existing suites untouched).
- On SM-A715F (in-place `adb install -r`, no uninstall, no data cleared):
  Scan Food opens; camera + gallery photo flows still work; Analyze shows
  the loading state then the estimate card with total kcal and disclaimer
  (see result below — depends on the Worker being deployed with `/scan-food`).

## Unchanged by design
Camera/photo picker, navigation, chat backend, Gradle/AGP/compileSdk/
dependencies (none added or upgraded), all other screens. No Food Logging,
no automatic saving of scan results. Notifications (14A/14B) untouched.

## Deployment note
The new `/scan-food` endpoint must be deployed to the existing Worker
(`nova-ai-backend.novaaicoach-4d1.workers.dev`) with the unchanged
`OPENAI_API_KEY` secret: `npx wrangler deploy` from `nova-ai-backend/`.