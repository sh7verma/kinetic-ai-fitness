# Kinetic — AI-Powered Fitness Tracker

An Android app that lets users log meals through natural language, with GPT-4o parsing the input into structured nutrition data in real time. Built as a full-stack portfolio project demonstrating LLM integration in a production-grade Android architecture.

> "I had 2 eggs and a roti" → instant calorie + macro breakdown, saved to your daily log.

---

## Demo

<!-- Add a GIF here: screen record the AI Chat → meal logged flow, ~15 seconds -->
<!-- Suggested tool: Android Studio's built-in screen recorder, then convert with ffmpeg or ezgif.com -->

| AI Meal Logging | Nutrition Dashboard | Diet Plan |
|:-:|:-:|:-:|
| *(screenshot)* | *(screenshot)* | *(screenshot)* |

---

## What it does

- **Natural language meal logging** — type what you ate, GPT-4o extracts food items, estimates portion weights in grams, and returns calories + macros (protein, carbs, fat)
- **Personalized targets** — onboarding collects age, weight, height, goal, and activity level; a separate AI call calculates your daily calorie and macro targets using a nutrition strategy prompt
- **Diet plan** — structured meal plan generated from your profile, with per-meal macro breakdowns
- **Fuel dashboard** — daily calorie ring, macro progress bars, and meal history
- **Workout plan** — auto-generated push/pull/legs split with set/rep tracking
- **Progress tracking** — weight log with trend chart
- **Firebase auth** — Google Sign-In via Credential Manager

---

## Architecture

Clean Architecture with three layers: UI → Domain (repositories) → Data (Room + Firebase + OpenAI).

```
com.shverma.kinetic/
├── data/
│   ├── network/          # OpenAI Retrofit service + prompt templates
│   ├── local/            # Room database (Food, FoodLog entities + DAOs)
│   ├── repository/       # Business logic, coordinates AI + local + remote
│   ├── model/            # Data classes, AI response models
│   ├── auth/             # Firebase Auth / Google Sign-In
│   ├── preference/       # DataStore for user profile persistence
│   └── worker/           # Background sync tasks
├── di/                   # Hilt modules (NetworkModule, DatabaseModule, etc.)
├── ui/
│   ├── aichat/           # Conversational meal logging screen
│   ├── fuel/             # Calorie + macro dashboard
│   ├── diet/             # Diet plan and food log
│   ├── plan/             # Workout plan
│   ├── stats/            # Progress charts
│   ├── onboarding/       # Profile + goal setup flow
│   └── theme/            # Kinetic design system tokens
└── utils/
```

**State management:** Kotlin Flow + ViewModel  
**DI:** Hilt (SingletonComponent)  
**Async:** Coroutines throughout; no RxJava

---

## LLM Integration

All AI logic lives in `data/network/` and `data/repository/`.

### How meal logging works

```
User types "2 eggs, 1 roti"
  → AIChatViewModel.sendMessage()
  → DietAIRepository.logFood()
  → FoodAIService builds an OpenAI chat request:
      • system prompt: strict JSON schema, portion size rules, common Indian food gram estimates
      • user message: the input + full user profile (age, weight, goal, activity)
  → GPT-4o-mini returns structured JSON
  → Parsed into AILogResponse (meal type, food items, grams, confidence score)
  → Converted to UI state → user sees breakdown
  → User taps "Save" → inserted into Room
```

### Prompt design

Three purpose-built system prompts in `AIPrompts.kt`:

| Prompt | Purpose | Output |
|--------|---------|--------|
| `logMealSystemPrompt` | Parse natural language meals | `AILogResponse` — food items with gram weights |
| `getNutritionPer100gPrompt` | Batch nutrition lookup | Calories + macros per 100g per food item |
| `initStrategyPrompt` | Personalized macro calculation | Daily calorie target + protein/carb/fat split |

Key design choices:
- JSON-only responses enforced in the system prompt
- User profile injected as context in every meal-logging request so portion estimates are personalized
- Confidence scoring per food item; fallback nutrition values kick in below threshold
- Validation layer on parsed values (calorie range sanity checks, null guards)

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin |
| UI | Jetpack Compose, Material 3 |
| Navigation | Compose Navigation |
| AI | OpenAI API (GPT-4o-mini) via Retrofit |
| Local DB | Room |
| Remote DB | Firebase Firestore |
| Auth | Firebase Auth, Google Credential Manager |
| DI | Hilt |
| Async | Kotlin Coroutines, Flow |
| Image loading | Coil |
| Logging | Timber |
| Serialization | kotlinx.serialization, Gson |
| Preferences | DataStore |

---

## Setup

### Prerequisites
- Android Studio Hedgehog or later
- JDK 17
- An OpenAI API key (GPT-4o or GPT-4o-mini access)
- A Firebase project with Auth and Firestore enabled

### Steps

1. Clone the repo:
   ```bash
   git clone https://github.com/sh7verma/kinetic-ai-fitness.git
   ```

2. Add a `local.properties` file in the project root (already in `.gitignore`):
   ```properties
   OPENAI_API_KEY=sk-...your-key-here...
   ```

3. Add your `google-services.json` to `app/`:
   - Open [Firebase Console](https://console.firebase.google.com) → your project → Project Settings
   - Download `google-services.json` and place it at `app/google-services.json`
   - This file is in `.gitignore` and is **not included in the repo** — you must supply your own

4. Build and run on an emulator or physical device (API 26+).

> **Note on API key security:** The key is read via `BuildConfig` at compile time. For a production release, replace the direct OpenAI call with a backend proxy so the key is never embedded in the APK.

---

## Design System

The app uses a custom design language called **Kinetic Precision**:
- Background: obsidian `#0E0E0E`
- Primary accent: high-voltage lime `#CAFD00`
- Secondary accent: neon cyan `#00E3FD`
- Typography: Lexend (display), Space Grotesk (labels)
- Components: `KineticProgressBar`, `KineticDataCard`, `KineticTopAppBar`

---

## Key Files

| File | What it does |
|------|-------------|
| `FoodAIService.kt` | Orchestrates all OpenAI API calls |
| `AIPrompts.kt` | System prompt templates |
| `DietAIRepository.kt` | Combines AI + Room + Firestore for food logging |
| `AIChatViewModel.kt` | UI state for the chat screen |
| `NetworkModule.kt` | Hilt wiring for Retrofit + OkHttp |
| `MacrosCalculator.kt` | Converts AI strategy response to daily targets |

---

## License

MIT
