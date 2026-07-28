# Risk Assessment

Research date: 2026-07-19

Decision headline: The biggest launch risks are security, privacy/compliance, nutrition accuracy, and retention. AI cost is a secondary risk.

## P0 Launch Blockers

### OpenAI API Key In APK

The current app uses `BuildConfig.OPENAI_API_KEY` directly from the Android client. This is a production no-go.

Risks:

- Extractable key from APK.
- Abuse of the account and unexpected API cost.
- Inability to enforce per-user rate limits.
- No central safety controls.
- No server-side audit trail.

Required fix:

- Move all OpenAI calls behind a backend proxy.
- Issue authenticated app requests to the backend.
- Store OpenAI keys only in server-side secret storage.
- Add rate limits and abuse detection.

### Health App Privacy And Store Declarations

Kinetic handles nutrition, fitness, body metrics, and health-adjacent data. Google Play treats nutrition, weight management, and fitness apps as health apps. The app must satisfy:

- Play Health Apps declaration.
- User Data policy.
- Data Safety form.
- Privacy policy.
- Clear data collection and sharing disclosures.
- Account/data deletion where applicable.

### Placeholder Product Surfaces

The app currently exposes multiple surfaces that are not production complete:

- Workout plan is hardcoded in `PlanViewModel`.
- Exercise logging does not persist as a mature user workflow.
- Stats and Coach surfaces are not strong enough for a public "AI fitness coach" claim.

Google Play's functionality guidance expects apps to be stable, functional, and meaningful. Placeholder-like surfaces create review and user-trust risk.

## Accuracy And Trust Risks

Nutrition estimation is inherently noisy:

- Research on ChatGPT nutrition estimation found many estimates within broad error bands, but those bands are too wide for users who expect precise tracking.
- JAMA Network Open research found AI nutrition estimates often miss strict energy accuracy thresholds and can misestimate macronutrients.
- Image-based LLM studies also show meaningful portion and energy errors, especially with larger portions.
- Real-world food logging is hard because portion size, oil, sauces, preparation method, and hidden ingredients are often missing.

Kinetic must not present estimates as precise truth.

Required mitigations:

- Show editable estimates before save.
- Display assumptions.
- Add confidence levels.
- Flag low-confidence foods.
- Let users correct portions quickly.
- Persist user corrections for future meals.
- Avoid medical or clinical claims.

## Retention Risk

Food logging is a high-friction habit. Research shows adherence declines over time:

- A dietary self-monitoring study found adherence measures declined, and fewer than half of participants were still tracking after week 10.
- Braze reported 2026 Health/Fitness day-30 retention around 3.9% on iOS and 3.4% on Android.
- Other app retention benchmarks show health/fitness day-30 retention can be below 6%.

Kinetic's launch risk is not getting first installs. It is getting people to log again tomorrow and next week.

## Compliance Risks

### India

If Kinetic offers services to users in India, India's Digital Personal Data Protection Act applies to processing of digital personal data, including some processing outside India when connected to offering goods or services in India.

Practical requirements include:

- Clear notice.
- Consent and consent withdrawal.
- Purpose limitation.
- Security safeguards.
- Breach notification duties.
- Erasure and grievance mechanisms.
- Additional child-data duties.

The DPDP Rules were finalized in 2025, making this a current operating concern for India-facing launches.

### United States

HIPAA may not apply to a standalone consumer fitness app unless the company acts as a covered entity or business associate. That does not mean there is no US risk.

Relevant risks:

- FTC Act unfair/deceptive practices.
- FTC Health Breach Notification Rule.
- State privacy laws.
- Misleading health claims.
- Unauthorized sharing of health data for ads.

The FTC's GoodRx enforcement action shows that consumer health data sharing can create major liability even outside traditional HIPAA contexts.

## Technical Risks

Current technical concerns:

- Direct OpenAI calls from client.
- Network logging at BODY level can expose sensitive health and food data in logs.
- Destructive Room migration can wipe user data during upgrades.
- Firestore/local sync rules need a clear source of truth.
- Offline behavior, retries, and conflict handling are not ready for real-world use.
- AI JSON parsing should use strict structured outputs where possible.

## Risk Decision

No-go for public real-user launch today.

Go only for closed beta after:

- API key is removed from the APK.
- Privacy policy and deletion path exist.
- Health app declarations are prepared.
- Placeholder tabs are hidden or finished.
- Estimates are presented as editable and uncertain.
- Logging and retention analytics are in place.

## Sources

- [OpenAI - API Key Safety Best Practices](https://help.openai.com/en/articles/5112595-best-practices-for-api-key-safety)
- [Android Developers - Insecure API Usage](https://developer.android.com/privacy-and-security/risks/insecure-api-usage)
- [OWASP MASWE-0005 - Hardcoded API Key](https://mas.owasp.org/MASWE/MASVS-AUTH/MASWE-0005/)
- [Google Play Console Help - Health Apps](https://support.google.com/googleplay/android-developer/answer/16679511)
- [Google Play Console Help - Health Connect Policy](https://support.google.com/googleplay/android-developer/answer/13996367)
- [Google Play Console Help - User Data](https://support.google.com/googleplay/android-developer/answer/10144311)
- [Google Play Console Help - Functionality](https://support.google.com/googleplay/android-developer/answer/9898783)
- [PubMed - Dietary self-monitoring adherence](https://pubmed.ncbi.nlm.nih.gov/31155473/)
- [PubMed - ChatGPT nutrition estimation](https://pubmed.ncbi.nlm.nih.gov/38194819/)
- [JAMA Network Open - AI nutrition estimation](https://jamanetwork.com/journals/jamanetworkopen/fullarticle/2813295)
- [PubMed - Image-based LLM nutrition estimation](https://pubmed.ncbi.nlm.nih.gov/41081011/)
- [Braze - Mobile app retention](https://www.braze.com/resources/articles/mobile-app-retention-10-tip)
- [Appsflyer - Retention rate](https://www.appsflyer.com/glossary/retention-rate/)
- [India Code - DPDP Act Section 5 Notice](https://www.indiacode.nic.in/show-data?abv=CEN&actid=AC_CEN_45_0_00003_2023-22_1763464807080&orderno=5&orgactid=AC_CEN_45_0_00003_2023-22_1763464807080&sectionId=101271&sectionno=5&statehandle=123456789%2F1362)
- [PIB - DPDP Rules Notified](https://www.pib.gov.in/PressReleasePage.aspx?PRID=2190655&lang=2&reg=3)
- [HHS - Health Apps and HIPAA](https://www.hhs.gov/hipaa/for-professionals/special-topics/health-apps/index.html)
- [FTC - Mobile Health Apps Interactive Tool](https://www.ftc.gov/business-guidance/resources/mobile-health-apps-interactive-tool)
- [FTC - Health Breach Notification Rule Basics](https://www.ftc.gov/business-guidance/resources/health-breach-notification-rule-basics-business)
- [FTC - GoodRx Enforcement Action](https://www.ftc.gov/news-events/news/press-releases/2023/02/ftc-enforcement-action-bar-goodrx-sharing-consumers-sensitive-health-info-advertising)
- [OpenAI - Structured Outputs](https://openai.com/index/introducing-structured-outputs-in-the-api/)
