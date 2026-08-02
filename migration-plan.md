# Kinetic KMP Migration Plan

Last reviewed: 2026-08-02  
Source plan: `/Users/shubham/.claude/plans/piped-painting-kettle.md`

## Objective

Migrate Kinetic from a native Android app to Kotlin Multiplatform + Compose Multiplatform, with shared code running on Android and iOS.

The migration must preserve these existing flows:

- Welcome and authentication
- Onboarding and nutrition target calculation
- Fuel dashboard
- AI meal logging
- Profile and account management

The milestone is functional Android and iOS parity. Pixel-perfect iOS polish and release hardening can follow afterward.

## Status legend

- **Not started** — no implementation work completed.
- **Partial** — some scaffold or groundwork exists, but the phase is not verified or complete.
- **Waiting for input** — implementation depends on an external artifact or user decision.
- **Complete** — implementation and the phase verification gate are finished.

## Current repository state

- `composeApp/` is now the Android application module and contains the Android host, Koin graph, Android SDK adapters, shared Compose UI, migrated models, pure logic, and KMP persistence.
- The old `app/` directory remains only as an unreferenced cleanup shell; it is no longer included by `settings.gradle.kts` and its host sources/resources have moved to `composeApp/src/androidMain`.
- `iosApp/` exists and wraps the Compose framework through `MainViewController`.
- `androidApp/` has been removed from the Gradle build and its transitional launcher sources are deleted.
- `settings.gradle.kts` now includes only `:composeApp`.
- The target architecture is now represented by `composeApp` as the Android+iOS application module; the old `app/` directory still needs final deletion and Gradle cleanup after parity.
- Shared production foundation is partially migrated; the shared iOS feature host and Koin composition root now exist, while native iOS Google Sign-In/Firebase runtime verification remains.
- Current branch: `refactor/kmp`.
- Existing migration commits: `9c3a3b6` (dead-code cleanup) and `7389661` (KMP scaffold).

## Phase status

| Phase | Area | Status | Exit condition |
|---|---|---|---|
| 0 | Baseline and regression reference | **Complete** | Current Android app builds and the full existing flow is manually verified. |
| 1 | Android dead-code and dependency cleanup | **Complete** | Unused screens/dependencies are removed and Android behavior remains unchanged. |
| 2 | KMP/CMP scaffold | **Complete** | The KMP Android application and iOS framework build; the iOS host now starts the shared feature UI, with native service verification tracked in Phase 8. |
| 3–4 | Shared code migration and Koin | **Partial** | Shared models, UI, repositories, mappers, feature controllers, and Android/iOS Koin graphs compile; final platform-auth integration and app-module consolidation remain. |
| 5 | Ktor networking | **Partial** | Android `FoodAIService` now uses Ktor and the live meal-log smoke test passes; iOS key/configuration wiring remains. |
| 6 | Room KMP | **Partial** | Room entities/DAOs and the shared SQLite driver are migrated; iOS database instantiation and CRUD verification remain. |
| 7 | DataStore and unified navigation | **Partial** | Common DataStore and route state exist and Android uses one Activity; iOS persistence/runtime verification and auth-aware routing remain. |
| 8 | Firebase and Google Sign-In | **Partial** | GitLive Auth/Firestore dependencies, native iOS SDK linkage, and the Google Sign-In bridge are wired; the supplied iOS plist is integrated, while live sign-in/data parity remains. |
| 9 | Kermit logging | **Partial** | Kermit is wired and Timber/Android logging is removed; Android logging is verified, while an iOS console capture of the shared startup message remains. |
| 10 | Remove old `app` module and Gradle cleanup | **Partial** | `composeApp` is the only included Gradle module and `androidApp` is removed; the unreferenced legacy `app/` directory and leftover catalog/config cleanup remain. |
| 11 | Final end-to-end verification | **Not started** | Complete fresh-install, persistence, authentication, logging, and profile flows pass on both platforms. |

## Detailed execution plan

### Phase 0 — Establish the Android baseline

**Status: Complete**

- Branch `refactor/kmp` is active.
- `./gradlew :app:assembleDebug` passed successfully.
- On the connected Android emulator, verified Welcome, debug sign-in, all three onboarding screens, Fuel, Log, and Profile.
- Submitted the built-in `2 eggs and a roti` example through the live AI flow; the response included per-item confidence and assumptions.
- Saved the meal and verified Fuel updated to `234` kcal with a meal-history row.
- The crash buffer was empty throughout the smoke test.

