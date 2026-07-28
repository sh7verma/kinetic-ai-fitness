# Kinetic — What Changes Now

_Based on: `docs/research/01` through `docs/research/11` (research date 2026-07-19)_
_Written: 2026-07-26_

The research's bottom line ([10-final-decision.md](research/10-final-decision.md)): **do not launch publicly yet.** The viable product isn't "AI fitness app" — it's *"a fast, trustworthy AI meal logger for people who already track calories and macros."* Everything below narrows Kinetic toward that, in priority order. Each item names the file(s) it touches so it's actionable, not just directional.

---

## P0 — Blockers before any real user touches the app (closed beta gate)

These come from [06-risk-assessment.md](research/06-risk-assessment.md) and [10-final-decision.md](research/10-final-decision.md). None of the rest of this doc matters if these aren't fixed.

1. **Get the OpenAI key out of the APK.**
   `FoodAIService.kt` calls `BuildConfig.OPENAI_API_KEY` directly at 4 call sites. This is extractable from any release APK. Stand up a thin backend proxy (Cloud Run or Firebase Functions), move the key server-side, and have the app call the proxy with an authenticated request instead. Add per-user rate limiting there — the client currently has none.

2. **Turn off full-body network logging in release builds.**
   `NetworkModule.kt:28` sets `HttpLoggingInterceptor.Level.BODY` unconditionally, which writes raw request/response payloads — including food/health data sent to OpenAI — to Logcat on every build variant. Gate this behind `BuildConfig.DEBUG` (or strip it in release).

3. **Replace the destructive Room migration.**
   `DatabaseModule.kt:28` uses `.fallbackToDestructiveMigration()`. Once real users have logged meals, any schema change wipes their data. Write real `Migration` objects before beta.

