# MVP Scope

Research date: 2026-07-19

Decision headline: The MVP should be a trustworthy AI meal logger, not a full fitness platform.

## Current App Reality

Kinetic currently has a strong start around AI-assisted food logging:

- Onboarding collects body and goal data.
- Fuel flow supports conversational meal logging.
- AI response can be converted into editable UI meal log items.
- Foods are cached locally and can be resolved through Firestore or AI nutrition estimation.
- Room stores foods, food logs, diet plans, and exercises.
- DataStore stores profile, current diet plan, current workout plan, and sync state.

But several visible product areas are not ready:

- Workout plan data is hardcoded.
- Exercise logging is not production-complete persistence.
- Stats and Coach surfaces are not mature enough.
- Details screens are incomplete relative to the implied product promise.
- OpenAI access is client-side.

## Launch MVP

The minimum viable public-facing product should include only:

### Required Core

- Account or local profile setup.
- Goal and macro target setup.
- Natural-language meal logging.
- AI parse into food items.
- Editable calories, protein, carbs, fat, quantity, and serving assumptions.
- Save meal to local store.
- Daily totals for calories and macros.
- Meal history.
- Repeat previous foods/meals quickly.
- Error and empty states.
- Offline-safe local source of truth.

### Required Trust Features

- Show "estimated" labels.
- Show assumptions behind portions.
- Confidence indicator per meal or item.
- Easy correction before saving.
- Persistent corrections for future estimates.
- Clear disclaimer that the app is not medical advice.

### Required Technical Safety

- Backend proxy for OpenAI calls.
- No API key in APK.
- No sensitive body-level network logging in release builds.
- Non-destructive migrations.
- Privacy policy.
- Data deletion path.
- Basic analytics for activation and retention.

## Hide Or Defer

Defer from public MVP:

- AI coach tab.
- Workout planning tab.
- Exercise logging.
- Advanced stats.
- Wearable integrations.
- Social features.
- Meal plans.
- Photo logging, unless implemented to a high standard.
- Barcode scanning, unless a credible database/API is selected.

Debug builds can keep development tabs, but release builds should not expose unfinished experiences.

## Why This Scope

Mature competitors already set a high expectation for full-featured tracking:

- MyFitnessPal offers a huge database, barcode, voice, meal scan, and premium macro tools.
- Cronometer offers verified nutrition data, micronutrients, biometrics, charts, and photo/voice logging.
- MacroFactor offers adaptive macro coaching and AI food logging.
- Lose It offers photo, barcode, AI voice, and long-standing weight-loss workflows.

Kinetic cannot be feature-complete against these on day one. It can be meaningfully useful if the first workflow is faster and more trustworthy for the right users.

## MVP Acceptance Criteria

Before public launch, the MVP should pass:

- 95%+ successful AI parse rate for the beta meal prompt set.
- Median meal log flow under 30 seconds for repeat users.
- User can correct an estimate without leaving the flow.
- Daily totals update immediately after save.
- App works after network loss for saved logs.
- No release build exposes OpenAI keys.
- No placeholder tabs visible in release.
- Privacy policy and deletion flow are reachable.
- Crash-free session rate acceptable for Play review and beta use.

## MVP Decision

Go for a narrow MVP rebuild/hardening pass.

No-go for a full-platform launch. The most valuable thing to ship is the logging loop: message, parse, review, correct, save, summarize, repeat.

## Sources

- [MyFitnessPal Premium Features](https://support.myfitnesspal.com/hc/en-us/articles/360032625951-What-are-the-features-of-MyFitnessPal-Premium)
- [Cronometer Gold](https://cronometer.com/gold/index.html)
- [Cronometer - 4 Ways to Log Food](https://cronometer.com/blog/4-ways-to-log-food-on-cronometer/)
- [MacroFactor AI Describe](https://help.macrofactorapp.com/en/articles/216-log-foods-with-ai-describe)
- [MacroFactor AI Food Logging](https://macrofactor.com/ai-food-logging/)
- [Lose It Snap It](https://loseit.zendesk.com/hc/en-us/articles/47771695186580-How-to-Use-Snap-It)
- [Google Play Console Help - Functionality](https://support.google.com/googleplay/android-developer/answer/9898783)
- [Android Developers - Offline-first data layer](https://developer.android.com/topic/architecture/data-layer/offline-first)
- [Android Developers - WorkManager](https://developer.android.com/reference/androidx/work/WorkManager.html)
- [Android Developers - DataStore](https://developer.android.com/topic/libraries/architecture/datastore)
- [OpenAI - API Key Safety Best Practices](https://help.openai.com/en/articles/5112595-best-practices-for-api-key-safety)
