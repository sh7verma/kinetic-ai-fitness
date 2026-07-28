# Kinetic Redesign — Phases & Changes

Tracks execution of the redesign plan (`~/.claude/plans/purring-enchanting-umbrella.md`, approved 2026-07-26/27). Source design: `docs/redesign/kinetic-redesign-v1.html`, brief at `docs/REDESIGN_PROMPT.md`. Strategic context: `docs/APP_UPDATE.md`, `docs/research/`.

Scope: narrow Kinetic to 5 screens — **Auth/Welcome, Onboarding, Meal Logging, Fuel Dashboard, Profile** — and move the visual language from the old "Kinetic Precision" HUD look (obsidian/lime/cyan) to a calm, clinical, trust-first palette.

---

## Phase 0 — Remove legacy surfaces and dead code ✅ done (2026-07-27)

**Screens deleted outright** (zero cross-references outside their own nav wiring): `ui/plan/`, `ui/logexercise/`, `ui/coach/`, `ui/stats/`, `ui/details/`, `ui/diet/`.

**Nav graph rebuilt**:
- `LandingActivity.kt` — `LandingRoutes` trimmed to `Fuel`/`Profile`/`AIChat`. Bottom nav is now permanently Log/Fuel/Profile (Log = AIChat as a real tab, not a pushed screen). `startDestination` collapsed to `Fuel` unconditionally (was branching on `BuildConfig.IS_DEBUG` to `Plan`). Bottom nav chrome (`KineticBottomNavigation`/`KineticNavigationItem`) re-themed off `KineticTheme.colors`/`typography` instead of hardcoded hex/`SpaceGroteskFamily`.
- `MainActivity.kt` — `Routes.DetailRoute` and its composable removed.

**Data layer**:
- Deleted `ExerciseEntity`/`ExerciseDao` and `DietPlanEntity`/`DietPlanDao` (Room bumped v9→v10, both already dead — no injection sites).
- Removed diet-plan-generation backend: `DietAIRepository.createDietPlan()`, `FoodAIService.getCreateDietPlan()`, `OpenAIService.getCreateDietPlan()`, `AIPrompts.mealSystemPrompt2()`, the `AIDietPlanResponse`/`AIDietPlan`/`AIMeal` types. Kept `AIFoodItem`/`toGrams()`/`safeGrams()` in `AIDietPlanResponse.kt` — still used by the meal-logging path.
- Follow-on compile-driven fixes not in the original deletion list (deleting `AIDietPlanResponse` cascaded further than expected): removed `AIResponse.DietPlanResult` variant + its `"diet_plan"` parse branch, `DataStoreHelper.saveDietPlan()`/`dietPlan` flow + `CURRENT_DIET_PLAN_KEY`, and the dead `AIDietPlan.toFoodLogs()` extension in `FoodLogEntity.kt`.

**Dead components removed** from `ui/components/KineticComponents.kt`: `TonalActionButton` (zero callers), `KineticExerciseHeader`, `KineticMetricCard`, `KineticUpNextCard` (only used by the deleted `LogExerciseScreen`).

**Left alone deliberately**: `BarlowCondensedFamily`, `KineticPrimaryContainer`, `KineticSecondary`, `Meal*` color constants, `AIResponse`'s `"workout"` branch / `WorkoutPlanResponse` — still referenced by screens pending their own rebuild phase, or not confidently traced as dead.

**Verified**: `./gradlew :app:compileDebugKotlin` clean. Booted Pixel_8_API_35 emulator, installed the APK, launched, tapped all 3 tabs (Log/Fuel/Profile) — no crashes, light palette already active everywhere via existing `KineticTheme.colors` usage.

---

## Phase 1 — Rebuild Welcome/Auth screen ✅ done (2026-07-27)

Rewrote `ui/welcome/WelcomeScreen.kt` entirely: dropped the hero background image, neon-glow "KINETIC VOLT" wordmark, italic Lexend type, and glassmorphism action-hub panel. New layout: indigo "K" mark, "Kinetic" title, one-line tagline ("Log your meals in plain English. Trust the numbers."), a plain "Continue with Google" button, and the "Estimates, not medical advice." disclaimer footer — matching `kinetic-redesign-v1.html`'s Auth/Welcome screen. Everything now sourced from `KineticTheme.colors`/`typography`/`KineticSpacing`/`KineticShape`; zero hardcoded hex or legacy font families remain in this file.

