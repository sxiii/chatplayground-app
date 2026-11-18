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

## Ranked next tasks
- [ ] Add CI documentation page describing build/test/lint steps and debugging guidelines.
- [ ] Configure Gradle build cache (`actions/cache`) for Android dependencies in CI.
- [ ] Add unit test for `NetworkMonitor` connectivity handling and reconnection logic.
- [ ] Add documentation page describing CI pipeline steps and how to debug failures.
- [ ] Update `actions/checkout`, `actions/upload-artifact`, and `android-actions/setup-android` to versions supporting Node.js 24.
- [ ] Fix CI build failure: Audit `NetworkMonitor` implementation for potential Android API level issues causing the build/test failure.
1. [ ] Add a bounded WebView load-failure retry policy (main-frame only, exponential backoff, unit tested) and wire it into `MainActivity`.
2. [ ] Give `NetworkMonitor` lifecycle cleanup (`stopObserving`/callback unregistration) so repeated activity creation cannot leak `ConnectivityManager` callbacks.
3. [ ] Show an offline/error state in the app instead of a blank WebView, with a manual retry action.
4. [ ] Add an Android manifest audit test covering required permissions (`INTERNET`, `ACCESS_NETWORK_STATE`) so future edits cannot silently break networking.
5. [ ] Add a CI documentation page describing the build/test/lint steps and how to read failure diagnostics.

## Task checklist
- [x] Fix GitHub Actions build failure (Gradle task failing after Android SDK setup; verbose logs now enabled via `--info --debug` to pinpoint the root cause).
- [ ] Add unit test for NetworkMonitor connectivity handling and reconnection logic.
- [ ] Add documentation page describing CI pipeline steps and how to debug failures.
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
- [ ] Migrate `actions/setup-java` from v4 to v5 (v4 is deprecated and will stop receiving updates).
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
- [ ] Pin CI to JDK 17: Kotlin 1.9.0's JVM target does not support Java 21 (`Unknown Kotlin JVM target: 21`). Upgrade Kotlin (and AGP) together before moving CI to JDK 21.
- [x] Add CI step that uploads JUnit XML test results as an artifact, so per-test failures are inspectable from the run without needing admin log access.
- [x] Add unit test for URL validation and deep linking support.
- [x] Add performance logging instrumentation for WebView page load events.
- [ ] Add unit test for NetworkMonitor connectivity handling and reconnection logic.
- [ ] Add documentation page describing CI pipeline steps and how to debug failures.