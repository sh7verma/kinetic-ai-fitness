# Distribution Strategy

Research date: 2026-07-19

Decision headline: Kinetic should not rely on generic Play Store discovery. Use a controlled beta, community-led validation, and narrow ASO around AI meal logging.

## Launch Sequence

Recommended sequence:

1. Closed beta with real Android users.
2. Manual onboarding of first 12-30 testers.
3. Instrumented cohort tracking for meal logs, corrections, return usage, and deletion.
4. Small public waitlist or landing page once the beta workflow works.
5. Play Store public release only after privacy, security, retention, and core MVP quality gates pass.

If this is a new personal Google Play developer account, Google Play requires closed testing with at least 12 opted-in testers for 14 continuous days before applying for production access.

## Channel Priorities

### Google Play Search

Use Play Store optimization, but do not expect it to create the business alone.

Google says Play discovery and ranking use metadata, relevance, app quality, ratings/reviews, engagement, technical performance, and assets. That means a new app with no rating base and immature retention will struggle.

Kinetic's initial store positioning should focus on:

- AI meal logger
- Macro tracker
- Calorie tracker
- Protein tracker
- Fast food logging
- Home-cooked meal logging

Avoid broad claims such as "AI fitness coach" until the coach and workout surfaces are fully useful.

### Fitness Communities

Best early acquisition channel:

- Small fitness Discords.
- Body recomposition communities.
- GLP-1 support communities where nutrition preservation is discussed.
- South Asian fitness/nutrition communities.
- Friends-of-friends gym users.

The ask should be "help test a faster meal logger" instead of "download my app."

### Reddit

Use carefully.

Reddit's spam policy and Reddiquette discourage repeated self-promotion and require respecting community rules. For Kinetic, Reddit is useful for:

- Recruiting testers after participating genuinely.
- Asking for feedback on the logging workflow.
- Sharing transparent lessons or validation questions.

It is risky for launch blasting.

### Product Hunt

Product Hunt is useful for feedback, social proof, and founder visibility. It is not likely to be the main source of retained fitness users.

Use only after:

- The app has a public waitlist or install path.
- The first comment clearly explains the narrow product.
- There are screenshots or a short demo of the real logging flow.
- There is a feedback ask, not just a request for votes.

### Paid Ads

No-go until LTV and retention are known.

The category is crowded, competitor ads are sophisticated, and downloads alone do not prove business viability. Paid acquisition should wait until:

- At least day-7 retention is known.
- Pricing intent has been measured.
- The activation event is clear.
- The onboarding funnel has no obvious drop-off.

## Messaging

Good first positioning:

"Fast AI meal logging for people who track calories and macros."

Better if validated:

"Log messy home-cooked meals in plain English, review the assumptions, and keep your macros on track."

Avoid:

- "The ultimate AI fitness coach."
- "Perfect calorie estimates."
- "Medical nutrition advice."
- "Personal trainer and dietitian in your pocket."

## Metrics To Instrument Before Distribution

Core funnel:

- Install
- Onboarding completed
- First meal message sent
- AI parse succeeded
- User edited estimate
- User saved meal
- Second meal saved
- Day-1 return
- Day-7 return
- Pricing screen viewed
- Subscription intent or purchase

Quality metrics:

- Parse failure rate
- User correction size by calories/protein/carbs/fat
- Food resolver cache hit rate
- New food estimation rate
- Average model latency
- Cost per active logger

## Distribution Decision

Go for closed beta distribution.

No-go for public launch or paid acquisition today. The product needs proof that the first 12-30 users repeatedly log meals and trust the output enough to continue.

## Sources

- [Google Play Console Help - App quality and discovery](https://support.google.com/googleplay/android-developer/answer/9958766)
- [Google Play Console Help - Store listing best practices](https://support.google.com/googleplay/android-developer/answer/9898842)
- [Google Play Console - Store Listings](https://play.google.com/intl/en-GB/console/about/storelistings/)
- [Google Play Console Help - Closed testing requirement](https://support.google.com/googleplay/android-developer/answer/14151465)
- [Product Hunt Launch Guide](https://www.producthunt.com/launch)
- [Product Hunt - Before Launch](https://www.producthunt.com/launch/before-launch)
- [Product Hunt - Preparing for Launch](https://www.producthunt.com/launch/preparing-for-launch)
- [Reddit Spam Policy](https://support.reddithelp.com/hc/en-us/articles/360043504051-Spam)
- [Reddiquette](https://support.reddithelp.com/hc/en-us/articles/205926439-Reddiquette)
- [Reddit Rules](https://redditinc.com/policies/reddit-rules)