4. **Hide the placeholder tabs from any build a real user sees.**
   `StatsScreen`, `CoachScreen`, and `DetailScreen` are stubs (literal "Stats Screen" text, a button-only Coach screen, a generic "Detail Screen for Item ID" view). `PlanViewModel` serves hardcoded `dummyExercises`, and `LogExerciseViewModel` doesn't persist anything. Google Play's functionality policy expects stable, meaningful screens — shipping these to beta/production testers is a review and trust risk. Either finish them (see the "Cut from v1" section — most shouldn't be finished yet) or remove them from the nav graph for release builds.

5. **Add a privacy policy, data deletion path, and Play health-app declarations.**
   Kinetic collects nutrition, weight, and biometric data — Google Play treats this as a health app. Account deletion already exists (`ProfileViewModel.deleteAccount()`), which is good; it needs to be paired with a written privacy policy and the Play Data Safety / Health Apps declaration before any beta listing.

---

## P1 — Must-have for v1 (the logging loop itself)

From the "Final Ranked Feature Table" in [11-pain-points-and-feature-scope.md](research/11-pain-points-and-feature-scope.md). These are ranked `must-have v1` and marked `wiring-only` — the underlying data already exists, it just isn't surfaced.

6. **Make AI confidence and assumptions visible to the user.**
   `AILogResponse` already carries a `confidence: Double`, but `UILog`/`UIMeal`/`UIFoodItem` drop it before it reaches the screen. This is the single highest-leverage trust fix identified in the research — surface confidence per food item in the chat/review UI, and show what was assumed (serving size, cooking method, oil/sauce) so users know what to check before saving.

7. **Lower the correction burden in the save flow.**
   Correction burden is churn-driver #1 in the research's ranking. Review `LogFoodComponent` / the meal-edit UI in `ui/components/MealComponents.kt` for friction — fewer taps to adjust grams, clearer per-item edit affordances, and a way to flag "this looks wrong" that feeds back into future estimates (see item 9).

8. **Polish daily totals and meal history.**
   The Fuel dashboard already computes daily calorie/macro totals — make sure they update immediately on save with no stale state, and that history/quick-repeat of previously logged foods is fast (this is churn-driver #4: repeat logging fatigue).

9. **Persist user corrections for future estimates.**
   Today `FoodResolver` caches AI-estimated foods, but a correction just overwrites the log entry — it doesn't feed back into the resolver's cache or the AI prompt. Without this, the app "repeats early mistakes" instead of improving. Wire corrected values back into the local/Firestore food cache so the same meal is estimated better next time.

10. **Add basic funnel/quality analytics.**
    None of this exists yet. Instrument, at minimum: first meal message sent, parse succeeded/failed, meal saved, day-1 and day-7 return, and correction size (how far the user's edit was from the AI's estimate). This is required to evaluate the go/no-go gates in [09-open-questions.md](research/09-open-questions.md) and [10-final-decision.md](research/10-final-decision.md) — without it there's no way to know if beta is working.

---

## Cut or defer from v1 (stop investing here for now)

Per [07-mvp-scope.md](research/07-mvp-scope.md) and [11-pain-points-and-feature-scope.md](research/11-pain-points-and-feature-scope.md), these should be hidden from release, not built further, until the logging loop above proves retention:

- **Workout Plan + Exercise Logging** — currently hardcoded/non-persisted; a shallow implementation is "worse than none." Revisit only after nutrition logging retains users.
- **AI Coach** — currently just a button into the chat screen; don't build it out as a headline feature. If revived later, scope it as a weekly review grounded in the user's actual logged data, not a generic chatbot.
- **Details screen** — doesn't close any identified pain point as built; drop it.
- **Diet Plan generation** — already built, but de-prioritize marketing/investment in it. It doesn't address the top churn reason (untrusted, high-friction logging).
- **Barcode scanning, photo logging, voice logging, social/streak features** — all v2. They're acquisition hooks competitors already have, but building them now would be scope creep before the core loop is trusted.

This also means **the README's current claims about a finished workout plan and progress-tracking trend chart should be walked back** to match reality, consistent with what `docs/APP_STATUS.md` already flagged.

---

## Positioning and go-to-market changes

From [02](research/02-target-segment.md), [03](research/03-competitive-landscape.md), [05](research/05-distribution-strategy.md):

- **Target segment**: stop thinking "general fitness users." Target fitness-serious meal-loggers who already track calories/macros and find their current tracker's logging too slow — not casual wellness users, not serious athletes needing lab-grade precision (Cronometer/MacroFactor own that), not India as a first paid market.
- **Messaging**: lead with *"log messy home-cooked meals in plain English, review the assumptions, correct in seconds, keep your macros on track."* Avoid "AI fitness coach," "perfect calorie estimates," or "personal trainer in your pocket" — those overpromise relative to what's built.
- **Distribution**: no paid ads, no Play Store SEO push yet. Recruit 12–30 closed beta testers directly (fitness Discords, body-recomp/GLP-1 communities, South Asian food/fitness communities) — Google Play requires 12 opted-in testers for 14 continuous days on a new developer account anyway before production access opens up.
- **Pricing**: don't charge yet. When testing willingness to pay, anchor at $4.99–$7.99/month or $39–$59/year — not the $15+/month tier, which requires proven coaching/analytics value Kinetic doesn't have yet. AI inference cost (~$0.01–$0.08/user/month on `gpt-4o-mini`) is a non-issue either way.

---

## Decision gates before considering public launch

Copied from [10-final-decision.md](research/10-final-decision.md) — these are the numbers that answer "are we ready," not opinions:

- 20+ users log real meals in closed beta.
- 40%+ of beta users log at least 2 meals on day 1.
- 20%+ of beta users are still logging on day 7.
- At least 5 users say they'd **replace** their current tracker, not just "also try" Kinetic.
- Correction burden is low enough that AI genuinely saves time versus manual entry.
- At least 10% of beta users show paid intent at the tested price.
- No open P0 security/privacy/Play-policy issue remains.

---

## Suggested order of work

1. P0 security/privacy fixes (items 1–5) — nothing else can ship without these.
2. Surface confidence + assumptions in the chat/review UI (item 6) — highest-leverage trust fix, and the data already exists.
3. Tighten the correction flow and wire corrections back into the food cache (items 7, 9).
4. Add funnel analytics (item 10) so beta results are measurable.
5. Recruit the closed beta cohort and run it against the decision gates above before writing another line of workout/coach/stats code.
