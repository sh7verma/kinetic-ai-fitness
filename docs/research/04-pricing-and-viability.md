# Pricing And Viability

Research date: 2026-07-19

Decision headline: AI inference cost is not the limiting factor. Pricing viability depends on retention, willingness to pay, and trust versus mature paid alternatives.

## Competitor Price Anchors

Current public price anchors in the category:

- MyFitnessPal Premium: $79.99/year or $19.99/month.
- MyFitnessPal Premium+: $99.99/year or $24.99/month.
- Cronometer Gold: $10.99/month or $59.99/year.
- MacroFactor: $11.99/month, $47.99/half-year, or $71.99/year.
- Lose It Premium: $79.99/year.
- Lose It Lifetime: $299.99.
- Noom Weight: examples include $70/month or $209/year, but Noom includes a broader behavior-change program.
- HealthifyMe Ria AI coach: starts at INR 999/month in India.

The practical consumer subscription band for Kinetic is:

- Early beta paid test: $4.99-$7.99/month or $39-$59/year.
- Mature AI logging subscription: $7.99-$11.99/month or $59-$79/year.
- Avoid $15+/month until the app has coaching, analytics, and retention proof.

Kinetic cannot justify premium pricing on meal parsing alone unless the workflow is meaningfully faster and more trusted than existing options.

## OpenAI Cost Model

The current app uses OpenAI `gpt-4o-mini` for meal parsing and nutrition estimation. Official pricing for `gpt-4o-mini` is $0.15 per 1M input tokens and $0.60 per 1M output tokens.

Estimated cost per meal log:

- Simple parser call: 400 input tokens and 120 output tokens, about $0.00013.
- Parser plus uncached nutrition lookup: 900 input tokens and 350 output tokens, about $0.00035.
- High estimate with verbose inputs and output: 2,000 input tokens and 1,000 output tokens, about $0.00090.

At 3 meal logs per day, inference cost is roughly:

- Low: about $0.01/user/month.
- Typical: about $0.03/user/month.
- High: about $0.08/user/month.

Even after adding retries, moderation/safety checks, and occasional re-estimation, model cost should remain small relative to a $5-$10/month subscription.

## Cheaper Or Faster AI Options

Cheaper model options exist and should be evaluated, but not swapped blindly:

- OpenAI announced GPT-4.1 nano pricing at $0.10/1M input and $0.40/1M output.
- OpenAI announced GPT-5 nano pricing at $0.05/1M input and $0.40/1M output.
- Structured Outputs can improve schema adherence versus plain JSON mode.

For Kinetic, the biggest cost and reliability levers are:

- Cache known foods locally and in Firestore.
- Avoid repeated nutrition calls for the same food.
- Use Structured Outputs with strict schemas for parsing.
- Keep prompts short and deterministic.
- Benchmark cheaper models against a fixed real-meal test set before migration.

Do not optimize cost before accuracy and trust. The unit cost is already low.

## Non-AI Operating Costs

Firestore is unlikely to be a near-term cost blocker:

- Free tier includes 1 GiB storage, 50K reads/day, 20K writes/day, 20K deletes/day, and 10 GiB outbound/month.
- Published examples show modest monthly costs even at much larger scale, but usage patterns matter.

Costs can rise from:

- Excessive listener usage.
- Poor indexing.
- Repeated food lookup misses.
- Per-user duplicated food documents.
- Logging verbose request/response payloads.

The larger infrastructure blocker is not cost. It is security: the OpenAI key must not ship inside the Android app.

## Revenue Viability

A plausible model:

- Free tier: limited daily AI logs, manual review, basic daily summary.
- Paid tier: unlimited AI logs, history/repeats, custom targets, weekly trends, export, advanced correction memory.
- Beta pricing: $39-$59/year to test willingness to pay.
- Mature pricing: $59-$79/year if retention and differentiation are proven.

Minimum validation threshold before paid launch:

- 40%+ of beta users log at least 2 real meals on day 1.
- 20%+ of beta users are still logging on day 7.
- 10%+ of beta users complete a paywall or pricing-intent action.
- At least 5 users say they would replace their current tracker, not merely "also use" Kinetic.

These thresholds are intentionally modest because health and fitness app retention is harsh.

## Pricing Decision

Go for pricing validation after product hardening.

No-go for paid public launch today because Kinetic has not proven retention, differentiation, or payment intent. But the economics are not blocked by AI cost. If the MVP retains users, a $5-$10/month subscription can support the inference load.

## Sources

- [OpenAI - gpt-4o-mini model docs](https://developers.openai.com/api/docs/models/gpt-4o-mini)
- [OpenAI - Structured Outputs](https://openai.com/index/introducing-structured-outputs-in-the-api/)
- [OpenAI - GPT-4.1](https://openai.com/index/gpt-4-1/)
- [OpenAI - GPT-5 for Developers](https://openai.com/index/introducing-gpt-5-for-developers/)
- [OpenAI - Prompt Caching](https://openai.com/index/api-prompt-caching/)
- [MyFitnessPal Membership Pricing](https://blog.myfitnesspal.com/myfitnesspal-membership-pricing-tiers/)
- [Cronometer Gold](https://cronometer.com/gold/index.html)
- [MacroFactor Subscriptions](https://help.macrofactorapp.com/en/articles/393-how-macrofactor-subscriptions-and-bundles-work)
- [Lose It Premium Pricing](https://loseit.zendesk.com/hc/en-us/articles/47574703250580-Updates-to-Lose-It-Membership-Pricing)
- [Lose It Lifetime](https://loseit.zendesk.com/hc/en-us/articles/47492421271956-What-is-Lose-It-Lifetime)
- [Noom Plan Pricing](https://www.noom.com/support/faqs/subscription-and-billing/2025/10/noom-plan-pricing-and-what-to-expect/)
- [HealthifyMe Smart Features](https://www.healthifyme.com/smart/features.html)
- [Firestore Pricing](https://firebase.google.com/docs/firestore/pricing)
- [Google Cloud Firestore Pricing](https://cloud.google.com/products/firestore)
- [Firestore Billing Example](https://firebase.google.com/docs/firestore/billing-example)