**Verification gate:** the current Android app builds and the existing flow works before migration begins.

### Phase 1 — Remove dead Android code

**Status: Complete**

- Completed in commit `9c3a3b6`.
- Removed the unwired Home screen and its ViewModel.
- Removed the empty worker directory.
- Removed the unused Media3, jose4j, WorkManager, and Hilt Work dependencies.
- Rebuilt and smoke-tested the original Android app as part of the Phase 0 baseline.

**Verification gate:** `:app:assembleDebug` succeeds and the Phase 0 behavior is unchanged.

### Phase 2 — Build and prove the KMP scaffold

**Status: Complete**

Existing groundwork:

- Completed in commit `7389661`.
- `composeApp/` contains the shared Compose entry and now packages the Android application as well as the iOS framework.
- `iosApp/` contains the Xcode project and embeds the Compose framework through the Gradle run script.
- Android targets and the iOS simulator/device targets are configured for the current scaffold.

Verified in the scaffold and module-consolidation tasks:

- The original scaffold checkpoint passed with `:androidApp:assembleDebug`, `:composeApp:assembleAndroidMain`, and `:composeApp:linkDebugFrameworkIosSimulatorArm64` before module consolidation.
- The consolidated `:composeApp:assembleDebug` APK installed and launched the real Android host on the connected emulator with an empty crash buffer.
- `ComposeApp.framework` was produced at `composeApp/build/bin/iosSimulatorArm64/debugFramework/ComposeApp.framework`.
- `xcodebuild -project iosApp/iosApp.xcodeproj -list` and Xcode build-settings inspection passed.
- Android Studio project sync updated `.idea/gradle.xml` during the transition.
- The iOS host now launches the shared Welcome, Onboarding, Fuel, AI chat, and Profile flows through the Compose framework; native iOS service verification remains.

Remaining work:

- Complete native iOS platform-service wiring and remove the unreferenced legacy `app/` directory during final cleanup.
- Converted `composeApp` from the Android-KMP library plugin to the AGP 8-compatible KMP + `com.android.application` setup, so it now produces the Android APK while retaining iOS framework targets.
- Moved the Android `Application`, `MainActivity`, platform SDK adapters, Koin module, lifecycle ViewModels, screen adapters, utilities, manifest, Android resources, and `google-services.json` into `composeApp/src/androidMain` / the `composeApp` module.
- Removed the transitional `androidApp` module and removed `:app` and `:androidApp` from `settings.gradle.kts`; the old `app/` directory is intentionally retained only for final cleanup review.
- Verified `:composeApp:assembleDebug`, `:composeApp:compileKotlinIosSimulatorArm64`, Android APK installation/launch with an empty crash buffer, and the Xcode iOS Simulator host build before GitLive’s native Firebase symbols were introduced.
- Applied the Kotlin Native CocoaPods plugin and confirmed Gradle exposes the expected `podInstall`, `podImport`, `podspec`, and framework-linking tasks.
- Normalized `composeApp/build.gradle.kts` so the CocoaPods configuration and KMP source-set dependencies are evaluated at the correct Kotlin block level.
- Replaced the iOS scaffold entry with a Koin-backed shared `IosApp` that creates the common Welcome, Onboarding, Fuel, AI chat, and Profile controllers and renders the shared route/content composables.
- Added the common `GoogleSignInLauncher` boundary and an initial iOS implementation; it has since been replaced by the native Swift Google Sign-In bridge while the user-provided Firebase/OAuth configuration remains pending.
- Re-verified common metadata, iOS Simulator compilation/framework linking, Android compilation, and `:composeApp:assembleDebug` after the real shared iOS host was added.
- Added the complete iOS target set required by the plan: `iosX64`, `iosArm64`, and `iosSimulatorArm64`, including the matching Room KSP compiler target.

**Verification gate:** the consolidated Android application and the Xcode-hosted iOS framework build; feature parity remains a later phase gate.

**Input received:** the iOS Firebase app is registered for bundle ID `com.shverma.kinetic`, and the user-provided `GoogleService-Info.plist` is now present and included in the iOS target. The optional local OAuth override remains available through `Config.local.xcconfig`.

### Phase 3–4 — Move shared code and replace Hilt with Koin

**Status: Partial**

Completed in the current migration task:

