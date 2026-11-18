# Project Vision: ChatPlayground Android Wrapper

The objective of this project is to create a robust, secure, and user-friendly Android application wrapper for the ChatPlayground web application.

## Key Goals
1. Provide a native-like experience for interacting with ChatPlayground on Android.
2. Ensure secure handling of session data and credentials.
3. Support offline functionality where applicable.
4. Maintain high performance and responsiveness.
5. Iterate on ideas and improvements for the app.
6. Enhance UI/UX based on user feedback.
7. Integrate analytics for performance monitoring.

- [ ] Add comprehensive unit tests for NetworkMonitor reconnection and lifecycle handling.
- [x] Add Android manifest permission audit test (INTERNET, ACCESS_NETWORK_STATE).
- [x] Document CI debugging steps in a new markdown page and link from vision (see `.autonomy/ci_debugging.md`).
- [ ] Enable Gradle build cache for Android SDK via actions/cache in CI workflow.
1. [ ] Add a bounded WebView load-failure retry policy (main-frame only, exponential backoff, unit tested) and wire it into `MainActivity`.
2. [ ] Give `NetworkMonitor` lifecycle cleanup (`stopObserving`/callback unregistration) so repeated activity creation cannot leak `ConnectivityManager` callbacks.
3. [ ] Show an offline/error state in the app instead of a blank WebView, with a manual retry action.
4. [ ] Add an Android manifest audit test covering required permissions (`INTERNET`, `ACCESS_NETWORK_STATE`) so future edits cannot silently break networking.
5. [ ] Add a CI documentation page describing the build/test/lint steps and how to read failure diagnostics.

## Task checklist
- [x] Fix GitHub Actions build failure (Gradle task failing after Android SDK setup; verbose logs now enabled via `--info --debug` to pinpoint the root cause).
- [x] Fix `MainActivityTest.kt` compilation errors in CI. Root cause (2026-11-18): the committed test called `assertTrue(...)` without importing it, so the whole unit-test source set failed to compile. The test step fails while `assembleDebug` passes — that asymmetry is the signature of a test-only compile error.
- [ ] Implement `NetworkMonitor.checkConnectivity` correctly and ensure `NetworkMonitorTest.kt` passes. (BLOCKED: Local toolchain missing JDK/Gradle. Validation must be CI-only.)
- [ ] Implement a test that simulates network state transitions using Robolectric `ShadowConnectivityManager` to verify `NetworkMonitor.observe` logic. (BLOCKED: Local toolchain missing JDK/Gradle. Validation must be CI-only.)
- [ ] Consolidate duplicate tasks in checklist.
- [ ] Document local-toolchain prerequisites (CI-only).
- [ ] Add CI check for lint warnings and deprecations.
- [ ] Implement automated release tagging workflow on main branch merges.
- [ ] Add Gradle build cache (`actions/cache`) configuration for Android dependencies in GitHub Actions workflow.
- [ ] Add test coverage report generation task (`jacoco`) in Gradle build.
- [ ] Implement robust error handling and retry mechanism for WebView load failures.
- [ ] Add unit test for SecureStorage encryption edge cases and fallback mechanisms.
- [ ] Add Espresso UI tests for WebView.
- [ ] Add code formatting enforcement (ktlint).
- [ ] Implement user feedback mechanism for UI/UX improvements.
- [ ] Add dependency verification step to the CI pipeline to ensure security.
- [ ] Implement Dark Mode support.
- [ ] Add a settings screen for toggling features.
- [ ] Implement Biometric Authentication for enhanced security.
- [ ] Add Network Monitoring & Auto-reconnect handling.
- [ ] Implement Screen Reader accessibility improvements for the WebView content.
- [ ] Add basic performance profiling for initial app load.
- [ ] Add automated UI integration tests using Espresso.
- [x] Migrate `actions/setup-java` from v4 to v5 (workflow now uses `actions/setup-java@v5` with Temurin JDK 17).
- [ ] Pin and bump the AGP / Gradle / Kotlin versions together in a single verified step (currently AGP 8.2.0 + Gradle 8.5 + Kotlin 1.9.0).
- [ ] Add an explicit CI step that asserts the generated APK artifacts exist before upload (fail fast with a clear message).
- [ ] Add `actions/cache` for `android-sdk` to further speed up builds.
- [ ] Run `./gradlew lint` in CI and gate on lint results.
- [ ] Add unit tests for SecureStorage implementation (covers encryption, retrieval, and error handling).
- [x] Upgrade `androidx.webkit:webkit` to latest version.
- [ ] Investigate alternative CI failure causes if upgrade fails.
- [x] Investigate CI failure: Upgrade JDK to 21 (AGP 8.x requirements).
- [x] Fix Gradle build configuration error: removed misplaced `pluginRepositories` block from root `build.gradle` (which belongs in `settings.gradle`).
- [ ] Add CI check for lint warnings and deprecations.
- [ ] Implement automated release tagging workflow on main branch merges.
- [ ] Add Gradle build cache (`actions/cache`) configuration for Android dependencies in GitHub Actions workflow.
- [ ] Add test coverage report generation task (`jacoco`) in Gradle build.
- [ ] Implement robust error handling and retry mechanism for WebView load failures.
- [x] Pin CI to JDK 17: Kotlin 1.9.0's JVM target does not support Java 21 (`Unknown Kotlin JVM target: 21`). Upgrade Kotlin (and AGP) together before moving CI to JDK 21. (Verified: `.github/workflows/build.yml` sets Temurin 17.)
- [x] Add CI step that uploads JUnit XML test results as an artifact, so per-test failures are inspectable from the run without needing admin log access.
- [x] Add unit test for URL validation and deep linking support.
- [x] Add performance logging instrumentation for WebView page load events.
- [x] Fix `MainActivityTest.kt` compilation errors in CI. Root cause (2026-11-18): the committed test called `assertTrue(...)` without importing it, so the whole unit-test source set failed to compile. The test step fails while `assembleDebug` passes — that asymmetry is the signature of a test-only compile error.
- [ ] Implement `NetworkMonitor.checkConnectivity` correctly and ensure `NetworkMonitorTest.kt` passes. (BLOCKED: Local toolchain missing JDK/Gradle. Validation must be CI-only.)
- [ ] Implement a test that simulates network state transitions using Robolectric `ShadowConnectivityManager` to verify `NetworkMonitor.observe` logic. (BLOCKED: Local toolchain missing JDK/Gradle. Validation must be CI-only.)
- [ ] Consolidate duplicate tasks in checklist.
- [ ] Document local-toolchain prerequisites (CI-only).