`ui/welcome/WelcomeViewModel.kt` was **not touched** — its auth-state-driven navigation and the unconditional `syncFoodsIfNeeded()` background food-catalog sync are preserved verbatim, as planned.

**Verified**: compiled clean, rebuilt APK, cleared app data on-emulator to force the logged-out path, launched, screenshotted — matches the mockup, no crashes in logcat.

---

## Phase 2 — AI model/prompt groundwork for confidence + assumptions ✅ done (2026-07-27)

**Why**: the meal-logging screen (Phase 3) can't show per-item confidence or "assumed: ..." text because the data doesn't exist yet. Today:
- `AILogResponse.confidence` is response-level only (one number for the whole meal, not per item).
- `AIFoodItem` (`food`, `grams`) has no confidence or assumption/reasoning field at all.
- `AIPrompts.logMealSystemPrompt()` never asks the model for either per item.
- `toUILog()` (`FoodLogEntity.kt`) drops the response-level confidence entirely when building `UIFoodItem`/`UIMeal`.

**What changed**:
1. `AIPrompts.logMealSystemPrompt()` now requires per-item `confidence` (0.0–1.0, same bands as before: 0.9–1.0 exact, 0.7–0.9 minor estimation, 0.4–0.7 moderate guess, <0.4 unclear) and a short `assumed` string per item (e.g. "2 medium rotis (~40g each), no ghee"), with explicit instructions to describe serving size/cooking method/hidden ingredients in under 12 words and never leave it empty.
2. `AIFoodItem` (`AIDietPlanResponse.kt`) gained `confidence: Double = 0.0` and `assumed: String? = null`.
3. `UIFoodItem` (`AIChatViewModel.kt`) gained the same two fields; `toUILog()` (`FoodLogEntity.kt`) now threads `item.confidence`/`item.assumed` through instead of dropping them.
4. `ConfidenceTier` (`ui/theme/ConfidenceTier.kt`) gained a `companion object` with `fromScore(score: Double)`, collapsing the prompt's 4 numeric bands into the UI's 3-tier system: `>=0.7` → High (covers both "exact" and "minor estimation" — both read as trustworthy), `>=0.4` → Medium, else Low.
5. Left `AILogResponse.confidence` (meal-level) in place — harmless, not required to remove for the UI work ahead.

**Verification**: `./gradlew :app:compileDebugKotlin` clean; confirmed `EditMealBottomSheet`'s existing `.copy(...)` calls on `UIFoodItem` are all named-argument, so the new fields pass through safely without code changes there. This phase has no user-visible surface (pure data plumbing) — full end-to-end verification (does the live OpenAI call actually return conforming `confidence`/`assumed` per item) happens as part of Phase 3's verification, once there's a screen to display it and a real meal-log message can be sent through the app.