- Moved the serializable AI response models, user/profile models, allergy data, meal-item model, `MacrosCalculator`, prompts, and shared theme files into `composeApp/src/commonMain`.
- Made the AI response parser compile in `commonMain` by removing Android logging and isolating meal-time lookup behind `expect`/`actual` implementations for Android and iOS.
- Preserved the platform typography boundary with `InterFamily`; both transitional platform actuals currently use `FontFamily.Default` because the Android KMP library target does not expose the copied Android `R` resource namespace.
- Moved `OnboardingStep`, onboarding header/continue components, and reusable Kinetic components into `commonMain`.
- Extracted `Modifier.kineticGlow` behind `expect`/`actual`: Android keeps the `BlurMaskFilter` implementation, while iOS uses a platform-safe draw fallback until the final visual treatment is tuned.
- Extracted the Welcome visual content and `AuthState` into `commonMain`; the Android `WelcomeScreen` now acts as a thin Koin/lifecycle/platform-context adapter.
- Moved `OnboardingUiState`, the `OnboardingActions` contract, the shared onboarding host, and the biometrics, goals, and results step composables into `commonMain`; the existing Android `OnboardingViewModel` now implements the shared action contract.
- Moved the Profile content/form into `commonMain` behind `ProfileActions` and shared `ProfileEvent`; the Android Profile screen is now a thin Koin adapter.
- Moved the AI-chat presentation models (`UILog`, `UIMeal`, `UIFoodItem`, `ChatMessage`, and `AIChatState`), `ChatType`, platform time, and `LogFoodComponent` into `commonMain`; the Android ViewModel and screen still provide the transitional platform adapters.
- Extracted the AI-chat presentation layer into shared `AIChatActions` and `AIChatContent`; the Android `AIChatScreen` is now only a Koin ViewModel adapter while the chat list, empty state, typing indicator, input, and meal-card rendering compile for Android and iOS.
- Moved the Fuel dashboard presentation into shared `FuelContent`, `FuelActions`, and platform-neutral meal-summary models; the Android screen/ViewModel remain adapters while date-sensitive calculations stay in the Android data layer for now.
- Added common calorie and percentage formatting helpers used by the shared Fuel UI.
- Moved `DietAIRepository` and its implementation into `commonMain` behind the shared `KineticPreferences` contract; Android Koin now provides the common implementation using its transitional DataStore wrapper.
- Moved `LocalFoodDataSource` and `AINutritionDataSource` into `commonMain`; Android Koin supplies their DAO/service dependencies, while Firestore-backed resolution remains platform-specific.
- Moved `FoodResolver` and `FoodRepository` into `commonMain` behind a shared `FoodRemoteDataSource` contract; the Android Firestore adapter and Koin providers remain transitional platform wiring.
- Moved AI-log mapping (`AILogResponse.toFoodLogs` and `toUILog`) into `commonMain`; it now uses the shared resolver, Room entities, UI models, and platform time without Timber or Android APIs.
- Moved the `UserProfileRepository` interface into `commonMain`; the shared GitLive profile repository now owns the common Firestore/DataStore boundary.
- Extracted onboarding state loading, profile persistence, AI target calculation, and step transitions into common `OnboardingController`; Android `OnboardingViewModel` is now a thin Koin/lifecycle/current-user adapter.
- Extracted profile loading, target recalculation, logout, delete-account orchestration, and profile events into common `ProfileController`; Android `ProfileViewModel` now only bridges Koin/lifecycle and Firebase auth operations.
- Extracted AI chat state, Ktor meal logging, common response validation/mapping, Room meal saving, and retry/error states into common `AIChatController`; Android `AIChatViewModel` is now only the Koin lifecycle adapter.
- Extracted Fuel calculations, macro/progress formatting, weekly trend and meal grouping, and repeat-meal persistence into common `FuelController`; Android keeps only the `Calendar`/date-formatting `FuelClock` adapter and lifecycle wrapper.
- Extracted Welcome session restoration, daily food sync, profile lookup, and auth-state transitions into common `WelcomeController`; Android now only adapts Activity-based Credential Manager/Firebase results into common auth types.
- Added the iOS Foundation `FuelClock` implementation for local day/week boundaries and meal-time formatting; the shared Fuel controller now has platform clock adapters on Android and iOS.
- Added the Koin Multiplatform foundation (`koin-core`, `koin-compose`, and `koin-compose-viewmodel`) to `composeApp`; Koin is pinned to 4.1.1 because the project currently uses Kotlin 2.2.20.
- Added a common `KineticPlatformDependencies` contract and Koin module for portable preferences, AI service, food data sources, repositories, resolver, and profile repository; platform SDK setup is still supplied by transitional Android wiring.
- Replaced the transitional Android Hilt graph with Koin: `Application` starts Koin, the Android module registers Firebase Auth/Firestore, DataStore, Ktor, Room DAOs, repositories, and all five feature ViewModels, and the five Compose screen wrappers use `koinViewModel()`.
- Added the shared module as a dependency of the legacy Android app so the moved types remain source-compatible during the incremental migration.
- Removed Hilt/KAPT/Room code generation from the transitional `app` module; Room remains as a runtime API dependency because the host references the shared `AppDatabase` type.
- Verified common metadata compilation, iOS Simulator Kotlin compilation, Android app compilation/assembly, APK installation, app launch, and an empty Android crash buffer after the Koin conversion.

