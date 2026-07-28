# Pain Points And Feature Scope

Research date: 2026-07-19

Decision headline: Kinetic's launch positioning should be built around reducing food database search fatigue and portion-entry friction for mixed/home-cooked meals. The current app only partially solves this because confidence exists in the AI response model but is not yet exposed as a user-facing trust loop.

Cross-references:

- [03-competitive-landscape.md](03-competitive-landscape.md) says AI logging is now table stakes and Kinetic should compete on speed, transparency, and a narrower use case.
- [07-mvp-scope.md](07-mvp-scope.md) says the MVP should be a trustworthy AI meal logger, not a full fitness platform.
- [10-final-decision.md](10-final-decision.md) should therefore remain no-go for public launch, go for controlled validation beta after hardening.

## Pain Point Analysis

### What Makes Users Quit Meal And Fitness Logging

Food logging fails because it is repetitive, slow, and cognitively expensive. The strongest churn drivers are:

1. Manual portion estimation and gram-weighing.
   Users do not consistently know grams for bowls, plates, home-cooked portions, sauces, oil, or mixed dishes. Research on food logging and portion-size apps shows portion estimation remains a real source of error even when mobile tools are available.

2. Database search fatigue.
   Traditional apps ask users to search large databases, compare near-duplicate entries, pick serving units, and decide whether a database item is trustworthy. This is especially painful for meals assembled from multiple ingredients.

3. Generic or wrong nutrition data.
   Open food databases can contain duplicate, stale, branded, or user-submitted entries. Precision-focused competitors such as Cronometer use verified/lab-analyzed data as a differentiator, which implies users do care about nutrition-data trust.

4. Poor support for local, cultural, and home-cooked food.
   Western packaged-food databases do not map cleanly to "dal with rice," "paneer sabji," "chicken curry," "homemade poha," or "two rotis with sabji." Users either approximate, give up, or create custom entries.

5. No visible progress feedback.
   A meal log has to convert into daily totals, weekly trends, weight trend, or behavior feedback. Without feedback, the log feels like clerical work.

6. No accountability loop.
   Logging requires daily repetition. Reminders, streaks, goals, coaching, or social/accountability loops can help, but they also add scope and privacy risk.

7. Workout logging feels disconnected from diet logging.
   Body recomposition users care about protein, training, weight trend, and progression together. If workout and diet logs never meet, the app is less useful than a focused nutrition tracker plus a separate workout app.

8. Too many unfinished surfaces.
   Placeholder Coach, Stats, Details, or workout screens can harm trust faster than they help acquisition.

The churn list is not the same as the acquisition list. Photo scanning and AI buzz can get installs; correction burden, missing progress feedback, and low trust make users leave.

## What Kinetic Actually Solves Today

This section is based on current app code, not assumption.

| Pain Point | Current Kinetic Evidence | Solved Today? | Notes |
|---|---|---:|---|
| Database search fatigue | `AIChatViewModel.sendMessage()` sends a natural-language meal message to `DietAIRepository.logFood()`, `FoodAIService.logFood()` parses it, and `AILogResponse.toUILog()` turns it into editable UI meal items. | Partial | The user can describe a meal instead of manually searching first. This is real. |
| Manual gram entry | `AIPrompts.logMealSystemPrompt()` requires grams and includes examples like egg, roti, banana, and bowl rice. `safeGrams()` normalizes grams and falls back to 100g if invalid. | Partial | It reduces first-pass typing, but it does not verify the portion. User review remains mandatory. |
| Local/home-cooked food context | `FoodResolver.resolve()` strips terms like `sabji`, `curry`, `masala`, and `gravy`; prompts include roti/rice examples. | Partial | This is useful for South Asian-style naming, but it is not a validated cultural food database. |
| Generic nutrition data | `FoodResolver` checks local Room first, then Firestore, then AI estimation, and saves newly estimated foods locally and remotely. | Partial | Caching reduces repeated misses, but can also repeat a wrong estimate. There is no verified nutrition source. |
| Confidence-scored parsing | `AILogResponse` has a `confidence: Double`; the prompt defines confidence bands. | Partial | The confidence score exists in the response model, but `UILog`, `UIMeal`, and `UIFoodItem` do not carry it to the user. It is not item-level and not yet a trust feature. |
| Editable review before save | `saveMeal(originalMeal, updatedMeal)` saves edited meal items to `FoodLogDao`; `LogFoodComponent` presents editable meal values. | Yes | This is one of the strongest current MVP assets. |
| Workout-diet integration | `LogExerciseViewModel` only mutates local screen state. `PlanViewModel` uses dummy exercise data. | No | This does not support a launch claim around body recomposition coaching. |
| Progress visibility | `StatsScreen` currently renders only "Stats Screen." | No | This is a core retention gap. |
| Accountability/coaching | `CoachScreen` is a title plus a Nutrition Chat button. | No | There is no accountability loop, weekly review, or behavior memory. |
| Details depth | `DetailScreen` displays a generic item ID. | No | Not load-bearing for v1. |

