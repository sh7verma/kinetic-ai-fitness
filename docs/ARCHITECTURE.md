# Kinetic — App Architecture & Logic

A complete walkthrough of how Kinetic works end to end: navigation, screens, data flow, and the AI integration — as of the post-redesign codebase (see `docs/redesign/PHASES.md` for how it got here). Diagrams are Mermaid; they render natively on GitHub.

---

## 1. What the app does

Kinetic lets a user describe a meal in plain English ("2 eggs and a roti"), sends it to an LLM (`gpt-4o-mini`) which returns structured food items with gram estimates, calories, macros, a per-item **confidence score**, and a short **assumption** string (what was assumed about portion/cooking method). The user reviews, corrects, or flags each item, then saves it — the running daily total and a "quick repeat" of recent meals live on the Fuel dashboard. Daily calorie/macro targets are computed once at onboarding (and re-computed on demand from Profile) via a second AI call plus a deterministic Mifflin-St Jeor calculation.

---

## 2. Tech stack & layering

| Layer | Technology |
|---|---|
| UI | Jetpack Compose, Material 3, Navigation-Compose |
| State | ViewModel + Kotlin `StateFlow`/`SharedFlow` |
| DI | Hilt |
| Local persistence | Room (food catalog + food logs), DataStore (user profile, prefs) |
| Remote persistence | Firebase Firestore (profile backup, shared food catalog) |
| Auth | Firebase Auth via Google Sign-In (Credential Manager) |
| AI | OpenAI `gpt-4o-mini` via Retrofit, called directly from the client |
| Async | Kotlin Coroutines + Flow throughout |

```mermaid
flowchart TB
    UI["UI — Compose screens"] --> VM["ViewModel — state + intents"]
    VM --> Repo["Repository layer"]
    Repo --> Room[("Room — local DB\nfood cache + food logs")]
    Repo --> DataStore[("DataStore\nuser profile, prefs")]
    Repo --> Firestore[("Firestore\nprofile backup, food catalog")]
    Repo --> OpenAI[["OpenAI API\ngpt-4o-mini"]]
```

**Known architectural risk** (tracked in `docs/APP_UPDATE.md` as a P0 item, not yet fixed): the OpenAI API key is embedded in the Android client (`BuildConfig.OPENAI_API_KEY`) and called directly — there is no backend proxy. This is fine for local development/testing but must move server-side before any real-user distribution.

---

## 3. App entry & navigation graph

Two Activities, each hosting its own Compose `NavHost`:

```mermaid
flowchart TD
    Start(["App launch"]) --> Main["MainActivity\n(auth + onboarding shell)"]
    Main -->|"WelcomeRoute"| Welcome["WelcomeScreen"]
    Welcome -->|"no profile yet"| Onboarding["OnboardingRoute\nOnboardingScreen"]
    Welcome -->|"profile exists"| Landing
    Onboarding -->|"onFinish()"| Landing["LandingActivity\n(main app shell)"]

    Landing --> Tabs{"Bottom nav"}
    Tabs -->|"Log"| AIChat["AIChat route\nAIChatScreen"]
    Tabs -->|"Fuel"| Fuel["Fuel route\nFuelScreen (start destination)"]
    Tabs -->|"Profile"| Profile["Profile route\nProfileScreen"]
    Fuel -->|"onAIChatClick"| AIChat
```

- `MainActivity`'s `NavHost` starts at `Routes.WelcomeRoute`. It has exactly two destinations: Welcome and Onboarding.
- `LandingActivity`'s `NavHost` starts at `LandingRoutes.Fuel` and has exactly three destinations — Fuel, Profile, AIChat — matching the three permanent bottom-nav tabs (Log/Fuel/Profile). There is no back button anywhere in this shell; "Log" is a peer tab, not a pushed screen, and Android's default hardware-back handling is all that's wired.
- Handing off between the two Activities is a plain `Intent` + `startActivity`, not a shared nav graph.

---

## 4. Auth & session restore (`WelcomeViewModel`)

Runs unconditionally every time `WelcomeScreen` is composed — it's not tied to a button press:

```mermaid
flowchart TD
    Init(["ViewModel init"]) --> Local["checkLocalSession():\ncollect userProfileRepository\n.getUserProfileData()"]
    Local -->|"profile != null"| SuccessTrue["AuthState.Success(hasProfile=true)"]
    Local -->|"profile == null"| Firebase["checkFirebaseUser():\nauthRepository.getCurrentUser()"]
    Firebase -->|"no current user"| Idle["stays Idle —\nwaits for sign-in tap"]
    Firebase -->|"user exists"| FetchRemote["fetchUserProfileFromFirestore(uid)"]
    FetchRemote -->|"found"| SaveLocal["saveUserProfileData()\n(cache locally)"] --> SuccessTrue
    FetchRemote -->|"not found"| SuccessFalse["AuthState.Success(hasProfile=false)"]

    Tap(["User taps\nContinue with Google"]) --> SignIn["signInWithGoogle(activity)"]
    SignIn -->|"Success, isAnonymous"| SuccessFalse
    SignIn -->|"Success, real user"| FetchRemote2["fetchUserProfileFromFirestore(uid)"] --> Branch{"profile found?"}
    Branch -->|"yes"| SuccessTrue
    Branch -->|"no"| SuccessFalse
    SignIn -->|"Error"| ErrState["AuthState.Error →\nsnackbar, then Idle"]
    SignIn -->|"Cancelled"| Idle

    SuccessTrue -.->|"WelcomeScreen observes"| ToLanding(["→ LandingActivity"])
    SuccessFalse -.->|"WelcomeScreen observes"| ToOnboarding(["→ OnboardingRoute"])
```

A second, unrelated background job also kicks off on init: `syncFoodsIfNeeded()` refreshes the local food cache from Firestore if it's been more than 24h since the last sync. It's fire-and-forget and has no UI.

---

## 5. Onboarding (`OnboardingViewModel`, 3 steps)

```mermaid
flowchart LR
    B["Step 1 — Biometrics\nage, weight, height, sex"] -->|"nextStep()"| G["Step 2 — Goals\ngoal (Lose/Maintain/Gain)\nactivity level"]
    G -->|"nextStep()"| R["Step 3 — Results"]
    R -->|"Start logging"| Finish(["onFinish() → LandingActivity"])
```

`nextStep()` autosaves partial profile data on every step transition, but the **AI target calculation only fires once, on the Results screen itself**:

```mermaid
sequenceDiagram
    participant UI as Results screen
    participant VM as OnboardingViewModel
    participant Repo as UserProfileRepository
    participant AI as FoodAIService (OpenAI)
    participant Calc as MacrosCalculator

    UI->>VM: LaunchedEffect(Unit) — targetCaloriesData == null?
    VM->>VM: saveUserProfileData() — isCompleted=true (already on last step)
    VM->>Repo: getInitialTargetCalories(profile)
    Repo->>AI: initStrategyPrompt() + age/weight/height/activity/goal
    AI-->>Repo: NutritionStrategy (protein_per_kg, fat_ratio, calorie_adjustment, reasoning)
    Repo-->>VM: (TargetCaloriesData, NutritionStrategy) or null on failure
    alt AI call succeeded
        VM->>Calc: (not called directly — FoodAIService already ran MacrosCalculator.calculate internally)
    else AI call failed
        VM->>Calc: MacrosCalculator.fallback(profile)
    end
    VM->>Repo: saveUserProfileData(final) + saveUserProfileToFirestore(final)
    Repo-->>UI: reactive Flow emits updated profile → targets render
```

**Why the calculation is anchored to the Results screen, not the Goals screen's "Continue" button**: `viewModelScope.launch` runs on `Dispatchers.Main.immediate`, so `saveUserProfileData()`'s synchronous read of `_uiState.value.currentStep` happens *before* `nextStep()`'s later line advances the step. Calling the recalculation mid-transition would read the *old* step and never satisfy `isCompleted`. Anchoring it to `LaunchedEffect` on the screen that's already resting on the final step sidesteps the race entirely.

**What actually drives the numbers** (`MacrosCalculator.calculate`):
- **BMR** — Mifflin-St Jeor formula, branches on `sex`
- **TDEE** — BMR × activity multiplier (`SEDENTARY` 1.2 → `VERY ACTIVE` 1.9)
- **Target calories** — TDEE × `(1 + calorieAdjustment)`, where `calorieAdjustment`/`proteinPerKg`/`fatRatio` come from the AI's `NutritionStrategy` (clamped to sane ranges — the calculator never trusts raw AI output)
- **Protein** — `weight × proteinPerKg`; **Fat** — `% of target calories`; **Carbs** — remainder

Onboarding intentionally does **not** collect diet type, allergies, cuisine preference, or workout commitment days/equipment — confirmed unused by this calculation, cut in the redesign (`docs/redesign/PHASES.md`, Phase 4).

---

## 6. Meal logging — the core loop (`AIChatViewModel` + `LogFoodComponent`)

This is the screen the whole redesign is built around: every AI estimate must show what was assumed, how confident the model is, and let the user fix it in the fewest taps.

