# Kinetic Redesign Prompt

_Paste the section below into a design tool (Figma AI, v0, Galileo, etc.). Context above the divider is for you; only what's below the `---` is meant to be pasted._

## Why this redesign

Research ([docs/research/](research/)) concluded Kinetic should stop presenting itself as a full "AI fitness coach" and narrow to one job: a fast, trustworthy AI meal logger for people who already track calories and macros ([[project-target-segment]]). The current "Kinetic Precision" visual identity — obsidian background, high-voltage lime, neon cyan, aerospace-HUD styling — was built for a broader gamified fitness-coach pitch. It doesn't match a product whose main value is "trust my numbers, correct them fast." This redesign moves the visual language toward the calm, clinical precision of Cronometer/MacroFactor instead, and restructures the screens around the meal-logging loop, cutting everything that isn't real yet (see `docs/APP_UPDATE.md`).

---

## PROMPT — paste below this line

You are redesigning an Android app called **Kinetic**, currently a Jetpack Compose app with Material 3. Redesign its UI/UX and visual system from scratch. Produce high-fidelity mobile screens (portrait, Android, ~412×915dp) plus a small design system reference sheet.

### Product

Kinetic lets a user type what they ate in plain English ("2 eggs and a roti," "chicken curry with rice, medium bowl") and an AI model returns a structured breakdown: food items, gram estimates, calories, and macros (protein/carbs/fat). The user reviews and corrects the estimate, then saves it to a daily log. It also computes personalized daily calorie/macro targets from the user's profile (age, weight, height, goal, activity level).

### Who it's for

Fitness-serious meal-loggers, not casual wellness beginners: people 20–40 who already understand calories and macros, are mid-switch from (or dissatisfied with) MyFitnessPal, Cronometer, MacroFactor, Lose It, or Cal AI, and are frustrated by slow manual portion entry and database search — especially for home-cooked or mixed meals that don't map cleanly to a packaged-food database. They will judge this app against those competitors within the first session. Secondary: people eating South Asian / mixed home-cooked meals that Western food databases handle poorly.

### The one job this redesign must nail

**Trust, at a glance, with minimal correction effort.** Every AI-generated estimate must visibly communicate: what was assumed (serving size, cooking method, hidden ingredients like oil/sauce), how confident the model is, and how to fix it in the fewest taps possible. This is the single most important design problem in the app — treat it as a first-class UI pattern, not a tooltip.

### Design direction: calm, clinical trust — not a HUD

Move away from gamified "performance dashboard" styling entirely. Target the emotional register of a precise, calm nutrition instrument — closer to Cronometer/MacroFactor/Apple Health than to a fitness-tracker HUD or a game.

- **Palette**: a quiet neutral base (soft off-white/warm paper tone for light mode, a muted charcoal — not pure black — for dark mode), one confident, restrained brand color used sparingly for primary actions and brand moments (think deep forest green, muted teal, or a desaturated indigo — avoid neon, avoid high-voltage lime/cyan). No glow effects, no glassmorphism blur, no "gamer" gradients.
- **Semantic confidence colors**: a distinct, consistent 3-tier system for AI confidence — e.g., calm green for high confidence, warm amber for medium/needs-a-glance, muted red-orange for low-confidence/needs-review. These should read instantly and consistently everywhere confidence appears.
- **Typography**: clean, highly legible sans-serif for both display and body (a single well-paired type family, or two at most). Numbers (calories, grams, macros) should be the visual focus — give them clear hierarchy — but skip the oversized "56sp bravado" display style; prioritize scannability over drama.
- **Elevation**: soft, subtle shadows or thin neutral borders for card separation — no tonal-glow layering, no colored shadows.
- **Density**: generous whitespace, calm grid, symmetric and predictable layouts. This is the opposite of "editorial asymmetry" — predictability builds trust here.
- **Motion**: minimal, functional only (state transitions, confirmation feedback) — no motion for spectacle.
- Support both light and dark mode; do not assume dark-only.

### Screens to design (in priority order)

Only these should exist in the redesign. Do not design Workout Plan, Exercise Logging, AI Coach, Stats, or a generic Details screen — they're intentionally cut from this version of the product (unfinished/placeholder in the current app; see `docs/APP_UPDATE.md`). If a bottom nav is used, it should only contain items for the screens below.

1. **Meal logging / chat screen** (the core screen — spend the most design effort here)
   - Natural-language text input for describing a meal, with a lightweight "thinking" state while the AI parses.
   - Parsed result shown as an editable card per food item: name, gram estimate, calories, macros.
   - **Confidence indicator per food item** (not just per meal) using the semantic color system above.
   - **Visible assumptions** per item — e.g., "assumed: 1 medium roti (~40g), no ghee" — shown as a small inline chip or expandable line, not hidden.
   - Fast inline correction: tap to adjust grams/quantity or swap the food, without leaving the screen or opening a separate editor.
   - Clear "Save" action, plus a lightweight way to flag "this estimate looks wrong" that feels distinct from just editing the numbers.
   - Empty/first-use state that teaches the interaction in one glance.