The honest claim is: Kinetic has a real natural-language meal logging loop, a confidence value in the AI response, and an editable save flow. It does not yet have confidence-scored parsing as a visible user promise.

## Pain Points Still Unsolved

High-risk unsolved pain points:

- User-facing confidence and assumptions are missing.
- Stats is a stub, so progress feedback is missing.
- Workout and diet are not integrated.
- There is no barcode fallback for packaged foods or AI misses.
- There is no verified restaurant/packaged food database.
- There is no streak, reminder, or accountability loop.
- There is no robust background sync path for cross-device or reliable recovery.
- There is no retention instrumentation around correction burden, repeat logging, or day-7 return behavior.

These matter because food logging adherence declines over time. A 2019 dietary self-monitoring study found adherence measures declined and fewer than half of participants were still tracking after week 10. App benchmarks also show health and fitness day-30 retention is low, so Kinetic should assume churn pressure is severe until proven otherwise.

## Churn Ranking

Ranked by likely contribution to users quitting after initial curiosity:

| Rank | Pain Point | Why It Drives Churn | Kinetic Status |
|---:|---|---|---|
| 1 | Correction burden is still high | If the AI estimate needs heavy edits, users return to their old tracker or stop logging. | Partially solved |
| 2 | No progress visibility | Logging feels pointless without daily/weekly feedback. | Unsolved |
| 3 | Low trust in estimates | Users will not rely on vague or hidden assumptions for calories/macros. | Partially solved internally, not solved in UI |
| 4 | Repeat logging is not fast enough | Most users eat repeated foods. Re-entry friction kills habit formation. | Partially solved through local food cache, not polished as UX |
| 5 | No fallback when AI misses | Packaged foods, restaurants, and exact brands need barcode/database fallback. | Unsolved |
| 6 | No accountability loop | Daily logging needs prompts, streaks, or reviews. | Unsolved |
| 7 | Workout-diet disconnection | Fitness-serious users want training, protein, weight trend, and adherence linked. | Unsolved |
| 8 | Placeholder surfaces | Stubbed screens reduce trust. | Partially mitigated in release nav, still present in code |

## Acquisition Ranking

Ranked by likely ability to make someone install or try Kinetic:

| Rank | Pain Point Or Hook | Why It Drives Acquisition | Kinetic Status |
|---:|---|---|---|
| 1 | "Log meals in plain English" | Clear, quick, demo-friendly value proposition. | Partially solved |
| 2 | Home-cooked/local food support | Differentiates from packaged-food database workflows. | Partially solved |
| 3 | AI confidence and assumptions | Strong trust message if made visible. | Not user-facing yet |
| 4 | Photo or barcode fallback | Users expect this because competitors have it. | Unsolved |
| 5 | Protein/body-recomp dashboard | Fitness-serious users understand the outcome. | Partially solved through macro data, weak UI |
| 6 | AI coach | Attractive headline, but risky if shallow. | Mostly unsolved |
| 7 | Workout integration | Broadens market but increases scope. | Unsolved |
| 8 | Social/accountability | Can attract communities but is not needed for first MVP. | Unsolved |

The first launch message should optimize for acquisition rank 1 and churn ranks 1-3: plain-English meal logging, less correction burden, visible assumptions, and daily macro feedback.

## Feature Gap - More

Features competitors have that close real pain points:

| Feature | Competitor Evidence | Pain Point Closed | Retention Or Acquisition | Infra Impact |
|---|---|---|---|---|
| Barcode scan | MyFitnessPal, Cronometer, Lose It, and Lifesum support barcode workflows. | Exact packaged-food fallback when AI estimate is wrong or unnecessary. | Retention | New data source; can reuse `FoodResolver`, Room, and Firestore after lookup |
| Verified/curated nutrition database | Cronometer differentiates on verified/lab-analyzed nutrition data. MyFitnessPal claims a very large food database. | Trust in nutrition values. | Retention | New data source and data licensing/quality process |
| Recent meals and quick repeats | Mature trackers support history and repeated foods/meals. | Repetitive daily logging fatigue. | Retention | Mostly wiring-only on Room/FoodLogDao if data model supports meal grouping |
| Daily and weekly progress charts | Cronometer, MacroFactor, MyFitnessPal, and Lose It all provide progress/trend views. | Logging feels disconnected from outcomes. | Retention | Wiring-only for local food logs; new architecture only for cloud sync/multi-device |
| Weight trend and adaptive targets | MacroFactor's core value is adaptive macro coaching from trend/adherence. | Fitness-serious users need feedback, not just logs. | Retention | New logic and data model; can start local, backend later |
| Confidence and assumption UI | Kinetic has internal confidence but competitors mostly emphasize edit/review. | Trust and correction burden. | Both | Wiring-only for current confidence; new model schema for item-level assumptions |
| Photo logging | Cal AI, Cronometer, MacroFactor, Lose It, Lifesum, and SnapCalorie support photo/AI logging. | Reduces typing, strong demo hook. | Acquisition | New architecture/model path; potentially image upload/storage/privacy |
| Voice logging | MyFitnessPal, Cronometer, MacroFactor, Lose It, Lifesum, and SnapCalorie support voice or speech/text flows. | Logging while busy. | Both | Wiring-only if speech-to-text is on-device; new architecture if server processed |
| Reminders/streaks | Common habit app mechanic, not unique to nutrition. | Daily return habit. | Retention | Wiring-only for local reminders/streaks |
| Social/accountability | Useful for some cohorts, but high moderation/privacy burden. | Motivation and accountability. | Retention for some, acquisition for communities | New architecture and policy surface |
| Workout-diet linkage | Macro/fitness users care about training, protein, weight, and adherence together. | Diet feels disconnected from body-recomp goal. | Retention | New data model and real exercise persistence |

## Feature Gap - Less

### Coach

Current state: `CoachScreen` is mostly a title and a button into Nutrition Chat.

Decision: cut from v1 public surface unless it becomes a weekly review grounded in logged meals, targets, and corrections.

Why: AI Coach is an acquisition phrase, but the current implementation does not close a proven retention pain point. It risks overpromising.

### Background Sync

Current state: local Room and Firestore food cache exist; `WelcomeViewModel` calls food sync, and DataStore tracks last food sync. Full user-log sync is not mature.

Decision: must-have v1 only as reliable local-first behavior and safe retry for food data. Full multi-device sync can be v2, but release must not lose logs.

Why: users forgive no multi-device sync in a beta; they do not forgive lost meal logs.

### Details

Current state: `DetailScreen` shows a generic item ID.

Decision: cut from v1.

Why: it does not close a top churn or acquisition pain point. Details should reappear only when it shows meaningful food, meal, or progress insight.

### Workout Plan And Exercise Logging

Current state: workout plan is hardcoded and exercise logging mutates screen state without a real persisted training history.

Decision: cut from v1 public surface. Revisit in v2 only after nutrition logging retains users.

Why: workout-diet integration is retention-relevant for body-recomp users, but a shallow implementation is worse than none.

### Diet Plan Generation

Current state: AI diet-plan generation exists.

Decision: v2 unless beta users ask for it after they trust logging.

Why: diet plans sound valuable, but the immediate churn pain is "logging is annoying and untrusted." A plan without retained logging does not solve that.

### Onboarding Depth

Current state: onboarding captures biometrics, activity, cuisine/preferences, meal setup, and workout setup.

Decision: trim any onboarding step that does not improve first meal logging or macro targets.

Why: long onboarding can hurt activation before the user experiences the core logging value.

## Scope Decision

Kinetic should launch validation around one pain point:

"Searching databases and estimating portions for real home-cooked meals is too slow and untrustworthy; Kinetic lets users describe the meal, see the assumptions, correct it quickly, and track daily macros."

That means v1 should be smaller than a full fitness app, but slightly sharper than the current logging flow:

- Keep natural-language meal logging.
- Add visible confidence and assumptions.
- Make corrections faster.
- Add daily totals, history, and quick repeats.
- Hide or cut Coach, Details, and workout surfaces from public v1.
- Do not add photo, social, or full workout planning before the logging loop retains users.