**Not in scope for this phase**: any UI changes (that's Phase 3), and no backend proxy migration (tracked separately as a P0 security item in `docs/APP_UPDATE.md`, orthogonal to this redesign).

---

## Phase 3 — Meal Logging screen (the core screen) ✅ done (2026-07-27)

**What changed**:
- `ui/aichat/components/LogFoodComponent.kt` rewritten entirely. Each food item is now its own card (`FoodItemCard`), not a row inside a meal-level card: name + grams subtitle, a `ConfidencePill` (dot + label — "High confidence" / "Worth a glance" / "Needs review" — built from `ConfidenceTier.fromScore()` + `.colors()`), a bold kcal number with inline P/C/F macro text, an "Assumed: ..." line (info icon + `item.assumed`), an in-place `GramsStepper` (-/+ buttons, live-recalculated kcal, replaces the old `EditMealBottomSheet` modal entirely — no more leaving the screen to correct a value), and two distinct actions: "Adjust grams" (brand-colored, toggles the stepper) and "Flag as wrong" (danger-colored via `colors.confidenceLowText`, local-only acknowledgment — no backend correction-persistence endpoint exists yet, that's a separate future initiative per `docs/APP_UPDATE.md`). One "Save to today's log" button per meal, replacing the old per-meal "LOG" button + modal-save flow. Old `MealCard`/`FoodItemRow`/`MacroRow`/`MacroItem` and the standalone `ui/components/MealComponents.kt` (`EditMealBottomSheet`, `InlineMacro`) were deleted — fully superseded, zero remaining callers.
- `ui/aichat/AIChatScreen.kt` rewritten: dropped the back arrow (Log is now a permanent bottom-nav tab, not a pushed detail screen — matches Fuel/Profile having none either; hardware back still works via Compose Navigation's default handling), dropped the vestigial `chatType` filter-chip row (confirmed dead — `sendMessage()` never read `state.chatType`), added the mockup's empty/first-use state (icon + "Describe what you ate" + two tappable example chips that fill the input field), and restyled the header/bubbles/input off `KineticTheme` tokens throughout.
- `AIChatViewModel.kt`: removed the auto-injected "Hello! I'm your Kinetic Nutrition coach..." greeting message on init — the new empty-state screen fills that role instead.
- `LandingActivity.kt`: dropped `onBackClick` from the `AIChatScreen()` call site to match the new signature.

**Verified live, end-to-end, with a real OpenAI call** (not just compiled): booted the emulator, opened the Log tab (empty state matched the mockup), tapped an example chip (filled the input correctly), sent "2 eggs and a roti" through the actual `logFood()` API call. The response came back with per-item confidence and assumption text exactly as Phase 2's prompt change requested — both items landed "High confidence" with `"Assumed: 2 large eggs (~50g each)"` and `"Assumed: 1 medium roti (~40g)"`. Tested the inline stepper (100g→120g, kcal/macros recalculated live: 150→180 kcal, P12→14g etc.), "Flag as wrong" (button text changed to "Flagged — thanks"), and "Save to today's log" (saved successfully, card switched to a "Saved to today's log" read-only state). Cross-checked on the Fuel screen: "Eaten: 264" appeared immediately (180+84 kcal, exactly matching what was saved), confirming the data pipeline into Room is intact. Zero crashes in logcat throughout.

## Phase 4 — Onboarding trimmed to 3 steps ✅ done (2026-07-28)

**What changed**:
- `OnboardingStep` collapsed from 5 entries to 3: `BIOMETRICS(1)`, `GOALS(2)`, `RESULTS(3)` (was BIOMETRICS/WORKOUT_SETUP/MEAL_SETUP/ACTIVITY_SETUP/FLAVOR_PROTOCOL). Dropped the dramatic HUD copy ("ESTABLISH BASELINE", "FUEL PROTOCOL", etc.) for plain titles matching the mockup.
- `OnboardingUiState`/`OnboardingViewModel`: removed `commitmentDays`, `equipment`, `selectedDietTypes`, `selectedAllergies`, `selectedCuisines` — confirmed unused by `MacrosCalculator.calculate()`/`FoodAIService.getInitialTargetCalories()` back in the exploration phase. Removed `previousStep()`/`skipOnboarding()` and the diet/cuisine/equipment mutator methods (no longer called by anything). `saveUserProfileData()` now falls back to `MacrosCalculator.fallback(profile)` if the AI call fails, instead of silently leaving `targetCaloriesData` null — the Results screen would otherwise have nothing to show on failure.
- Deleted `OnboardingMealSetupScreen.kt`, `OnboardingFlavorProtocolScreen.kt`, `OnboardingActivityCuisineScreen.kt`, `OnboardingWorkoutSetupScreen.kt` outright (confirmed via grep: nothing outside the onboarding feature referenced their composables).
- New `OnboardingGoalsScreen.kt` replaces the old Workout Setup + Activity/Cuisine screens: a single screen combining Goal (Lose/Maintain/Gain — simplified from the old "FAT LOSS"/"MUSCLE_GAIN"/"PERFORMANCE" free-text values, still sent as free text to the AI strategy prompt so no backend change needed) and Activity Level (5 options — `SEDENTARY`/`LIGHT`/`MODERATE`/`ACTIVE`/`VERY ACTIVE` — chosen to exactly match `MacrosCalculator`'s activity-multiplier lookup keys; the old screen's `"PRO-ATHLETE"` key was a pre-existing bug that silently fell through to the `else` multiplier, now fixed as a side effect).
- New `OnboardingResultsScreen.kt` (didn't exist before — the old flow exited straight to `LandingActivity` from the last data-entry step without ever showing calculated targets). Shows a loading state while the AI call is in flight, then a checkmark + "Your daily targets" + kcal/day hero number + Protein/Carbs/Fat stats + "Start logging" button, matching the mockup.
- Rewrote `OnboardingBiometricsScreen.kt`: plain labeled number fields (Age, Weight (kg), Height (cm)) with safe `toDoubleOrNull()` parsing — fixes the crash-on-empty-input risk flagged in `docs/APP_STATUS.md` — plus a Male/Female toggle (kept; required by `MacrosCalculator`'s BMR formula branch, even though not visible in the mockup's captured screenshot). Dropped the KG/LBS and CM/FT unit toggles entirely to match the mockup's simplicity.
- Rewrote `OnboardingComponents.kt` down to two composables: `OnboardingHeader` (step counter + title + progress bar) and `OnboardingContinueButton` (single full-width button) — replacing `OnboardingTopBar`/`OnboardingStepHeader`/`OnboardingBottomNavigation`'s back+skip+continue three-button chrome, none of which appeared in the mockup.
- Rewrote `OnboardingScreen.kt`'s step dispatch for the new 3-step `when`.
- **Sequencing fix carried over from the old code's working pattern**: the Results screen triggers the AI calculation itself via `LaunchedEffect(Unit) { if (targetCaloriesData == null) viewModel.saveUserProfileData() }`, rather than the Goals screen's "Continue" calling it — `nextStep()`'s `saveUserProfileData()` call reads `_uiState.value.currentStep` synchronously (before the subsequent step-advance line runs, since `viewModelScope.launch` on `Dispatchers.Main.immediate` executes synchronously up to its first suspension point), so calling it mid-transition would read the *old* step and never trigger `isCompleted`. Confirmed this matches how the original 5-step flow worked (the old `FlavorProtocolScreen` called `saveUserProfileData()` directly while already resting on the last step, never through `nextStep()`).

**Verified live** (real device, real AI calls, not just compiled): since triggering onboarding through an actual Google Sign-In isn't practical in this environment, temporarily pointed `MainActivity`'s `NavHost` start destination at `Routes.OnboardingRoute` for testing, then reverted immediately after (confirmed via `git status`/grep — no leftover trace). Walked through all 3 steps fresh (cleared app data first): Biometrics (confirmed empty/invalid age input no longer crashes — self-heals instead), Goals (goal segments + all 5 activity levels selectable, progress bar advances correctly), Results (real `getInitialTargetCalories` AI call — first with Maintain/Active → 2358 kcal, then with Lose/Lightly-active → 2022 kcal, confirming the calculation genuinely responds to the new inputs, not cached). "Start logging" correctly navigated to `LandingActivity`, and Fuel immediately reflected the new 2,023 kcal target with a freshly-generated AI summary referencing "fat loss" and "light activity level." Also confirmed the returning-user restore path still works (a previously-completed profile jumps straight to the Results step showing its saved targets). Fixed one visual bug found during testing: the new header had no status-bar inset (`.statusBarsPadding()` was on the deleted `OnboardingTopBar`, not carried to the new `OnboardingHeader`/Results screen) — added to both. Zero crashes throughout.

## Phase 5 — Fuel Dashboard meal history + quick-repeat ✅ done (2026-07-28)

**What changed**:
- `FuelViewModel.kt`: replaced the dead `items: List<FuelItem>`/`loggedMealNames` fields (computed but never rendered) with a proper grouping layer. New `groupIntoMeals()` reconstructs "meals" from the individual `FoodLogEntity` rows Room actually stores — there's no meal-grouping column in the schema, so items are bucketed by `(mealType, timestamp rounded to the nearest minute)`, since a single `saveMeal()` call inserts all of a meal's items in one fast synchronous loop. Produces `LoggedMealGroup(displayName, mealType, time, timestamp, totalCalories, items)` where `displayName` is a comma-joined food-name list (e.g. "roti, paneer tikka").
- `FuelState` gained `todaysMeals: List<LoggedMealGroup>` (grouped from today's logs, sorted chronologically oldest-first — matches the mockup's 8:15 AM → 1:05 PM → 7:40 PM ordering) and `quickRepeats: List<LoggedMealGroup>` (grouped from the last 7 days, sorted most-recent-first, deduplicated by `displayName`, capped at 2).
- New `FuelViewModel.repeatMeal(meal)`: loops `meal.items`, inserts a fresh `FoodLogEntity` per item with `System.currentTimeMillis()` — re-logs the exact same foods/grams **without calling the AI**, per the plan.
- `FuelScreen.kt`: added a "Quick repeat" horizontally-scrolling chip row (`QuickRepeatChip` — repeat icon + meal name, calls `viewModel.repeatMeal`) and a "Today's meals" section (`TodaysMealRow` — name, time, calories) between the macro grid and the weekly trend chart, matching the mockup's relative ordering. Both sections only render `if (...isNotEmpty())`, so a fresh account with no logs sees neither (verified). Deleted the now-fully-superseded dead `FuelItemRow`/`MiniCard` composables and the `FuelItem` data class.
- Known simplification, called out explicitly rather than silently skipped: history entries don't show a per-meal confidence dot the way the mockup's dashboard sketch implied, because confidence is never persisted to `FoodLogEntity`/Room — only the in-memory `UIFoodItem` from Phase 2/3 carries it. Persisting confidence to the DB schema was out of scope for this phase's plan; flagging it here as a candidate for later.

**Verified live** (real device, two real meals logged through the actual AI, not fixtures): confirmed a fresh account shows neither section. Logged "2 eggs and a roti" (150+84 kcal) and "paneer tikka with 2 rotis" (500+168 kcal) through the real `logFood()` flow and saved both. Fuel then showed: Eaten = 902 kcal (exact sum, confirming the grouping/aggregation math), two quick-repeat chips ("roti, paneer tikka" / "roti, egg"), and two chronologically-ordered history rows with correct per-meal calorie totals (234 / 668). Tapped a quick-repeat chip: Eaten jumped to 1136 (+234, exact), a third history row appeared ("egg, roti" at the current time), and `adb logcat` confirmed **zero** `sendMessage`/`logFood` calls fired — the repeat genuinely bypassed the AI. Zero crashes throughout.

## Phase 6 — Profile "Edit targets" ✅ done (2026-07-28)

**What changed**:
- `ProfileScreen.kt` fully rebuilt to match the mockup: a "DAILY TARGETS" card (Calories/Protein/Carbs/Fat + an "Edit targets" link) and a "BIOMETRICS" card (Age/Weight/Height/Activity level), replacing the old flat `ProfileItem`+`Divider` list. Sign out / Delete account kept, restyled to `KineticTheme` tokens.
- Editing works as an in-place mode switch (`var isEditing by remember { mutableStateOf(false) }`), not a new nav route — kept Phase 6 scoped to `ui/profile/` only rather than touching `LandingActivity`'s nav graph. Tapping "Edit targets" swaps the two cards for `EditTargetsForm`: Age/Weight/Height fields, Sex toggle, Goal segments, Activity level list — prefilled from the current profile.
- Rather than duplicate the onboarding form UI, `NumberField`/`SexOption` (`OnboardingBiometricsScreen.kt`) and `GOALS`/`ActivityOption`/`ACTIVITY_OPTIONS`/`SegmentOption`/`ActivityOptionRow` (`OnboardingGoalsScreen.kt`) had their `private` modifier dropped and are now imported directly into `ProfileScreen.kt` — same component, same behavior, no copy-pasted form code.
- `ProfileViewModel.saveEditedTargets(updated: UserProfileData)` (new): mirrors `OnboardingViewModel.saveUserProfileData()`'s recalculation logic — calls `userProfileRepository.getInitialTargetCalories()` for a fresh AI strategy + `MacrosCalculator` pass, falls back to `MacrosCalculator.fallback()` on failure, then persists via the same `saveUserProfileData()`/`saveUserProfileToFirestore()` calls used everywhere else. Exposed via a new `isRecalculating: StateFlow<Boolean>` so the Save button can show a "Saving..." state. This duplicates ~15 lines against the onboarding ViewModel rather than introducing a shared use-case class for two call sites — deliberate, per the project's "don't add abstractions beyond what's needed" guidance.
- Fixed the same status-bar-inset bug found in Phase 4 (`.statusBarsPadding()` missing from the screen's root `Column`) proactively, before it showed up in a screenshot this time.

**Verified live**: confirmed the read-only view matches the mockup closely (card layout, values). Opened "Edit targets" — form correctly prefilled (Age 28, Weight 75, Height 175, Male, Active); the Goal segment showed nothing selected, which is correct, not a bug — this test profile's `workoutGoal` predates the new Lose/Maintain/Gain values from an earlier test session. Selected "Lose", tapped Save: returned to the view immediately (optimistic), then Carbs (322→293g) and Fat (66→79g) updated in place moments later once the real AI recalculation completed and the reactive `Flow` picked up the persisted change — no navigation or manual refresh needed. Zero crashes.

## Cleanup pass ✅ done (2026-07-28)

**What changed** (all confirmed dead via `grep` across the whole `app/src/main/java` tree before deletion — zero references outside their own definitions):
- `ui/theme/Color.kt`: deleted the entire "LEGACY — Kinetic Precision HUD palette" section — `KineticBackground`/`KineticSurface*`/`KineticOutlineVariant`, all `Meal*` constants (`MealBg`, `MealCard`, `MealVolt`, `MealCyan`, `MealDanger`, `MealWhite`, `MealGray`, `MealDescription`, `MealInputBg`, `MealInactive`, `TealCheckmark`, `SyncDot`, `SyncText`, `CTAColor`), and the old lime/cyan `Kinetic{Primary,Secondary,Tertiary,OnSurface,...}` raw hex constants. Confirmed first that `KineticColors.kt`'s `KineticLightColors`/`KineticDarkColors` only reference `Redesign*` constants now, not these.
- `ui/theme/KineticTypography.kt`: deleted `LexendFamily`, `PlusJakartaSansFamily`, `SpaceGroteskFamily`, `BarlowCondensedFamily` — `InterFamily` is the only type family left, and has been since Phase 0's token work.
- `data/model/ai/AIResponse.kt`: removed the dead `WorkoutPlan` sealed variant and its `"workout"` parse branch. Confirmed `AIResponse.parse()`'s only real caller (`FoodAIService.getInitialTargetCalories`) only ever pattern-matches on `NutritionStrategyResult` — the `WorkoutPlan` variant could never be observed by any caller.
- Deleted `data/model/WorkoutPlan.kt` entirely (`WorkoutPlanResponse`/`WorkoutPlan`/`Exercise`) — its only two consumers (the `AIResponse` branch above and `DataStoreHelper`'s workout-plan persistence) were both dead.
- `data/preference/DataStoreHelper.kt`: removed `saveWorkoutPlan()`/`workoutPlan` Flow/`CURRENT_WORKOUT_PLAN_KEY` — confirmed zero callers anywhere (the workout-plan feature was hardcoded/dummy from the start, per Phase 0's original findings, so this was never actually wired to real data).

**Verified**: `./gradlew :app:compileDebugKotlin` and a full `:app:assembleDebug` both clean. A final `grep` sweep for all the deleted symbol names across `app/src/main/java` came back empty. Booted the emulator, installed, launched — no crashes, screen rendered correctly (font rendering unaffected, confirming the Google Fonts provider config didn't depend on the deleted `FontFamily` declarations), and the previously-saved "Lose" goal recalculation from Phase 6 testing (Carbs 293g, Fat 79g) persisted correctly through the change, confirming no data-layer regression.

**All 6 redesign phases plus the cleanup pass are now complete.** The app is fully on the calm/clinical redesign with no remaining legacy HUD code paths.

---

## Verification pattern used for every phase

1. `./gradlew :app:compileDebugKotlin` — must be clean.
2. Boot `Pixel_8_API_35` emulator (`$ANDROID_HOME/emulator/emulator -avd Pixel_8_API_35 -no-snapshot-save -no-boot-anim`), `adb install -r` the debug APK, launch, screenshot, check `adb logcat -d | grep FATAL`.
3. Don't move to the next phase on an unverified one.