```mermaid
sequenceDiagram
    participant U as User
    participant Screen as AIChatScreen
    participant VM as AIChatViewModel
    participant Repo as DietAIRepository
    participant AI as FoodAIService (OpenAI)
    participant Resolver as FoodResolver
    participant Room as Room (FoodDao/FoodLogDao)

    U->>Screen: types "2 eggs and a roti", taps send
    Screen->>VM: sendMessage()
    VM->>VM: append user ChatMessage, isTyping=true
    VM->>Repo: logFood(message)
    Repo->>AI: logMealSystemPrompt() + user profile + message
    AI-->>Repo: AILogResponse { entries[{ mealType, items[{food, grams, confidence, assumed}] }] }
    Repo-->>VM: AILogResponse
    VM->>VM: validateFoodLog() — non-empty, every item has a food name
    VM->>Resolver: data.toUILog(foodResolver)
    Resolver->>Room: resolve each food name → cached FoodEntity\n(local Room → Firestore → AI nutrition lookup, in that order)
    Resolver-->>VM: UILog { meals[{ items[UIFoodItem{grams, calories, macros, confidence, assumed}] }] }
    VM-->>Screen: ChatMessage(aiLogs = UILog) appended, isTyping=false
    Screen->>Screen: renders one FoodItemCard per item
```

Per-item confidence comes back as a raw `0.0–1.0` score from the prompt's own bands (0.9–1.0 exact, 0.7–0.9 minor estimation, 0.4–0.7 moderate guess, <0.4 unclear) and is collapsed into the UI's 3-tier system by `ConfidenceTier.fromScore()`:

```mermaid
flowchart LR
    Score["AI confidence score\n0.0 – 1.0"] --> Check{"score"}
    Check -->|">= 0.7"| High["High\n(green pill)"]
    Check -->|">= 0.4"| Medium["Medium\n(amber pill —\n'Worth a glance')"]
    Check -->|"< 0.4"| Low["Low\n(red-orange pill —\n'Needs review')"]
```

**Per-card interaction** (`FoodItemCard` in `LogFoodComponent.kt`):

```mermaid
flowchart TD
    Card["Food item card:\nname, grams, confidence pill,\nkcal + macros, 'Assumed: ...' line"] --> Adjust{"Adjust grams\ntapped?"}
    Adjust -->|"yes"| Stepper["Inline -/+ stepper expands\nin the same card"]
    Stepper --> Recalc["grams change → recompute\ncalories/protein/carbs/fats\nfrom a per-gram density,\nlive, no network call"]
    Recalc --> Card
    Card --> Flag{"Flag as wrong\ntapped?"}
    Flag -->|"yes"| Flagged["Local-only acknowledgment\n('Flagged — thanks')\n— no backend correction\npipeline exists yet"]
    Card --> SaveTap{"Save to today's log\ntapped (once per meal)"}
    SaveTap --> SaveMeal["viewModel.saveMeal(original, edited)"]
    SaveMeal --> Insert["Insert one FoodLogEntity\nper item into Room"]
    Insert --> Saved["Card switches to\nread-only 'Saved' state"]
```

Confidence and the assumption string are **not** persisted to Room — `FoodLogEntity` only stores `foodId`/`grams`/`mealType`/`timestamp`. They exist only for the duration of the in-memory `UIFoodItem`/chat session. This is a known, documented gap (`docs/redesign/PHASES.md`, Phase 5) — meal-history rows on Fuel can't show a confidence indicator without a schema change.

---

## 7. Fuel dashboard — aggregation, history, quick-repeat (`FuelViewModel`)

```mermaid
flowchart TD
    Profile[("userProfileRepository\n.getUserProfileData()")] --> Combine
    Daily[("foodLogDao.getFoodLogsWithFoodInRange\n(startOfDay, endOfDay)")] --> Combine
    Weekly[("foodLogDao.getFoodLogsWithFoodInRange\n(startOfWeek, endOfDay)")] --> Combine
    Combine["combine() —\nrecomputes on ANY change\nto any of the 3 flows"] --> Totals["Sum calories/protein/carbs/fats\nfor today"]
    Combine --> Trend["Bucket weekly logs\nby day-of-week → 7-bar trend"]
    Combine --> Group["groupIntoMeals():\nbucket food-log rows by\n(mealType, timestamp ± 1 min)"]
    Group --> Today["todaysMeals —\nsorted chronologically"]
    Group --> Quick["quickRepeats —\nlast 7 days, most-recent-first,\ndeduped by name, top 2"]
```

