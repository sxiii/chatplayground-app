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

## Next Improvement Suggestions (2026-11-24)

1. **[MEDIUM] WebView is never destroyed**: `MainActivity.onDestroy` now detaches the WebView from its parent then calls `webView.destroy()`, so the WebView releases its activity context and renderer binding instead of leaking past the activity's life. Covered by `MainActivityTeardownTest`.
2. **[MEDIUM] Loading indicator can spin forever**: `MainActivity`'s `WebViewClient` now overrides `onPageStarted` (indicator `VISIBLE`), `onPageFinished` (indicator `GONE`), and `onReceivedError` (indicator `GONE`), so aborted loads no longer leave the indicator spinning forever.
3. **[LOW] No WebView debugging in debug builds**: `WebViewDebugging.configure(BuildConfig.DEBUG)` enables `WebView.setWebContentsDebuggingEnabled` only for debug builds.
4. **[MEDIUM] WebViewDownloadListener**: `MainActivity` now sets a `DownloadListener` that delegates downloads to the system `DownloadManager`. Covered by `MainActivityDownloadTest`.
5. **[MEDIUM] WebView Search/Find**: Added `findText` and `findNext` methods on `MainActivity` delegating to `WebView.findAllAsync` and `WebView.findNext`.
6. **[MEDIUM] WebView Cookie Management Settings**: Allow users to view and clear cookies managed by CookieManager. Covered by `MainActivityCookieTest`.
7. **[MEDIUM] WebView back-button history navigation**: `MainActivity.onCreate` registers an `OnBackPressedCallback`: Back calls `webView.goBack()` while `canGoBack()` is true, otherwise disables itself and re-dispatches so the activity finishes.
8. **[MED] WebView Ad Blocker / Content Filter**: Consider implementing `shouldInterceptRequest` to block ads, trackers, and malicious content patterns.
9. **[MEDIUM] Custom Tab warm-up and theming**: Package stamping + toolbar theming remain implemented.

## Tasks
- Implement `NetworkMonitor` load error test (FIXED - syntax error corrected).
- Push changes and verify CI passes.

## Cycle 2026-11-23 (06:00 UTC) — Fix Kotlin Lint and Push
1. **[HIGH] Fix Kotlin Lint in Test** — Addressed `standard:blank-line-before-declaration` and `standard:final-newline` in `MainActivityOfflineRecoveryTest.kt`. Acceptance: CI lint step green.

## Cycle 2026-11-24 (02:10 CET) — Fix NetworkMonitorLoadErrorTest Compilation

1. **[HIGH] Fix NetworkMonitorLoadErrorTest compilation error** — The test file `NetworkMonitorLoadErrorTest.kt` had unresolved reference to `ConnectivityManager` due to missing import. The file `NetworkMonitorObserveTest.kt` and `NetworkMonitorTest.kt` were updated to include explicit `@Config(sdk = [26])` annotation matching the Robolectric SDK consistency requirement. Acceptance: CI unit tests pass.

## Improvement Suggestions (open)
1. **[MEDIUM] A single failed sub-resource loads the offline page**: Handle `WebResourceRequest` overload and ignore `!request.isForMainFrame`; test that sub-resource errors leave the current page loaded with no toast.
2. **[MEDIUM] WebView is never destroyed**: Already fixed, covered by tests.
3. **[MEDIUM] Loading indicator can spin forever**: Already fixed with `onReceivedError` handling.
4. **[LOW] Silent drop of unusable URLs**: `MainActivity.handleUrl` returns `true` for URLs that match no rule without any trace. Acceptance: log at debug level for unsupported schemes.
5. **[MEDIUM] WebView Search/Find**: Implemented with `findText`/`findNext` methods.
6. **[MEDIUM] WebViewDownloadListener**: Implemented delegating to system `DownloadManager`.
7. **[MEDIUM] WebView Cookie Management Settings**: Implemented with UI for viewing/clearing cookies.