## Sources

- [03 Competitive Landscape](03-competitive-landscape.md)
- [07 MVP Scope](07-mvp-scope.md)
- [10 Final Decision](10-final-decision.md)
- [PubMed - Dietary self-monitoring adherence](https://pubmed.ncbi.nlm.nih.gov/31155473/)
- [ScienceDirect - MyFitnessPal usability challenges](https://www.sciencedirect.com/science/article/pii/S0899900718303678)
- [PMC - PortionSize and MyFitnessPal comparison](https://pmc.ncbi.nlm.nih.gov/articles/PMC11347805/)
- [JMIR - Mobile weight loss app engagement over time](https://www.jmir.org/2022/6/e34191/)
- [Braze - Mobile app retention](https://www.braze.com/resources/articles/mobile-app-retention-10-tip)
- [MyFitnessPal Premium Features](https://support.myfitnesspal.com/hc/en-us/articles/360032625951-What-are-the-features-of-MyFitnessPal-Premium)
- [Cronometer Gold](https://cronometer.com/gold/index.html)
- [MacroFactor AI Describe](https://help.macrofactorapp.com/en/articles/216-log-foods-with-ai-describe)
- [MacroFactor AI Food Logging](https://macrofactor.com/ai-food-logging/)
- [Lose It Snap It](https://loseit.zendesk.com/hc/en-us/articles/47771695186580-How-to-Use-Snap-It)
- [Lifesum Food Tracking Help](https://help.lifesum.com/en/category/food-tracking-1rthkrv/)
- [SnapCalorie App Store](https://apps.apple.com/us/app/snapcalorie-ai-calorie-counter/id1574239307)
- Current implementation evidence: `AIPrompts.logMealSystemPrompt`, `FoodAIService.logFood`, `AILogResponse`, `AIChatViewModel`, `FoodResolver`, `StatsScreen`, `CoachScreen`, `DetailScreen`, `LogExerciseViewModel`, `PlanViewModel`.

## Final Ranked Feature Table

Build-cost buckets: `wiring-only` means existing Room, Firestore, FoodResolver, DataStore, prompt/schema, or UI wiring is enough. `new architecture` means a backend, new external data source, new storage/sync model, moderation/accountability system, or image pipeline is required.

| Feature | Pain Point It Solves | Priority | Build Cost |
|---|---|---|---|
| 1. User-facing confidence and assumptions | Low trust in AI calories/macros; hidden portion guesses | must-have v1 | wiring-only |
| 2. Fast correction UI before save | Heavy correction burden after AI parse | must-have v1 | wiring-only |
| 3. Daily calorie/protein/macro totals | Logging feels disconnected from outcome | must-have v1 | wiring-only |
| 4. Meal history and quick repeat meals | Repetitive daily logging fatigue | must-have v1 | wiring-only |
| 5. Backend proxy for AI calls | Security and rate-limit risk from API key in APK | must-have v1 | new architecture |
| 6. Release-surface pruning | Placeholder Coach/Stats/Workout harms trust | must-have v1 | wiring-only |
| 7. Local-first persistence without destructive migration | Lost logs destroy trust | must-have v1 | wiring-only |
| 8. Barcode fallback | Packaged-food exactness when AI is unnecessary or wrong | v2 | new architecture |
| 9. Verified/curated nutrition source | Generic/wrong nutrition data | v2 | new architecture |
| 10. Weekly progress trends and weight trend | No feedback loop beyond today's totals | v2 | wiring-only |
| 11. Reminders and lightweight streaks | Users forget or stop returning | v2 | wiring-only |
| 12. Voice input | Busy users want low-friction capture | v2 | wiring-only |
| 13. Photo logging | Strong acquisition hook and low-typing flow | v2 | new architecture |
| 14. Workout-diet integration | Body-recomp users need training and nutrition linked | v2 | new architecture |
| 15. AI coach as weekly review | Accountability and behavior feedback | v2 | new architecture |
| 16. Social/accountability groups | Motivation for some cohorts | cut | new architecture |
| 17. Generic Details screen | Does not solve a top pain point as currently built | cut | wiring-only |
| 18. Full workout planner in v1 | Broadens scope before nutrition retention is proven | cut | new architecture |
| 19. Standalone AI diet-plan generation as headline | Sounds valuable but does not solve logging churn first | cut | wiring-only |