Remaining work:

- Remaining common migration work is now concentrated in the user-provided Firebase iOS configuration and final iOS runtime verification; all five Android feature ViewModels now live in the Android source set of the KMP application and remain thin lifecycle/Koin adapters around common controllers.
- The iOS Koin composition root, shared startup entry point, and native Google Sign-In bridge are now present; consume the user artifacts and complete parity verification, then remove the unreferenced legacy `app/` directory during final cleanup.
- Verify the configured iOS GitLive Auth/Firestore path against the same backend data as Android.
- Keep Android behavior aligned with the Phase 0 baseline.

**Verification gate:** shared code compiles, Android still behaves like the baseline, and the iOS framework links without errors.

### Phase 5 — Replace Retrofit/OkHttp with Ktor

**Status: Partial**

Completed in the current migration task:

- Added Ktor client core, content negotiation, Kotlin serialization JSON, Android OkHttp, and iOS Darwin engine dependencies to `composeApp`.
- Added serializable shared OpenAI transport models, a common `KtorOpenAIClient`, and platform-specific configured `HttpClient` factories.
- Kept the API key out of the shared client constructor’s defaults; callers must provide it explicitly from platform configuration.
- Switched the Android `FoodAIService` and Koin network provider from Retrofit to the shared Ktor client; the Ktor engine type remains hidden inside `composeApp`.
- Moved `FoodAIService` itself into `commonMain`; it uses shared prompts/models/macros and receives the API key from the platform Koin provider, so shared code no longer imports Android `BuildConfig`.
- Verified common metadata, iOS Simulator compilation, Android compilation, Android package assembly, and iOS Simulator framework linking with Ktor resolved.
- Installed the exact build and launched the exported Android entry activity; the crash buffer remained empty.
- Installed the resulting Android APK on the connected test emulator and submitted the built-in `2 eggs and a roti` example; the response returned structured `egg` and `roti` items, High confidence labels, and assumptions. The crash buffer remained empty.
- Removed the unreachable Retrofit OpenAI service/models, Retrofit/OkHttp logging dependencies, and the Retrofit-only network helper after confirming there were no remaining production callers.

Remaining work:

- Keep the Ktor request/response models on `kotlinx.serialization` and the OkHttp/Darwin engine split.
- Provide the OpenAI key through each platform's DI/configuration boundary; Android is wired, and iOS now includes the ignored `Config.local.xcconfig` hook for local configuration.
- Keep secrets out of tracked source; use local ignored configuration for each platform.

**Verification gate:** entering a natural-language meal and receiving structured food items works on Android through Ktor.

### Phase 6 — Migrate Room to Room KMP

**Status: Partial**

Completed in the current migration task:

- Added Room KMP/KSP wiring for Android, iOS device, and iOS Simulator targets with bundled SQLite.
- Moved `AppDatabase`, converters, `FoodEntity`, `FoodLogEntity`, relation models, `FoodDao`, and `FoodLogDao` into `composeApp/src/commonMain`.
- Added the generated `RoomDatabaseConstructor` contract and a shared `createAppDatabase` builder that selects `BundledSQLiteDriver`.
- Moved Android and iOS database-path builders into `composeApp` (`Context.getDatabasePath` on Android and `NSDocumentDirectory` on iOS); the transitional Android Koin module now delegates to the shared builder.
- Updated Android Koin database provisioning to use the shared Room builder, preserve the existing `kinetic_db` filename, and retain destructive-migration fallback during the transition.
- The remaining Android-owned data boundary is authentication/sign-in plus platform configuration; the AI/food-log mappers and Firestore repository boundary are now common.
- Verified Room KSP generation, common metadata, Android compilation/package assembly, and iOS Simulator framework linking.
- Verified `:composeApp:compileKotlinIosX64` and `:composeApp:linkDebugFrameworkIosX64`; Room KSP generation succeeds for the x64 target.
- Installed the resulting APK, launched the exported Android entry activity, and found no crash-buffer entries.

