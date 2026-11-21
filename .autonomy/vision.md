# Project Vision: ChatPlayground Android Wrapper

## Key Goals
Build a native Android wrapper for chatplayground.ai with a WebView-based UI,
leveraging Jetpack Compose for native elements (loading indicators, menus)
while keeping the main content in a WebView.

## Todo Tasks (Backlog)
- [x] Define technical stack (WebView vs Native Components) and architecture.
- [x] Setup initial project structure.
- [x] Add several new development ideas into vision.md
- [x] Implement basic WebView wrapper functionality.
- [x] Implement secure credential storage (Android Keystore).
- [x] Fix GitHub Actions build failure — root cause was the missing `gradle.properties` (`android.useAndroidX=true` not set; AGP 8.2 defaults it to false). Fixed by adding `gradle.properties`; CI now also publishes diagnostics on failure.
- [x] Add a CI step that asserts APK artifacts exist before upload.
- [x] Add a dependency-verification / lint gate to CI. — DONE: `Verify Dependencies` step exits 1 when `gradle_dependencies.log` contains `Could not`/`FAILED`; `Run Lint` step exits 1 when `lint-results.html` contains `warning`.
- [x] Fix `NetworkMonitor.observe()` callback leak — calling `observe()` repeatedly now unregisters the previous `NetworkCallback` before registering the next one (covered by unit test `observeCalledTwiceUnregistersPreviousCallback`).
- [x] Implement push notifications.
- [x] Add Network Monitoring & Auto-reconnect handling.
- [x] **[MEDIUM] WebView Scroll Position Restoration**: Save and restore `WebView` state with `saveState`/`restoreState` so the user does not lose their place after rotation or process death.
- [x] **[MEDIUM] Offline Fallback Page for WebView network errors** — DONE: implemented in `onReceivedError` with `file:///android_asset/offline.html`.
- [x] Implement Dark Mode support.
- [x] Fix Kotlin compilation errors (CacheClearer.kt - removed test import, fixed clearCookies API)
- [x] **[MEDIUM] WebView Navigation History**: Implement swipe gestures or UI controls to navigate WebView's history stack (back/forward), providing native-like browsing experience.
- [x] **[MEDIUM] Session Persistence**: Save/restore WebView navigation state (URL, scroll position, form inputs) to survive process death without relying on Activity recreation.
- [x] **Fix night mode color resources**: Added missing `surface` and `background` colors to base `values/colors.xml` for proper night mode fallback.

## Next Improvement Suggestions (2026-11-21)
1. **[MEDIUM] WebView Custom Tabs**: Use Android Custom Tabs for external links to provide a more integrated browser experience without leaving the app context entirely.
2. **[MEDIUM] Deep Link Integration**: Configure Android Intent Filters for `chatplayground.ai` links to allow the app to open when users click `chatplayground.ai` links in other apps.
3. **[MEDIUM] Biometric Authentication for Secure Storage**: Protect stored credentials in SecureStorage using BiometricPrompt before retrieval.
4. **[MEDIUM] Fix CI Lint Gate false positive**: Remove or replace the naive `grep -q "warning"` check against HTML lint reports. `./gradlew lint` already fails on lint errors. If we want to fail on warnings, use `android { lintOptions { warningsAsErrors = true } }` in `app/build.gradle` and let Gradle's own exit code handle it.
5. **[MEDIUM] CI JaCoCo Artifacts**: Resolve "No files were found" errors for JaCoCo coverage artifacts by ensuring execution data files are generated/uploaded to the correct paths in CI.
6. **[LOW] User-facing WebView load error handling**: Replace or augment the silent offline fallback with a toast or Compose dialog that tells the user the page failed to load and offers retry. — DONE: `MainActivity` now shows `Toast.makeText(..., "Network error occurred. Showing offline page.", ...)` before loading `offline.html` on network errors. Covered by `MainActivityLoadErrorNotificationTest` (asserts toast text and duration).
7. **[LOW] ktlint / Android Lint baseline**: Add `ktlint` or a `baseline.xml` for Android Lint so new warnings can be tracked without blocking CI on pre-existing ones.

## In Progress (2026-11-21)
- [ ] Remove faulty CI lint check that greps HTML reports for the literal word "warning" (`build.yml` updated; CI green verification pending).

## Completed Tasks
- WebView Cache Management
- WebView Loading Indicator
- WebView Load Retry Policy
- Offline Fallback Page Loading
- Dark Mode Support (with proper color resource fallback)
- Kotlin Compilation Error Fixes
- SecureStorage.remove() method
- WebView Navigation History Implementation
- Session Persistence Implementation