## Ranked improvement suggestions (2026-11-18)

- [ ] **Add lint warnings gate**: Fail the CI if `lint-results.html` contains any warning.
- [ ] **Verify APK artifact existence**: Add a CI step that asserts the debug and release APK files were produced.
- [ ] **Add CI step to upload test coverage report** via JaCoCo and make it searchable as an artifact.
Ranked by user impact and verifiability. Each is small enough for one cycle.

- [ ] Add lint warnings gate: Fail CI if `lint-results.html` contains any warning.
- [ ] Verify APK artifact existence: Ensure debug and release APK files are produced.
- [ ] Upload JaCoCo coverage report: Add CI step to generate and publish coverage as artifact.


2. **Proper manifest permission audit test.**
   Prevents future edits from silently removing networking permissions.
   *Acceptance:* test fails if either permission is deleted; runs in the
   `test` task (no emulator required).

3. **Real `NetworkMonitor` reconnection test.**
   Current tests only cover construction, the offline default and lifecycle
   idempotency. Robolectric network callbacks need explicit simulation.
   *Acceptance:* a test drives `ConnectivityManager` from offline to online and
   asserts both `checkConnectivity()` and the `observe` callback see the
   transition.

4. **Deduplicate this checklist.**
   Several tasks appear two or three times with different wording. Consolidate
   into one list with stable IDs so cycle reports can reference tasks by ID.
   *Acceptance:* every open task appears exactly once; completed work moves to
   a Done section with evidence.

5. **Document local-toolchain prerequisites.**
   There is no local JDK/Gradle; all validation is CI-only. Future cycles must
   not claim local test results.
   *Acceptance:* `.autonomy/` states the CI-only rule (started in
   `.autonomy/ci_debugging.md`).