- The database, entities, converters, and DAOs are now in `commonMain`.
- Configure Room KMP and KSP for Android and all iOS targets.
- Use `BundledSQLiteDriver` on both platforms.
- Provide platform-specific database paths through `expect`/`actual` builders.
- Preserve the existing database behavior initially, including the current destructive migration fallback, unless a separate migration policy is approved.

**Verification gate:** food catalog and food-log CRUD persists across app restarts on Android and iOS.

### Phase 7 — Migrate DataStore and unify navigation

**Status: Partial**

Completed in the current migration task:

- Extracted the Fuel/Log/Profile landing shell and bottom navigation into common `KineticLandingContent`; the Android application now supplies only Koin-backed screen adapters and logout routing from `composeApp`.
- Preserved the existing default Fuel tab, AI-chat shortcut, Profile logout behavior, and shared theme while removing the Android-only `NavDestination`/typed-route dependency from the landing shell.
- Migrated the DataStore implementation into common `KineticDataStore` using Preferences DataStore; Android and iOS provide file-path creation through `PreferenceDataStoreFactory`, while the Android Koin `DataStoreHelper` preserves the existing public API and file name.
- Updated DataStore to 1.2.1, which supports KMP Preferences DataStore, and verified common metadata, Android compilation/package assembly, and the iOS Simulator framework link.
- Installed the resulting APK, launched `MainActivity` on the connected emulator, and found no crash-buffer entries.
- Added shared `KineticApp` route state for Welcome, Onboarding, and the landing shell; the Android `MainActivity` in `composeApp` supplies the migrated screen adapters to that common route shell.
- Removed the Android `LandingActivity` and its manifest entry; onboarding, logout, and landing navigation now stay inside one Activity without Intent transitions.
- Rebuilt, installed, launched the new Android entry point, and found no crash-buffer entries.
- The Xcode iOS Simulator host build passed at the pre-Firebase scaffold checkpoint; after adding GitLive Auth/Firestore implementations, the Kotlin framework still links but the direct Xcode project now requires the native Firebase CocoaPods frameworks.

- Verify DataStore and Room persistence through the real iOS host after native Firebase pods are linked.
- Complete auth-aware iOS runtime routing after the native Google Sign-In launcher replaces the temporary error implementation.

**Verification gate:** Android and iOS navigate through the full non-auth flow without Activity transitions or platform-specific navigation duplication.

### Phase 8 — Migrate Firebase and Google Sign-In

**Status: Partial**

