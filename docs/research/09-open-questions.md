# Open Questions

Research date: 2026-07-19

Decision headline: The next work should answer retention, accuracy, willingness-to-pay, and trust questions with real users before public launch.

## Business Questions

1. Which wedge wins first: faster macro logging, home-cooked meal logging, South Asian food logging, or GLP-1 nutrition support?
2. Will users replace an existing tracker, or only try Kinetic once because AI sounds interesting?
3. What price produces real intent: $4.99/month, $7.99/month, $59/year, or $79/year?
4. Is there a viable India price point, or should India remain a later localized strategy?
5. Does the user value "confidence and assumptions" enough to differentiate Kinetic from photo-first apps?

## Product Questions

1. What is the minimum correction UI that makes users trust an AI estimate?
2. Should Kinetic ask clarifying questions before estimating, or estimate first and let users correct?
3. Which macro targets matter most: calories/protein only, or full protein/carbs/fat?
4. Do users need meal plans, or do plans distract from logging?
5. Are workout and coach tabs harmful if they are unfinished?
6. Should release builds hide everything except Fuel, Profile, and a basic Daily view?
7. What is the right disclaimer language so the app is trusted but not overclaiming?

## Accuracy Questions

1. What is the acceptable error band for the first target segment?
2. How often does the parser miss hidden calories from oil, sauces, sugar, nuts, and cooking method?
3. How accurate are estimates for mixed Indian/South Asian meals?
4. Does the food resolver cache improve accuracy over time, or merely repeat early mistakes?
5. How should Kinetic record user corrections for future personalization?
6. Should Kinetic block saving very low-confidence estimates until the user confirms assumptions?

## Technical Questions

1. What backend should own OpenAI calls: Firebase Functions, Cloud Run, or another lightweight API?
2. What authentication model should gate AI requests?
3. What rate limits should exist per user/day and per account?
4. What telemetry is allowed under the privacy policy?
5. What is the canonical source of truth for user logs: local Room, Firestore, or backend database?
6. How should multi-device conflicts be resolved?
7. What migration policy replaces destructive Room migration?
8. Should Kinetic migrate from JSON mode to Structured Outputs for AI parsing?
9. Which cheaper model passes a fixed nutrition parsing benchmark?

## Legal And Policy Questions

1. Which launch country is first: US only, India only, or both?
2. What health data does Kinetic collect, process, store, or share?
3. Does any analytics or attribution SDK receive health-adjacent event data?
4. What data deletion and account deletion flow is required before launch?
5. How will Kinetic satisfy Google Play Health Apps and User Data requirements?
6. What DPDP compliance steps are needed if India users are included?
7. What FTC Health Breach Notification Rule obligations might apply in the US?

## Validation Questions To Ask Users

During observation:

- "Log what you actually ate today."
- "What would you change before saving this?"
- "Which assumption did you trust least?"
- "Would you use this instead of your current tracker tomorrow?"
- "What would make this worth paying for?"
- "At what price would you stop considering it?"

Avoid asking:

- "Do you like this app?"
- "Would you use an AI fitness coach?"
- "Is this a good idea?"

Behavior matters more than encouragement.

## Decision Gates

Answer these before public launch:

- At least 12 testers complete closed testing requirements if applicable.
- 20+ users log real meals.
- 10+ users return after day 7.
- At least 5 users say they would replace their current tracker.
- Median correction burden is low enough that AI saves time.
- No critical privacy/security blocker remains.
- No placeholder claim is visible in release.

## Sources

- [Nielsen Norman Group - User Interviews](https://www.nngroup.com/articles/user-interviews/)
- [Nielsen Norman Group - Why You Only Need to Test with 5 Users](https://www.nngroup.com/articles/why-you-only-need-to-test-with-5-users/)
- [Y Combinator - Practical Design User Observation](https://www.ycombinator.com/blog/practical-design-user-observation/)
- [Google Play Console Help - Closed testing requirement](https://support.google.com/googleplay/android-developer/answer/14151465)
- [Google Play Console Help - Health Apps](https://support.google.com/googleplay/android-developer/answer/16679511)
- [Google Play Console Help - User Data](https://support.google.com/googleplay/android-developer/answer/10144311)
- [OpenAI - Structured Outputs](https://openai.com/index/introducing-structured-outputs-in-the-api/)
- [HHS - Health Apps and HIPAA](https://www.hhs.gov/hipaa/for-professionals/special-topics/health-apps/index.html)
- [FTC - Mobile Health Apps Interactive Tool](https://www.ftc.gov/business-guidance/resources/mobile-health-apps-interactive-tool)
