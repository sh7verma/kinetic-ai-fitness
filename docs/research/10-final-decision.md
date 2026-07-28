# Final Decision

Research date: 2026-07-19

Decision: NO-GO for public real-user launch today. GO for a controlled validation beta after security, privacy, and MVP-scope fixes.

## Why Not Launch Publicly Today

Kinetic is promising, but not launch-ready as a public real-user product.

The main blockers are:

- OpenAI API key is shipped in the Android client.
- The app handles health-adjacent nutrition and fitness data but needs final privacy, deletion, and Play policy readiness.
- Several visible product surfaces imply a full fitness platform but are not production complete.
- Nutrition estimates are inherently uncertain and need confidence, assumption display, and correction loops.
- Retention has not been proven in a category where day-30 retention is often very low.
- Competitors already offer AI logging, voice, barcode, photo logging, food databases, and polished subscription funnels.

Public launch would risk weak retention, poor reviews, privacy/security exposure, and unclear positioning.

## What Is Viable

The viable business is not "AI fitness app." It is:

"A fast, trustworthy AI meal logger for people who already track calories and macros."

The launch positioning should be built around one pain point:

"Searching databases and estimating portions for real home-cooked meals is too slow and untrustworthy; Kinetic lets users describe the meal, see the assumptions, correct it quickly, and track daily macros."

The best first wedge is:

- Fitness-serious users.
- Body recomposition or protein-focused users.
- Users who find current food tracking too slow.
- Users with mixed/home-cooked meals that are hard to search in a database.

This is a real opportunity because the diet/nutrition market is growing, consumer spend is meaningful, and AI logging demand is proven. But the product has to be narrower and sharper than the current app surface suggests.

## Required Fixes Before Closed Beta

P0:

- Move OpenAI calls behind a backend proxy.
- Remove API keys from the APK.
- Disable sensitive BODY network logging in release builds.
- Add privacy policy, data handling disclosure, and deletion path.
- Prepare Google Play Health Apps and Data Safety declarations.
- Hide or finish placeholder workout, stats, and coach surfaces.

P1:

- Add confidence and assumptions to AI meal estimates.
- Improve correction UX.
- Persist user corrections.
- Add day totals and meal history polish.
- Add quick repeat meals for foods/meals users eat often.
- Add analytics for activation, correction, parse failure, return usage, and pricing intent.
- Replace destructive Room migration before real user data accumulates.

## Closed Beta Go Criteria

Kinetic can enter controlled beta when:

- Release build has no embedded OpenAI secret.
- Only working MVP surfaces are visible.
- Users can log, review, correct, save, and revisit meals.
- Data deletion path exists.
- Privacy policy is accurate.
- Crash and parse errors are observable.
- The app makes clear that nutrition values are estimates, not medical advice.

If using a new personal Play developer account, plan for at least 12 opted-in testers for 14 continuous days.

## Public Launch Go Criteria

Do not launch publicly until validation shows:

- 20+ users log real meals.
- 40%+ of beta users log at least 2 meals on day 1.
- 20%+ of beta users continue logging on day 7.
- At least 5 users say they would replace their current tracker.
- Median logging time is meaningfully faster than their current method.
- Correction burden is low enough that AI saves time.
- At least 10% of beta users show paid intent at the proposed price.
- No critical privacy, security, or Play policy issue remains.

## Pricing Recommendation

Do not charge publicly yet.

For validation:

- Test $4.99-$7.99/month.
- Test $39-$59/year.
- Avoid premium pricing until retention and replacement intent are proven.

Long-term, $59-$79/year is plausible if Kinetic becomes a trusted daily logger. AI inference cost is small enough that pricing should be set by value and retention, not token cost.

## One-Sentence Decision

Kinetic should not launch publicly yet; it should narrow to the pain point of fast, trustworthy home-cooked meal logging, fix production blockers, run a real closed beta, and decide launch only after retention and replacement intent are proven.

## Sources

- [Grand View Research - Diet And Nutrition Apps Market](https://www.grandviewresearch.com/industry-analysis/diet-nutrition-apps-market-report)
- [Sensor Tower - Health and Fitness Apps and AI](https://sensortower.com/blog/health-and-fitness-apps-ai)
- [TechCrunch - MyFitnessPal acquired Cal AI](https://techcrunch.com/2026/03/02/myfitnesspal-has-acquired-cal-ai-the-viral-calorie-app-built-by-teens/)
- [MyFitnessPal Membership Pricing](https://blog.myfitnesspal.com/myfitnesspal-membership-pricing-tiers/)
- [Cronometer Gold](https://cronometer.com/gold/index.html)
- [MacroFactor Subscriptions](https://help.macrofactorapp.com/en/articles/393-how-macrofactor-subscriptions-and-bundles-work)
- [Lose It Premium Pricing](https://loseit.zendesk.com/hc/en-us/articles/47574703250580-Updates-to-Lose-It-Membership-Pricing)
- [OpenAI - API Key Safety Best Practices](https://help.openai.com/en/articles/5112595-best-practices-for-api-key-safety)
- [Google Play Console Help - Health Apps](https://support.google.com/googleplay/android-developer/answer/16679511)
- [Google Play Console Help - User Data](https://support.google.com/googleplay/android-developer/answer/10144311)
- [Google Play Console Help - Functionality](https://support.google.com/googleplay/android-developer/answer/9898783)
- [Google Play Console Help - Closed testing requirement](https://support.google.com/googleplay/android-developer/answer/14151465)
- [PubMed - Dietary self-monitoring adherence](https://pubmed.ncbi.nlm.nih.gov/31155473/)
- [Braze - Mobile app retention](https://www.braze.com/resources/articles/mobile-app-retention-10-tip)
- [HHS - Health Apps and HIPAA](https://www.hhs.gov/hipaa/for-professionals/special-topics/health-apps/index.html)
- [FTC - Mobile Health Apps Interactive Tool](https://www.ftc.gov/business-guidance/resources/mobile-health-apps-interactive-tool)
- [11 Pain Points And Feature Scope](11-pain-points-and-feature-scope.md)