- Added GitLive Firebase App, Auth, and Firestore `2.5.0` dependencies to `commonMain`.
- Verified common metadata compilation, Android application assembly, and the iOS Simulator framework link with the GitLive artifacts resolved.
- Added the common `AuthSession` contract for current-user, sign-out, and account-deletion operations.
- Added `GitLiveAuthSession` in `commonMain`; shared auth-session operations now use GitLive Firebase Auth rather than the Android Google-sign-in repository.
- Separated the Android `GoogleAuthRepository` into a platform-specific Credential Manager/Google launcher; onboarding, profile, and welcome session restoration consume `AuthSession`.
- Added common GitLive Firestore adapters for the food catalog and user profiles; Android Koin now provides these shared repositories.
- Added the iOS Koin composition root with platform-owned DataStore path, Room database, Ktor Darwin client, Fuel clock, GitLive repositories, and GitLive auth-session bindings.
- Added an iOS OpenAI configuration hook through `Config.xcconfig` and `Info.plist`; the checked-in value remains blank so the secret is not fabricated or committed.
- Added an optional `Config.local.xcconfig` include after the blank defaults plus a blank example file; local iOS OpenAI and OAuth values can now override those defaults without modifying tracked configuration.
- Verified the configuration-hook change with `git diff --check`, `:composeApp:compileDebugKotlinAndroid`, `:composeApp:assembleDebug`, `:composeApp:compileKotlinIosSimulatorArm64`, and `:composeApp:linkDebugFrameworkIosSimulatorArm64`.
- Added `iosApp/Podfile` for the native `FirebaseCore`, `FirebaseAuth`, and `FirebaseFirestore` frameworks required by the GitLive iOS implementation.
- Applied the Kotlin Native CocoaPods plugin to `composeApp`; the GitLive Firebase modules already register their native Firebase and Google Sign-In pod dependencies, while the host `iosApp/Podfile` lists the required pods explicitly for Xcode linking.
- Installed CocoaPods 1.16.2 and generated `iosApp/Podfile.lock` plus `iosApp/iosApp.xcworkspace`; the workspace contains the `iosApp` target and the Firebase/GoogleSignIn pod project.
- Verified `xcodebuild -list -workspace iosApp/iosApp.xcworkspace` and built the generated workspace successfully with `xcodebuild -workspace iosApp/iosApp.xcworkspace -scheme iosApp -sdk iphonesimulator -configuration Debug -destination 'platform=iOS Simulator,name=iPhone 17,OS=26.2' CODE_SIGNING_ALLOWED=NO build`.
- Native iOS Firebase Core/Auth/Firestore and GoogleSignIn linkage now compiles through the workspace; the first build completed with only CocoaPods inherited-search-path and older pod deployment-target warnings.
- Normalized the iOS app target's `FRAMEWORK_SEARCH_PATHS` entries to inherit the CocoaPods settings; the workspace rebuilt successfully and no longer reports the prior inherited-search-path warning. Remaining warnings are from legacy pod deployment targets and generated pod run-script phases.
- Replaced the iOS placeholder `MainViewController` content with the real shared `IosApp` host and wired all five common feature controllers through iOS Koin.
- Added the common `GoogleSignInLauncher` interface so shared UI/controller code can request platform sign-in without depending on an Android `Activity`.
- Replaced the temporary iOS launcher with a real Kotlin/Swift bridge: the shared launcher suspends on a request notification, the Swift host presents `GIDSignIn`, and the returned Google ID token is exchanged through the shared GitLive `AuthSession`.
- Added Swift-side Firebase initialization from `GoogleService-Info.plist`, Google OAuth client-ID loading from the same plist, native URL callback forwarding, and active-view-controller discovery for the presenting controller.
- Added the blank `GOOGLE_IOS_CLIENT_ID` Xcode configuration/Info.plist hook; the Swift bridge prefers this user-provided value and falls back to `CLIENT_ID` in `GoogleService-Info.plist`.
- Added an iOS Firebase configuration guard with offline-safe Auth, Firestore food, and profile adapters so a build without the user artifact reaches the shared Welcome screen instead of crashing during Koin startup; the GitLive adapters remain selected when the plist is present.
- Verified `:composeApp:compileKotlinIosSimulatorArm64` and `:composeApp:linkDebugFrameworkIosSimulatorArm64`, then verified the generated CocoaPods workspace builds successfully after Swift imported the generated bridge symbol.
- Rebuilt the CocoaPods workspace after adding the OAuth configuration hook; `xcodebuild -list` and the iOS Simulator Debug build pass with only existing CocoaPods deployment-target, generated-script, duplicate-library, and simulator SDK-version warnings.
- Verified the supplied `iosApp/iosApp/GoogleService-Info.plist` is present, passes `plutil -lint`, contains the Firebase/OAuth configuration keys required by the Swift bridge, and is referenced in the iOS target's Copy Bundle Resources phase. The file remains ignored so its credentials are not added to Git.
- Asserted without printing secrets that the plist has non-empty Firebase project, API-key, and OAuth client values and that `BUNDLE_ID` matches `com.shverma.kinetic`; the Xcode target composes its product identifier from the same `BUNDLE_ID` configuration.
- Re-ran CocoaPods installation from the local gem cache; the workspace regenerated successfully with the four declared dependencies and 24 installed pods.
- The follow-up Xcode workspace invocation is currently blocked by the local CoreSimulator service (`CoreSimulatorService connection invalid` / `Connection refused`) and reports the generated workspace as unreadable. This is an environment/tooling failure after plist validation, not a plist validation failure; rerun the workspace build once CoreSimulator/Xcode service is healthy.
- Retried the live iOS simulator gate after the Gradle verification; `simctl` still cannot connect to CoreSimulatorService, so Google Sign-In, Firebase session restore, and Firestore read/write remain unverified in this task.
- Used the already-open Xcode workspace to build the iOS target for the simulator; Xcode reached the install and `Launching Kinetic…` stages, and the resulting `Kinetic.app` has bundle identifier `com.shverma.kinetic`. The UI-control session then lost access to Xcode/Simulator, so the running screen and console output still require a later capture.
- Installed and launched the rebuilt `Kinetic.app` on the booted iPhone 17 / iOS 26.2 simulator; the shared Welcome screen was visible and the process stayed alive without `FIRIllegalStateException`. The simulator still emits the expected Firebase “default app has not yet been configured” warning while the plist is absent.
- Reinstalled and relaunched the rebuilt iOS Simulator app after the OAuth configuration update; `simctl launch` returned a live app PID, with no new startup failure observed.
- Reinstalled and relaunched the latest iOS build after the local-config include correction; `simctl launch` returned PID `58319` and no new launch failure was reported.
- Re-verified the shared Android compile/package and iOS Simulator compile/framework link after the supplied plist integration; `:composeApp:assembleDebug`, `:composeApp:compileKotlinIosSimulatorArm64`, and `:composeApp:linkDebugFrameworkIosSimulatorArm64` passed.
- Captured the relaunched iPhone 17 simulator screen and confirmed the shared Welcome UI renders, including the Kinetic branding and Google sign-in button.
- Re-ran `:composeApp:compileDebugKotlinAndroid` and `:composeApp:assembleDebug` after the iOS bridge/guard changes; both Android tasks passed.
- Routed Android Welcome sign-in through the common `GoogleSignInLauncher` contract using an Android-only `AndroidActivityProvider`; Credential Manager still receives the current Activity only inside the platform adapter.
- Verified the Android launcher refactor with `:composeApp:compileDebugKotlinAndroid`, `:composeApp:assembleDebug`, and `:composeApp:linkDebugFrameworkIosSimulatorArm64`.
- Moved Android Google credential exchange to the shared GitLive `AuthSession`; the Android platform adapter now returns only the Credential Manager ID token and common auth performs `GoogleAuthProvider` credential sign-in.
- Final verification passed with `:composeApp:compileDebugKotlinAndroid`, `:composeApp:assembleDebug`, and `:composeApp:linkDebugFrameworkIosSimulatorArm64` after the GitLive auth and Gradle cleanup.
- Corrected the Android target to JVM 17 because GitLive’s inline serialization APIs require the same JVM target as the Android compilation.
- Kept the Android Credential Manager adapter platform-specific for the Activity-bound token acquisition; GitLive now owns the Android Google credential exchange.
- Verified common metadata compilation, iOS Simulator Kotlin compilation and framework linking, Android compilation, and `:composeApp:assembleDebug` after the shared auth boundary wiring.

