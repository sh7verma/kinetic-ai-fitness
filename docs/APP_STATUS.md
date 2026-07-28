# Kinetic — App Status

_Last updated: 2026-07-19_

A snapshot of what's built, what's incomplete, and what should be improved next.

---

## ✅ Done (working end-to-end)

### AI Meal Logging (the core feature)
- Natural-language food entry via chat (`ui/aichat/`) — "2 eggs and a roti" → GPT-4o-mini returns food items, gram estimates, calories, and macros.
- Three purpose-built prompts in `AIPrompts.kt`: meal parsing, per-100g nutrition lookup, and macro strategy calculation.
- Confidence scoring per food item, with fallback nutrition values and sanity-check validation on parsed output.
- Interactive meal editing before saving (adjust grams, remove items), then persisted to Room.

### Nutrition Data Pipeline
- Multi-layer food resolution (`FoodResolver.kt`): Room cache → Firestore food database → AI lookup, with results cached back locally.
- Repositories: `DietAIRepository`, `FoodRepository`, plus dedicated local / Firebase / AI data sources.

### Onboarding & Personalization
- Multi-step onboarding flow: biometrics (age, weight, height), goal, activity level, cuisine/flavor preferences, workout commitment.
- AI-calculated daily calorie + macro targets via `initStrategyPrompt` and `MacrosCalculator.kt`.
- Profile persisted with DataStore and synced to Firestore.

### Fuel Dashboard
- Daily calorie ring, macro progress bars, and date-based meal history (`ui/fuel/`).

### Diet Plan
- AI-generated structured meal plan from the user profile, with per-meal macro breakdowns (`ui/diet/` — view + create flows).

### Auth & Profile
- Firebase Auth with Google Sign-In via Credential Manager.
- Profile screen showing user data, with logout and account deletion (removes Firestore doc + auth account + local data).

### Foundation
- Clean Architecture (UI → Domain → Data), Hilt DI, Kotlin Flow + Coroutines, Room, Compose + Material 3.
- "Kinetic Precision" design system with reusable components (`KineticProgressBar`, `KineticDataCard`, `KineticTopAppBar`, etc.) documented in `DESIGN_SYSTEM.md`.
- Portfolio-quality README with demo GIF and screenshots.

---

## 🚧 Pending (incomplete or stubbed)

| Area | Current state | What's missing |
|------|--------------|----------------|
| **Workout Plan** (`ui/plan/`) | Screen renders, but `PlanViewModel` serves a hardcoded `dummyExercises` list | Real data source — generate the push/pull/legs split from the user profile (the AI callbacks for this were removed in `4b07902`) and persist it |
| **Exercise Logging** (`ui/logexercise/`) | UI + local state only; `LogExerciseViewModel` has no repository injected | Persist logged sets/reps to Room — `ExerciseEntity`/`ExerciseDao` already exist but are unused outside `AppDatabase` |
| **Stats screen** (`ui/stats/`) | Placeholder — just renders the text "Stats Screen" | The weight log + trend chart promised in the README; progress charts overall |
| **AI Coach** (`ui/coach/`) | Placeholder — a single button that opens the meal chat | Actual coaching experience, or fold it into the chat tab |
| **Details screen** (`ui/details/`) | Scaffold stub ("Detail Screen for Item ID: …") | Real content, or removal if unused |
| **Background sync** (`data/worker/`) | Directory is empty | The WorkManager sync tasks the README architecture diagram claims |
| **Tests** | Only the generated `ExampleUnitTest` | Any real coverage (see below) |

**Note:** the README currently advertises "Workout plan — auto-generated push/pull/legs split" and "Progress tracking — weight log with trend chart" as done. Neither is; either build them or soften the README claims.

---

## 🔧 Needs improvement

### High priority
1. **API key security** — the OpenAI key ships in the APK via `BuildConfig`. Move AI calls behind a backend proxy (Cloud Functions / small server) before any real distribution. The README already flags this.
2. **Unit tests for the AI layer** — the JSON parsing, validation, and fallback logic in `FoodAIService` / `AIDietPlanResponse` / `MacrosCalculator` is the riskiest code in the app (LLM output is unpredictable) and has zero tests. This is also the highest-value testing for a portfolio project.
3. **Finish or cut the stub screens** — a portfolio reviewer tapping Stats or Coach hits a placeholder. Either implement them or hide the tabs until ready.

### Medium priority
4. **Error/offline handling** — audit the AI request paths for network failure, timeout, malformed-JSON, and rate-limit cases; make sure each surfaces a friendly retry state in the chat UI rather than a silent failure.
5. **Exercise data model wiring** — `ExerciseEntity`/`ExerciseDao` exist but nothing writes to them; connect Plan + LogExercise to Room so workout data survives restarts.
6. **Input parsing robustness** — several screens call `toDouble()` on raw text-field input (e.g. `OnboardingBiometricsScreen.kt:85,108,131`, `OnboardingWorkoutSetupScreen.kt:86`), which crashes on empty/invalid input; use `toDoubleOrNull()` with defaults.
7. **Firestore sync strategy** — define what happens on conflicts and when the user is offline at write time (currently best-effort).

### Lower priority
8. **Streaming AI responses** — token streaming in the chat would make the logging feel much faster.
9. **CI** — a GitHub Actions workflow running `assembleDebug` + lint + tests would add polish to the public repo.
10. **Release readiness** — ProGuard/R8 rules for Retrofit/Room/kotlinx.serialization, versioning, and a signed release config haven't been exercised.

---

## Suggested next steps (in order)

1. Wire `LogExerciseViewModel` and `PlanViewModel` to Room via `ExerciseDao` (kills two "pending" rows and de-dummies the Plan tab).
2. Build the Stats screen with the weight-log trend chart so the README claim is true.
3. Add unit tests for `MacrosCalculator` and the AI response parsing/validation.
4. Fix the `toDouble()` crash points in onboarding.
5. Decide the fate of Coach and Details screens.
