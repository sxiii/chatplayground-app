# Project Vision: ChatPlayground Android Wrapper
|
|## Key Goals
|1. Provide a native-like experience for interacting with ChatPlayground on Android.
|2. Ensure secure handling of session data and credentials.
|3. Support offline functionality where applicable.
|4. Maintain high performance and responsiveness.
|5. Iterate on ideas and improvements for the app.
|6. Enhance UI/UX based on user feedback.
|7. Integrate analytics for performance monitoring.
|
|## Todo Tasks (Backlog)
|- [x] **[HIGH]** Investigate CI JaCoCo report failure: identify why `jacocoTestReport` task fails despite successful `testDebugUnitTest` and generate/upload `.exec` files as artifacts to diagnose.
|- [x] **[HIGH]** Add explicit artifact upload step for `.exec` files in CI to simplify debugging.
|- [x] **[HIGH]** Implement a bounded WebView load-failure retry policy (main-frame only, exponential backoff, unit tested) and wire it into `MainActivity`. (DONE this cycle)
|- [x] **[HIGH]** Fix JaCoCo graceful skip: ensure `jacocoTestReport` actually skips when no `.exec` files are present in any build directory (current implementation fails to skip correctly).
|- [x] **[HIGH]** Add a dependency-verification step in CI to ensure dependency integrity and security.
- [x] **[HIGH]** Surface real dependency-resolution failures from the now-non-blocking `Verify Dependencies` step: grep `gradle_dependencies.log` for `Could not resolve`/`FAILED` → `::warning` annotation + summary line, so continue-on-error doesn't mask breakage.
- [x] **[MEDIUM]** Add a CI preflight step that validates `gradle.properties` contains `android.useAndroidX=true` and `android.enableJetifier=true` before `assembleDebug`, catching the historical missing-gradle.properties build failure early.
- [x] **[HIGH]** Implement automated release tagging workflow on main branch merges.
- [x] **[HIGH]** Add a linting gate in CI: fail CI when lint-results.html contains warnings; verify on next run.
- [x] **[MEDIUM]** Add `actions/cache` for `android-sdk` packages to reduce cold-start CI time.
- [ ] **[HIGH]** Add GitHub Actions caching for Android SDK packages and Gradle dependencies to reduce cold-start CI time.
- [ ] **[MEDIUM]** Add Android manifest audit test covering required permissions (`INTERNET`, `ACCESS_NETWORK_STATE`) so future edits cannot silently break networking.
|- [ ] **[MEDIUM]** Give `NetworkMonitor` lifecycle cleanup (`stopObserving`/callback unregistration) so repeated activity creation cannot leak `ConnectivityManager` callbacks.
|- [ ] Standardize AGP/Gradle/Kotlin versions to ensure CI stability.
|- [ ] Add a CI documentation page describing the build/test/lint steps and how to read failure diagnostics.
|- [ ] Implement `NetworkMonitor.checkConnectivity` correctly and ensure `NetworkMonitorTest.kt` passes. (BLOCKED: Local toolchain missing JDK/Gradle. Validation must be CI-only.)
|- [ ] Implement a test that simulates network state transitions using Robolectric `ShadowConnectivityManager` to verify `NetworkMonitor.observe` logic. (BLOCKED: Local toolchain missing JDK/Gradle. Validation must be CI-only.)
|- [ ] Add CI check for lint warnings and deprecations.
|- [ ] Implement automated release tagging workflow on main branch merges.
|- [ ] Add Gradle build cache (`actions/cache`) configuration for Android dependencies in GitHub Actions workflow.
|- [ ] Add test coverage report generation task (`jacoco`) in Gradle build.
|- [ ] Implement robust error handling and retry mechanism for WebView load failures.
|- [ ] Add unit test for SecureStorage encryption edge cases and fallback mechanisms.
|- [ ] Add Espresso UI tests for WebView.
|- [ ] Add code formatting enforcement (ktlint).
|- [ ] Implement user feedback mechanism for UI/UX improvements.
|- [ ] Add dependency verification step in the CI pipeline to ensure security.
|- [ ] Implement Dark Mode support.
|- [ ] Add a settings screen for toggling features.
|- [ ] Implement Biometric Authentication for enhanced security.
|- [ ] Add Network Monitoring & Auto-reconnect handling.
|- [ ] Implement Screen Reader accessibility improvements for the WebView content.
|- [ ] Add basic performance profiling for initial app load.
|- [ ] Add automated UI integration tests using Espresso.
|- [ ] Pin and bump the AGP / Gradle / Kotlin versions together in a single verified step (currently AGP 8.2.0 + Gradle 8.5 + Kotlin 1.9.0).
|- [ ] Add an explicit CI step that asserts the generated APK artifacts exist before upload (fail fast with a clear message).
|- [ ] Add `actions/cache` for `android-sdk` to speed up builds.
|- [ ] Run `./gradlew lint` in CI and gate on lint results.
|- [ ] Add lint warnings gate: Fail CI if `lint-results.html` contains any warning.
|- [ ] Verify APK artifact existence: Ensure debug and release APK files are produced.
|- [ ] Upload JaCoCo coverage report: Add CI step to generate and publish coverage as artifact.
|- [ ] Pin and document the `android-actions/setup-android` action version (currently `@v3`); add a cache step for `platforms;android-34` and `build-tools;34.0.0` to reduce cold-start time.
|- [ ] Add `actions/cache` for `~/.gradle` dependency cache in addition to the built-in `actions/setup-java` Gradle cache.
|- [ ] Add an `actions/cache` entry for `android-sdk` packages to speed up builds.
|- [ ] Add unit tests for SecureStorage implementation (covers encryption, retrieval, and error handling).
|- [ ] Investigate alternative CI failure causes if upgrade fails.
|
|## Done
|- [x] Fix GitHub Actions build failure (Gradle task failing after Android SDK setup).
|- [x] Disable JaCoCo test report generation temporarily to unblock CI builds.
|- [x] Adjust MainActivityTest to simplify WebViewClient assertion and avoid NoSuchMethodError.
|- [x] Migrate `actions/setup-java` from v4 to v5.
|- [x] Pin CI to JDK 17.
|- [x] Add CI step that uploads JUnit XML test results as an artifact.
|- [x] Add unit test for URL validation and deep linking support.
|- [x] Add performance logging instrumentation for WebView page load events.
|- [x] Upgrade `androidx.webkit:webkit` to latest version.
|- [x] Investigate CI failure: Upgrade JDK to 21.
|- [x] Fix Gradle build configuration error: removed misplaced `pluginRepositories` block.
|- [x] Add Android manifest permission audit test (INTERNET, ACCESS_NETWORK_STATE).
|- [x] Document CI debugging steps in a new markdown page.
|- [x] Added minimal layout and strings resources to fix Android build failure.
|- [x] Consolidate duplicate tasks in checklist.
|- [x] Document local-toolchain prerequisites (CI-only).