Remaining work:

- The common Google sign-in launcher boundary is now used by both Android and iOS; the Android implementation remains platform-specific and Activity-aware only internally.
- Keep Credential Manager implementation on Android.
- The user-provided `GoogleService-Info.plist` is now present and included in the iOS target. Its `CLIENT_ID` is available as the Swift bridge fallback; `Config.local.xcconfig` remains optional for overriding the OAuth client ID and is still absent locally.
- iOS AI meal logging still needs a local `iosApp/Configuration/Config.local.xcconfig` with `OPENAI_API_KEY`; the tracked `Config.xcconfig` intentionally leaves that value blank.
- Run the real iOS Google Sign-In flow and verify the returned GitLive session, profile lookup, onboarding branch, Firestore reads/writes, and logout once CoreSimulatorService is healthy; add the local OpenAI key before testing iOS AI meal logging.

**Verification gate:** a user can sign in on both platforms, restore an existing profile, complete onboarding, and read/write the same Firestore-backed data.

### Phase 9 — Replace Timber with Kermit

**Status: Partial**

- Added Kermit `2.0.4` to `commonMain` and the Android application hosted by `composeApp`.
- Replaced Timber and `android.util.Log` calls in the Application, Google auth adapter, Firebase food adapter, and Android profile repository with Kermit.
- Added common `configureKineticLogging()` using Kermit’s platform log writer and invoked it from both Android `Application` and the iOS Koin entry point.
- Set Kermit’s shared minimum severity explicitly to `Info` so the iOS startup signal is not suppressed by platform defaults.
- Added Kermit startup initialization logging to the iOS Koin composition root so the shared iOS entry has an explicit console signal.
- Changed the iOS startup signal to Kermit’s direct `String` overload and rebuilt the Android/iOS targets; the iOS workspace still builds successfully.
- Added an iOS Kermit `LogWriter` backed by `NSLog` and registered it alongside Kermit’s native platform writer so Xcode receives shared Kermit records through Apple’s console sink.
- Removed temporary diagnostic `NSLog` and direct-writer probe calls from the iOS Koin entry point; production startup now only configures Kermit and emits the shared Kermit startup records.
- Re-verified `:composeApp:compileKotlinIosSimulatorArm64`, `:composeApp:linkDebugFrameworkIosSimulatorArm64`, `:composeApp:compileDebugKotlinAndroid`, and `:composeApp:assembleDebug` after the logging cleanup; all passed with existing compiler deprecation/expect-actual warnings only.
- Reinstalled/launched the Android APK after the logging change; `adb logcat -s Kinetic:V` still reports `I/Kinetic: Application initialized`.
- Reinstalled/launched the iOS build after the logging change; the iPhone 17 simulator screenshot still shows the shared Welcome screen.
- Built common metadata, iOS Simulator code, and the Android APK; installed/launched the APK with an empty crash buffer.
- Re-linked `:composeApp:linkDebugFrameworkIosSimulatorArm64` successfully after the iOS logging entry-point change.
- Rebuilt, installed, and launched the latest APK; `adb logcat` showed `Kinetic: Application initialized` and the crash buffer remained empty.

