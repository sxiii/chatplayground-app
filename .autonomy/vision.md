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
- [x] **[HIGH]** Surface real dependency-resolution failures from the `Verify Dependencies` step.
- [x] **[MEDIUM]** Add a CI preflight step that validates `gradle.properties` contains `android.useAndroidX=true` before `assembleDebug`.
- [x] **[HIGH]** Implement automated release tagging workflow on main branch merges.
- [x] **[MEDIUM]** Give `NetworkMonitor` lifecycle cleanup (`stopObserving`/callback unregistration) so repeated activity creation cannot leak `ConnectivityManager` callbacks.
- [x] **[HIGH]** Fix `NetworkMonitor.observe()` callback leak.
- [x] **[MEDIUM]** Add CI documentation page describing the build/test/lint steps and how to read failure diagnostics.
- [x] **[MEDIUM]** Add lint warnings gate: Fail CI if `lint-results.html` contains any warning.
- [x] **[MEDIUM]** Add `actions/cache` for `android-sdk` to speed up builds.
- [x] **[MEDIUM]** Pin `android-actions/setup-android` to a specific version/digest instead of floating `@v3`.
- [x] **[HIGH]** Implement `NetworkMonitor.checkConnectivity` correctly.
- [x] **[MEDIUM]** Implement a test that simulates network state transitions using Robolectric `ShadowConnectivityManager`.
- [x] Add unit test for SecureStorage encryption edge cases and fallback mechanisms. — DONE (commit `071b87f`; CI run #103 green).
- [x] **[HIGH]** Implement `SecureStorage.contains(context, key)` to check key presence without decrypting. — DONE (commit `27d1ab7`).
- [x] **[HIGH]** Fix Kotlin version mismatch to support newer AndroidX libraries (upgraded to 2.1.0 in `270f1b1`).
- [x] **[HIGH]** Fix CI failure by reverting Gradle wrapper to 8.5 to match AGP 8.2.0 (commit `ec57da9`).
- [ ] **Add Espresso UI tests for WebView.**
- [x] Add code formatting enforcement (ktlint). — *Note: Removed from CI in #101 due to config failures.*
- [ ] Add automated UI integration tests using Espresso.
- [ ] Add a settings screen for toggling features.
- [ ] Implement Biometric Authentication for enhanced security.
- [ ] Implement Dark Mode support.
- [ ] Add Network Monitoring & Auto-reconnect handling.
- [ ] Implement Screen Reader accessibility improvements for the WebView content.
- [ ] Add basic performance profiling for initial app load.
- [x] Add an explicit CI step that asserts the generated APK artifacts exist before upload (fail fast with a clear message).
- [ ] **[MEDIUM]** Investigate why `org.jmailen.gradle.kotlinter` caused configuration failures and fix if possible. — RESOLVED (not fixed): removed from CI.

## Future Improvements
- [ ] **Add Gradle build cache configuration for Android dependencies** — use `actions/cache` pointing to `~/.gradle` and `app/build/outputs/`.
- [x] **Add automated dependency scanning** via Dependabot.
- [ ] **Implement a performance benchmark for WebView page load** in instrumentation tests.
- [ ] **Create a quick start guide** for developers who clone the repo locally.
- [ ] **[MEDIUM]** Add a CI preflight step that validates `gradle.properties` defines `agpVersion`, `kotlinVersion`, and `gradleVersion` before `assembleDebug`.
- [ ] **[MEDIUM]** Add CI step that asserts generated APK artifacts exist before upload.
- [ ] **Add Espresso UI integration test for WebView load-failure retry**.
- [ ] **Implement Network Monitoring & Auto-reconnect handling**.

## New Improvement Suggestions (2026-11-20)
- [ ] **[LOW] Custom Tab integration:** Use Android Custom Tabs for external URLs to provide a more integrated browser experience.
- [ ] **[MEDIUM] Splash Screen:** Implement a native splash screen to improve the app launch experience.
- [ ] **[LOW] URL Bar Overlay:** Implement a subtle, retractable overlay that displays the current loaded URL for user transparency.
- [ ] **[MEDIUM] Deep Link Integration:** Configure Android Intent Filters to allow the app to open when users click `chatplayground.ai` links in other apps.
- [x] **[MEDIUM] Add WebView Loading Indicator:** Implement a `ProgressBar` or similar UI element to show while the WebView is loading a page, improving perceived performance. — DONE (commit `3a59476`; CI success).
- [x] **[MEDIUM]** **Offline Fallback Page:** Show a friendly offline message or cached version when the network is unavailable instead of the standard browser error. — DONE (commit `ddde05d`).
- [ ] **[LOW]** **Performance Profiling Hook:** Expose optional logging of page load metrics (first-paint / DOMContentLoaded times) through `WebChromeClient` so future optimization work has data points.
- [ ] **[LOW]** **Accessibility Contrast Check:** Verify that WebView content and native overlay elements meet contrast ratios for screen-reader and low-vision users.
- [ ] **[MEDIUM]** **Add \"Share\" Button:** Implement a share action in the app's menu or a floating button to share the current WebView URL with other applications.
- [ ] **[LOW]** **Implement \"Clear Cache\" Option:** Add a user-accessible option to clear the WebView's cache and secure storage, useful for troubleshooting or freeing up storage.
- [ ] **[MEDIUM]** **Custom User-Agent:** Allow setting a custom User-Agent string via a settings config to bypass site restrictions.
- [ ] **[LOW]** **External Browser Option:** Add a menu option to open the current URL in the system's default browser.
- [ ] **[MEDIUM]** **WebView Zoom Control:** Implement basic zoom support for users with visual impairments.
- [ ] **[MEDIUM]** **Custom Theme Engine:** Allow users to inject a custom CSS file into the WebView to theme the site.
- [ ] **[MEDIUM]** **URL History Navigation:** Add \"Back\" and \"Forward\" buttons/gestures to the native UI for better WebView navigation.
- [ ] **[LOW]** **Page Title Sync:** Update the Android Activity title to match the current page title of the WebView.
- [ ] **[MEDIUM]** **Custom Error Pages:** Provide specific, styled local HTML pages for common errors (404, 500) to improve the offline/error experience.
- [ ] **[LOW]** **App Version Info:** Add a small \"About\" section or menu item showing the current app version and build number.
- [x] **[HIGH]** **Synchronize `docs/FUNCTIONS.md` with actual CI/Build config:**
    - `actions/setup-java` is currently `@v5` in `build.yml` but `@v4` in docs.
    - `android-actions/setup-android` is pinned to a specific SHA in `build.yml` but `@v3` in docs.
    - `gradle` wrapper is pinned to `8.5` in `gradle-wrapper.properties` (matching `gradleVersion=8.5` in `gradle.properties`), but docs may still reference older or mismatched versions.
    - `ktlint` is no longer enforced in CI (removed due to config failures), but `docs/FUNCTIONS.md` still claims it is a dedicated CI step.
- [ ] **[MEDIUM]** **Update `ci_debugging.md` to reflect `ci-workflow-hygiene`:**
    - Ensure it no longer suggests parsing `.log` files (removed from `build.yml`) and focuses on exit codes and `--stacktrace` output in the job console.
- [x] **[MEDIUM]** **Implement `SecureStorage.clear(context)`** — DONE (commit `6878f98`; CI success).
    - Provide a way to wipe all secure preferences, useful for a \"Log out / Reset app\" feature.
- [ ] **[MEDIUM]** **WebView page-load performance profiling:** Log first-paint / `onPageFinished` timings so slow loads can be identified without user reports.
- [ ] **[LOW]** **Settings screen for app options:** Allow toggling WebView debugging and a \"Clear secure storage\" action that calls `SecureStorage.clear(context)`.
