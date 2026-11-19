# Project Vision: ChatPlayground Android Wrapper

## Key Goals
1. Provide a native-like experience for interacting with ChatPlayground on Android.
2. Ensure secure handling of session data and credentials.
3. Support offline functionality where applicable.
4. Maintain high performance and responsiveness.
5. Iterate on ideas and improvements for the app.
6. Enhance UI/UX based on user feedback.
7. Integrate analytics for performance monitoring.

## Todo Tasks (Backlog)
- [x] **[HIGH]** Investigate CI JaCoCo report failure: identify why `jacocoTestReport` task fails despite successful `testDebugUnitTest` and generate/upload `.exec` files as artifacts to diagnose.
- [x] **[HIGH]** Add explicit artifact upload step for `.exec` files in CI to simplify debugging.
- [x] **[HIGH]** Implement a bounded WebView load-failure retry policy (main-frame only, exponential backoff, unit tested) and wire it into `MainActivity`.
- [x] **[HIGH]** Fix JaCoCo graceful skip: ensure `jacocoTestReport` skips when no `.exec` files are present in any build directory.
- [x] **[HIGH]** Add a dependency-verification step in CI to ensure dependency integrity and security.
- [x] **[HIGH]** Surface real dependency-resolution failures from the `Verify Dependencies` step: grep `gradle_dependencies.log` for `Could not resolve`/`FAILED` → `::warning` annotation + summary line.
- [x] **[MEDIUM]** Add a CI preflight step that validates `gradle.properties` contains `android.useAndroidX=true` and `android.enableJetifier=true` before `assembleDebug`.
- [x] **[HIGH]** Implement automated release tagging workflow on main branch merges (idempotent: checks tag existence before `git tag -a`).
- [x] **[MEDIUM]** Give `NetworkMonitor` lifecycle cleanup (`stopObserving`/callback unregistration) so repeated activity creation cannot leak `ConnectivityManager` callbacks.
- [x] **[HIGH]** Fix `NetworkMonitor.observe()` callback leak: calling `observe()` repeatedly now unregisters the previous `NetworkCallback` before registering the next one (covered by `observeCalledTwiceUnregistersPreviousCallback` test).
- [x] **[MEDIUM]** Add CI documentation page describing the build/test/lint steps and how to read failure diagnostics.
- [x] **[MEDIUM]** Add lint warnings gate: Fail CI if `lint-results.html` contains any warning.
- [x] **[MEDIUM]** Add `actions/cache` for `android-sdk` to speed up builds.
- [x] **[MEDIUM]** Pin `android-actions/setup-android` to a specific version/digest instead of floating `@v3` for reproducible CI builds.
- [x] **[HIGH]** Implement `NetworkMonitor.checkConnectivity` correctly and ensure `NetworkMonitorTest.kt` passes. — DONE
- [x] **[MEDIUM]** Implement a test that simulates network state transitions using Robolectric `ShadowConnectivityManager` to verify `NetworkMonitor.observe` logic. — DONE
- [x] **[MEDIUM]** Add CI documentation page describing the build/test/lint steps and how to read failure diagnostics. — DONE
- [x] **[MEDIUM]** Add lint warnings gate: Fail CI if `lint-results.html` contains any warning. — DONE
- [x] **[MEDIUM]** Add `actions/cache` for `android-sdk` to speed up builds. — DONE
- [x] **[MEDIUM]** Pin `android-actions/setup-android` to a specific version/digest instead of floating `@v3` for reproducible CI builds. — DONE
- [ ] Add unit test for SecureStorage encryption edge cases and fallback mechanisms.
- [ ] Add Espresso UI tests for WebView.
- [x] Add code formatting enforcement (ktlint).
- [ ] Add automated UI integration tests using Espresso.
- [ ] Add a settings screen for toggling features.
- [ ] Implement Biometric Authentication for enhanced security.
- [ ] Implement Dark Mode support.
- [ ] Add Network Monitoring & Auto-reconnect handling.
- [ ] Implement Screen Reader accessibility improvements for the WebView content.
- [ ] Add basic performance profiling for initial app load.
- [x] Add an explicit CI step that asserts the generated APK artifacts exist before upload (fail fast with a clear message). — already implemented in `.github/workflows/build.yml` "Assert APK Artifacts Exist" step.
- [ ] **[MEDIUM]** Investigate why `org.jmailen.gradle.kotlinter` caused configuration failures and fix if possible. — RESOLVED (not fixed): tried 3 approaches (plugin added run #99, config-block removed run #100, plugin removed run #101); all failed at "Build Debug APK" configuration phase. Per failure policy, not retried. kotlinter plugin removed from root + app `build.gradle`; orphaned `ktlintCheck` CI step removed from `.github/workflows/build.yml`. ktlint enforcement is not in CI until a compatible setup is found.

## Future Improvements
- [ ] **Add Gradle build cache configuration for Android dependencies** — use `actions/cache` pointing to `~/.gradle` and `app/build/outputs/`.
- [ ] **Add automated dependency scanning** via Snyk or Dependabot for the Android `build.gradle` files.
- [ ] **Implement a performance benchmark for WebView page load** in instrumentation tests and export results to a CSV artefact.
- [ ] **Create a quick start guide** for developers who clone the repo locally (Gradle wrapper, Android SDK environment variables).
- [ ] **[MEDIUM]** Add a CI preflight step that validates `gradle.properties` defines `agpVersion`, `kotlinVersion`, and `gradleVersion` before `assembleDebug`, failing fast with a clear message.
- [ ] **[MEDIUM]** Add Dependabot configuration (`.github/dependabot.yml`) for Gradle/Android dependencies to keep the AGP, Kotlin, and library toolchain current and receive automated security version bumps.
- [ ] **[HIGH]** Add `SecureStorage.remove(context, key)` method and unit test for key removal — DONE (commit `d6de9ab`).
- [x] **[MEDIUM]** Add ktlint code formatting enforcement to CI build. — DONE
- [ ] **[MEDIUM]** Add CI step that asserts generated APK artifacts exist before upload (fail fast with clear message). — already implemented in `.github/workflows/build.yml` "Assert APK Artifacts Exist" step.
- [ ] **Add Espresso UI integration test for WebView load-failure retry** — simulate an `onReceivedError` main-frame failure and verify `WebViewLoadRetryPolicy` schedules a reload with exponential backoff, bounded by `maxRetries`.
- [ ] **Implement Network Monitoring & Auto-reconnect handling** — surface connectivity state changes to the WebView layer (e.g. show offline banner / auto-reload on reconnect) and add unit tests for the reconnect path.