Remaining work:

- Confirm representative Kermit logs appear in the Xcode console; `simctl launch --console` still showed only an unrelated UIKit warning and not the shared Kermit startup message, so this gate remains unverified.

**Verification gate:** no shared code depends on Timber or Android-only logging APIs.

### Phase 10 — Remove the old Android module

**Status: Partial**

- Converted `composeApp` into the Android application module while retaining its iOS framework target.
- Removed `:app` and `:androidApp` from `settings.gradle.kts`; deleted the transitional `androidApp` launcher module.
- Removed the unreferenced Android-native Firebase repository classes, native `FirebaseAuth` Koin binding, direct Android Firebase Auth/Firestore dependencies, and the unused Firebase BOM catalog entries; GitLive now owns the shared Firebase boundary.
- Rebuilt, installed, and launched `composeApp-debug.apk` on the connected emulator after the cleanup; the Android crash buffer was empty.
- Audited the remaining legacy surface: `settings.gradle.kts` includes only `:composeApp`; the old `app/` directory contains only its stale build/test shell, while README references were updated to the KMP module and current Ktor/Koin/Kermit/GitLive stack.
- Delete the now-unreferenced legacy `app/` directory only after Firebase/iOS parity and release configuration are verified.
- Remove unused Hilt, Retrofit, Room Android-only, Firebase Android-only, and other obsolete Gradle entries.
- Re-check unused Coil and release configuration before deleting anything still needed.

**Verification gate:** the KMP module builds the Android app and the iOS framework without the legacy Android module.

### Phase 11 — Final cross-platform verification

**Status: Not started**

Run on both Android and iOS:

- Fresh install.
- Welcome and Google Sign-In.
- New-user onboarding and target calculation.
- Existing-user session restore.
- AI meal logging and Fuel total update.
- Profile editing, logout, and re-login.
- Kill and relaunch persistence check for DataStore and Room.
- iOS lifecycle, keyboard, safe-area, and rotation checks where applicable.

**Verification gate:** all current product flows work on both platforms with no known migration-blocking regressions.

## Main risks and dependencies

- The consolidated `composeApp` application/framework layout is now in place; the remaining module risk is deleting the unreferenced legacy `app/` directory only after parity verification.
- iOS Google Sign-In is the highest-uncertainty integration and should be prototyped before the full Firebase migration.
- The supplied Firebase iOS plist is now available for Phase 8; the remaining external configuration is the local OpenAI key and any optional OAuth override.
- The OpenAI key must remain outside tracked source for both platforms.
- Room currently uses destructive migration fallback; preserving it is part of the initial migration plan, but it remains a production data-loss risk.
- Every phase requires an Android regression check; iOS verification becomes mandatory once real shared UI and services are wired.