There is no "meal" table in the schema — Room only stores individual food-item rows. `groupIntoMeals()` reconstructs the meal boundary by assuming everything a single `saveMeal()` call inserted lands within the same minute (true, since it's one fast synchronous loop, not a slow multi-step process).

**Quick-repeat** deliberately bypasses the AI entirely:

```mermaid
sequenceDiagram
    participant U as User
    participant Screen as FuelScreen
    participant VM as FuelViewModel
    participant Room as FoodLogDao

    U->>Screen: taps a "Quick repeat" chip
    Screen->>VM: repeatMeal(meal)
    loop for each item in meal.items
        VM->>Room: insert(FoodLogEntity(same foodId/grams/mealType,\ntimestamp = now))
    end
    Room-->>VM: (reactive Flow re-emits)
    VM-->>Screen: state updates — Eaten total, history list refresh
    Note over VM,Room: No OpenAI call anywhere in this path.
```

---

## 8. Profile — read + edit targets (`ProfileViewModel`)

```mermaid
flowchart TD
    View["Profile (view mode)\nDaily Targets card + Biometrics card"] -->|"tap 'Edit targets'"| Edit["EditTargetsForm\n(prefilled: age, weight, height,\nsex, goal, activity)"]
    Edit -->|"tap Save"| SaveCall["viewModel.saveEditedTargets(updated)"]
    SaveCall --> Recalc["Same pipeline as onboarding:\ngetInitialTargetCalories() →\nfallback to MacrosCalculator.fallback()\non AI failure"]
    Recalc --> Persist["saveUserProfileData() +\nsaveUserProfileToFirestore()"]
    Persist -.->|"reactive Flow"| View
    Edit -->|"tap Cancel"| View
```

`EditTargetsForm` doesn't duplicate onboarding's input UI — it directly imports and reuses `NumberField`/`SexOption` (from `OnboardingBiometricsScreen.kt`) and `GOALS`/`ActivityOption`/`SegmentOption`/`ActivityOptionRow` (from `OnboardingGoalsScreen.kt`), which had their `private` visibility dropped specifically for this reuse.

---

## 9. AI prompts in play

| Prompt (`AIPrompts.kt`) | Called from | Returns | Consumed by |
|---|---|---|---|
| `logMealSystemPrompt()` | `FoodAIService.logFood()` | `AILogResponse` — meal(s) of food items, each with `grams`, `confidence`, `assumed` | `AIChatViewModel.sendMessage()` |
| `getNutritionPer100g(items)` | `FoodAIService.getNutritionPer100g()` | Per-100g calories/protein/carbs/fats for foods not in the local/Firestore cache | `FoodResolver` (as the last-resort tier of its 3-tier lookup) |
| `initStrategyPrompt()` | `FoodAIService.getInitialTargetCalories()` | `NutritionStrategy` — protein/kg, fat ratio, calorie adjustment, reasoning (never raw target numbers — those are computed deterministically) | `OnboardingViewModel`/`ProfileViewModel` via `MacrosCalculator.calculate()` |

`FoodResolver`'s 3-tier lookup for any food name:

```mermaid
flowchart LR
    Name["food name from AI"] --> L1{"in local Room\ncache?"}
    L1 -->|"yes"| Use["use it"]
    L1 -->|"no"| L2{"in Firestore\nshared catalog?"}
    L2 -->|"yes"| Cache1["cache locally"] --> Use
    L2 -->|"no"| L3["AI nutrition lookup\n(getNutritionPer100g)"]
    L3 --> Cache2["cache locally + Firestore"] --> Use
```

---

## 10. Data model summary

| Store | Holds | Notes |
|---|---|---|
| Room — `FoodEntity` | Cached nutrition-per-100g for known foods | Populated by `FoodResolver`'s 3-tier lookup |
| Room — `FoodLogEntity` | Individual logged food items (foodId, grams, mealType, timestamp) | No per-meal grouping column, no confidence/assumption columns — see §6/§7 gaps |
| DataStore | `UserProfileData` (biometrics, goal, activity, targets, `isCompleted`), last-food-sync timestamp | Source of truth for the profile locally; mirrored to Firestore |
| Firestore | Profile backup (`saveUserProfileToFirestore`/`fetchUserProfileFromFirestore`), shared food catalog | Backup/sync layer, not the primary read path |

---

## Where to look next

- `docs/redesign/PHASES.md` — how each screen got to its current shape, phase by phase, with what was verified live
- `docs/redesign/kinetic-redesign-v1.html` — the visual source of truth (open via a local HTTP server, not `file://`)
- `docs/APP_UPDATE.md` — what's still not production-ready (API key on-device being the big one) and the prioritized fix list