2. **Daily dashboard (Fuel)**
   - Today's calorie and macro totals vs. targets, updating immediately after a save.
   - Recent meal history for the day, with each entry showing its confidence level at a glance.
   - **Quick repeat**: one-tap re-log of a previously logged meal/food, prominent — this is a top retention lever, not a buried feature.
   - Simple day-to-day navigation (yesterday/today), no complex charting yet.

3. **Onboarding** (trimmed)
   - Only what's needed to compute a macro target: basic biometrics (age, weight, height), goal, activity level.
   - End state: a clear "here are your daily targets" screen with a brief, honest explanation of how they were calculated — reinforces trust from message one.
   - Cut any step that doesn't feed the macro calculation or improve first-meal-logging activation (e.g., don't ask about cuisine preference or workout commitment days in v1).

4. **Profile**
   - Current targets and biometrics, editable.
   - Sign out and delete-account actions (delete should be clearly separated/de-emphasized from routine actions, with a confirmation step).

5. **Auth / Welcome**
   - Minimal: value proposition in one line ("Log your meals in plain English, trust the numbers"), Google Sign-In.

### Explicit non-goals for this redesign

- No workout, exercise-logging, AI-coach, or advanced-stats screens.
- No barcode scanning, photo logging, or voice input UI — text-only for this version.
- No social/streak/gamification elements.
- No claims of medical precision — include a small, honest "estimates, not medical advice" acknowledgment somewhere reachable (e.g., footer/info, not a blocking modal).

### Deliverables

- A short design-system reference (color tokens for light + dark, type scale, spacing scale, card/button/input styles, the 3-tier confidence color system).
- High-fidelity screens for all five flows above, in both light and dark mode for the core meal-logging and dashboard screens at minimum.
- One annotated screen showing the confidence + assumptions + correction interaction in detail, since that's the product's core differentiator.


PLAN


                                                                    color). This is the screen the whole redesign hinges on.

Phase 4 — Onboarding: Collapse OnboardingStep from 5 steps to 3 (Biometrics → Workout Goal + Activity → Targets result), removing MEAL_SETUP/FLAVOR_PROTOCOL steps and the commitmentDays/equipment/selectedDietTypes/selectedAllergies/selectedCuisines state fields from OnboardingViewModel (confirmed unused by the macro calculation). Delete OnboardingMealSetupScreen.kt, OnboardingFlavorProtocolScreen.kt, and the equipment/commitment UI inside OnboardingWorkoutSetupScreen.kt. Rebuild the remaining screens plus OnboardingComponents.kt off KineticTheme tokens (currently overridden almost everywhere with hardcoded fonts/colors per exploration).

Phase 5 — Fuel Dashboard: Add the meal-history list and quick-repeat affordance the mockup requires — FuelViewModel already computes items: List<FuelItem> and loggedMealNames but FuelScreen.kt never renders them (dead FuelItemRow/MiniCard composables exist unused). Quick-repeat needs new logic: re-inserting a previous FoodLogEntity set for a repeated meal without re-calling the AI. Restyle the daily-energy/macro cards to the sampled tokens (spacing/shape/typography already exist; this screen already uses KineticTheme throughout, so it's a layout/feature change more than a re-skin).

Phase 6 — Profile: Add the "Edit targets" functionality the mockup shows (currently profile fields are read-only with no path back into target editing) and restyle to sampled tokens (already mostly on KineticTheme, minimal re-skin needed).

Cleanup pass (after Phase 6): once no kept screen references LexendFamily/PlusJakartaSansFamily/SpaceGroteskFamily/BarlowCondensedFamily or the legacy Meal*/lime/cyan Color.kt constants, delete them. Revisit the flagged AIResponse.parse() workout/diet_plan branches and WorkoutPlanResponse at that point too.

Notes for whoever picks up each phase

- Design source of truth: docs/redesign/kinetic-redesign-v1.html (open via a local python3 -m http.server + browser screenshot, as done earlier this session — file:// URLs aren't directly navigable by the browser automation tool).
- Theme tokens are already built — use KineticTheme.colors/KineticTheme.typography/KineticSpacing/KineticShape/ConfidenceTier rather than introducing new hardcoded values.
- After each phase, run ./gradlew :app:compileDebugKotlin and manually exercise the changed screen on-device/emulator before moving to the next phase — do not batch multiple screen rebuilds into one unverified pass.