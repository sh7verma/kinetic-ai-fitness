# Scalability And Expansion

Research date: 2026-07-19

Decision headline: The architecture can support a beta, but production scale requires a backend AI layer, explicit sync rules, and staged expansion.

## Current Scalability Strengths

Kinetic has useful foundations:

- Room local persistence.
- Firestore-backed shared food lookup.
- Local food cache to reduce repeated AI calls.
- DataStore for profile and plan state.
- Modular services for food AI, food resolving, repositories, and view models.

This is enough for controlled beta scale if release security is fixed.

## Current Scalability Weaknesses

Production issues:

- AI calls happen directly from the Android app.
- API keys are embedded through `BuildConfig`.
- Sync and conflict rules are not clearly defined.
- Room uses destructive migration, risking data loss.
- Firestore indexes and document shapes are not yet validated against real use.
- There is no server-side rate limiting or model fallback.
- There is no mature telemetry for parse failures, correction size, or retention.

## Infrastructure Cost Outlook

Firestore should not block early scale:

- Free tier includes 1 GiB storage, 50K reads/day, 20K writes/day, 20K deletes/day, and 10 GiB outbound/month.
- Google Cloud's listed Firestore standard prices start at $0.03 per 100K reads, $0.09 per 100K writes, and $0.01 per 100K deletes, plus storage and bandwidth.
- Firebase's published billing examples show modest costs at far larger usage than an early beta, though real costs depend on listeners, indexes, and data modeling.

The practical early cost concern is not Firestore. It is avoiding uncontrolled OpenAI usage and repeated uncached food estimation.

## Scaling Principles

Recommended production architecture:

- Android client writes user logs to local Room first.
- WorkManager syncs to backend/Firestore with retry.
- Backend owns OpenAI prompts, keys, rate limits, and model selection.
- Firestore stores canonical shared food entries and user-owned logs only where needed.
- Local cache remains the fastest source for repeated user foods.
- Security rules enforce user ownership and minimal public reads.
- AI output uses strict structured schemas.

Firestore offline persistence uses a last-write-wins model for conflicts, so Kinetic needs explicit conflict policy for user logs before multi-device sync is treated as reliable.

## Expansion Sequence

Only expand after the meal logging loop retains users.

### Phase 1: Meal Logging MVP

- Text meal logging.
- Correction memory.
- Daily macro dashboard.
- History and repeats.
- Privacy/security hardening.

### Phase 2: Trust And Convenience

- Barcode scanning.
- Photo logging evaluation.
- Saved meals and recipes.
- Weekly trend summaries.
- Export.

### Phase 3: Coaching

- Weight trend.
- Adaptive calorie/macro recommendations.
- User-specific adherence insights.
- Coach chat grounded only in user data and approved nutrition principles.

### Phase 4: Fitness Platform

- Real workout plan generation.
- Exercise logging persistence.
- Strength progress tracking.
- Wearable/Health Connect integrations.
- Recovery and adherence insights.

Do not reverse the order. A weak all-in-one app is less compelling than a focused meal logger that users trust.

## International Expansion

US-first is the clearest paid path. India and South Asian food support can be a product wedge, but India-first monetization should be validated separately because revenue per download is materially lower.

For India-facing expansion:

- Localize foods, measurements, and examples.
- Handle DPDP compliance.
- Price in INR separately.
- Consider a freemium or lower annual price.
- Avoid assuming US subscription conversion rates.

## Scalability Decision

Go for closed beta scale after security hardening.

No-go for production scale today. The architecture needs a backend AI proxy, non-destructive migrations, sync policy, and instrumentation before larger distribution.

## Sources

- [Firestore Pricing](https://firebase.google.com/docs/firestore/pricing)
- [Google Cloud Firestore Pricing](https://cloud.google.com/products/firestore)
- [Firestore Billing Example](https://firebase.google.com/docs/firestore/billing-example)
- [Firestore Best Practices](https://firebase.google.com/docs/firestore/best-practices)
- [Firestore Offline Data](https://firebase.google.com/docs/firestore/manage-data/enable-offline)
- [Android Developers - Offline-first data layer](https://developer.android.com/topic/architecture/data-layer/offline-first)
- [Android Developers - WorkManager](https://developer.android.com/reference/androidx/work/WorkManager.html)
- [Android Developers - DataStore](https://developer.android.com/topic/libraries/architecture/datastore)
- [OpenAI - API Key Safety Best Practices](https://help.openai.com/en/articles/5112595-best-practices-for-api-key-safety)
- [OpenAI - Structured Outputs](https://openai.com/index/introducing-structured-outputs-in-the-api/)
