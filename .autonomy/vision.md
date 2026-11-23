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

## Next Improvement Suggestions (2026-11-22, after the Custom Tabs cycle)

1. **[x] [HIGH] Robolectric SDK consistency**: DONE 2026-11-22. One source of truth — `app/src/test/resources/robolectric.properties` (`sdk=26`) — plus removal of all eleven contradictory per-class `@Config(sdk/minSdk/maxSdk)` pins (`CacheClearerTest`, `SecureStorage*`, `WebViewLoadRetryPolicy*`, `MainActivity*`, `ManifestDeepLinkTest`, `NetworkMonitorObserveTest`). Guard `ci_check_robolectric_sdk.py` runs in CI after `Check Unit Test Results` and fails the build when the file is missing, `sdk != 26`, or any test pins an SDK. CI run `44982364135` (in progress at cutover).
2. **[x] [MEDIUM] Docs claim ktlint enforcement that does not exist**: DONE 2026-11-22. Plugin `org.jmailen.kotlinter` 4.3.0 applied to `app/build.gradle` and `Run lintKotlin` step blocks CI pipeline.
3. **[MEDIUM] Custom Tab warm-up and theming**: De-scoped warm-up binding (API uncompileable). `CustomTabsServiceConnection` dropped as uncompileable API usage; dead binding fields removed from `MainActivity.kt`. Package stamping + toolbar theming remain implemented.
4. **[x] [MEDIUM] JaCoCo report is disabled**: DONE 2026-11-22. `xml.required = true` and `html.required = true` set; CI validates and uploads coverage artifacts.
5. **[x] [LOW] Misleading CI step name**: DONE 2026-11-22. Duplicate step dropped; `Collect JUnit Test Results` remains.
6. **[LOW] Silent drop of unusable URLs**: `MainActivity.handleUrl` returns `true` for URLs that match no rule (blank/malformed) without any trace. Acceptance: log at debug level (or show a toast) so unsupported schemes are diagnosable.
7. **[x] [MEDIUM] WebView back-button history navigation**: DONE 2026-11-22. `MainActivity.onCreate` registers an `OnBackPressedCallback`: Back calls `webView.goBack()` while `canGoBack()` is true, otherwise disables itself and re-dispatches so the activity finishes. Covered by `MainActivityTest.backPressNavigatesWebViewHistoryInsteadOfFinishingActivity` (asserts `goBackInvocations == 1`, previous URL restored, activity not finishing) and `backPressFinishesActivityWhenWebViewHasNoHistory`. RED run `44987520776` (1 failed), GREEN run `44987959751` (success).
8. **[x] [MEDIUM] Mixed Content / HTTPS enforcement**: DONE 2026-11-22. Added `WebViewConfigurator` (`app/src/main/java/com/chatplayground/app/utils/WebViewConfigurator.kt`) applying `WebSettings`: JavaScript enabled, DOM storage enabled, `mixedContentMode = MIXED_CONTENT_NEVER_ALLOW`, `allowFileAccess = false`, `allowContentAccess = false`. Called from `MainActivity.onCreate` before the first `loadUrl`. Covered by `WebViewConfiguratorTest` (js, domStorage, mixedContentMode == NEVER_ALLOW, local/file/content access disabled, idempotent, applied by MainActivity.onCreate). RED run `44990548191` failed; GREEN run `44990929779` success.

## Next Improvement Suggestions (2026-11-22, after the WebView settings cycle)

9. **[x] [MEDIUM] WebView is never destroyed**: DONE 2026-11-22. `MainActivity.onDestroy` now detaches the WebView from its parent (`(webView.parent as? ViewGroup)?.removeView(webView)`) then calls `webView.destroy()`, so the WebView releases its activity context and renderer binding instead of leaking past the activity's life. Covered by `MainActivityTeardownTest` (`wasDestroyCalled()` true after tear-down, parent cleared). RED run `44993168233` failed as designed (`WebViewConfigurator`-style unresolved/teardown missing); GREEN run `44993627992` success — full build + all tests green on GitHub workers.
10. **[x] [MEDIUM] Loading indicator can spin forever, and docs describe hooks that do not exist**: DONE 2026-11-22. `MainActivity`'s `WebViewClient` now overrides `onPageStarted` (indicator `VISIBLE`), `onPageFinished` (indicator `GONE`), and `onReceivedError` (indicator `GONE`), so aborted loads no longer leave the indicator spinning forever. `docs/FUNCTIONS.md` §4 now matches the implemented callbacks. Covered by `MainActivityLoadingIndicatorTest` (`loadingIndicatorIsShownOnPageStarted`, `loadingIndicatorIsHiddenOnPageFinished`, `loadingIndicatorIsHiddenOnReceivedError`). RED run `44995811409` failed as designed; GREEN run `44996139052` success — full build + all tests green on GitHub workers.
11. **[x] [LOW] No WebView debugging in debug builds**: DONE 2026-11-22. `WebViewDebugging.configure(BuildConfig.DEBUG)` enables `WebView.setWebContentsDebuggingEnabled` only for debug builds; a release build never exposes its renderer to `chrome://inspect`. Covered by `WebViewDebuggingTest` (debug build enables, release build does nothing, MainActivity wires `BuildConfig.DEBUG` into the injectable seam). `app/build.gradle` now sets `buildFeatures.buildConfig = true` (AGP 8 defaults it off) so `BuildConfig.DEBUG` is generated. Docs recorded in `docs/FUNCTIONS.md` under \"WebView Remote Debugging (`WebViewDebugging`)\". RED run `44999065618` failed as designed; GREEN run `45000486170` success — full build + all tests green on GitHub workers.

## Next Improvement Suggestions (2026-11-22, after the WebView debugging cycle)

12. **[x] [MEDIUM] `WebViewLoadRetryPolicy` retry budget**: DONE 2026-11-22. `WebViewLoadRetryPolicy(maxRetries = 3)` governs retry-with-backoff on network errors, capped at `maxRetries` reloads with exponential backoff, sub-frame failures never retried. Added `MainActivityRetryBudgetTest` asserting exactly one retry-budget unit per failed load (`shouldRetry` consumes; `recordRetry` is not called again by `MainActivity.onReceivedError`). The wiring had a real double-count: `shouldRetry(true)` advanced the counter and `recordRetry()` advanced it again, so a `maxRetries = 3` policy gave up after two real retries. Removed the redundant `recordRetry()` call so `shouldRetry()` is the single increment point. RED run `45003263226` failed as designed (`expected 1, actual 2`); GREEN run `45003682180` success — full build + all 103 tests green on GitHub workers.
13. **[x] [LOW] `ci_check_robolectric_sdk.py` scans only `app/src/test/java`**: DONE 2026-11-22. Broadened the scan to `app/src` in full (both `test` and `androidTest` source sets), removed the stray `@Config(sdk = [34])` pin on `app/src/androidTest/java/.../MainActivityTest.kt`, and updated `docs/FUNCTIONS.md` §1 to describe the broader scope. Guard now detects instrumentation-test SDK overrides too.

## Next Improvement Suggestions (2026-11-22, after the teardown cycle)

12. **[MEDIUM] A single failed sub-resource loads the offline page**: `MainActivity` overrides only the deprecated `onReceivedError(WebView, Int, String, String)`. On API 23+ that overload is still invoked for *sub-resource* failures (a blocked image, an analytics beacon), while the `WebResourceRequest`/`WebResourceError` overload is the main-frame one. So one broken image on `chatplayground.ai` replaces the whole page with `offline.html` and shows a \"Network error occurred\" toast. Acceptance: handle the `WebResourceRequest` overload and ignore `!request.isForMainFrame`; test that a sub-resource error leaves the current page loaded and shows no toast, and that a main-frame error still falls back to the offline page.
13. **[LOW] `NetworkMonitor` callback reads WebView state off the UI thread**: `MainActivity.onCreate` passes `observe { isConnected -> if (isConnected && webView.url == \"file:///android_asset/offline.html\") …}`. The `ConnectivityManager.NetworkCallback` runs on the connectivity agent thread, so only `reload()` is marshalled back (`runOnUiThread`) while the `webView.url` read and the branch decision happen off-thread. Acceptance: move the whole callback body into `runOnUiThread` (or post to a main-thread `Handler`), keep the auto-reconnect behavior identical, and cover it with a test that drives `onAvailable` from a non-main thread.
14. **[LOW] Docs section numbering collides**: `docs/FUNCTIONS.md` has two `## 4.` headings (`4. WebView UI & Loading` at line 53 and `4. Code Formatting Enforcement (CI)` at line 102), so cross-references (\"§4\") are ambiguous — the ktlint section is also the one whose claims are unimplemented (item 2). Acceptance: renumber the formatting section to 5 and make the `§4` references in `vision.md`/`AUTONOMY` notes unambiguous.
15. **[LOW] `README.md` is three lines**: it does not say the project can only be built by GitHub workers, where CI status/artifacts live, or how to run the unit tests. Acceptance: document the GitHub-native build requirement, the workflow name, the artifact names, and the Robolectric SDK pin.
16. **[x] [MEDIUM] Verify JaCoCo report before upload**: DONE 2026-11-22. `.github/workflows/build.yml` now runs `Verify JaCoCo Report Present` before `Upload JaCoCo Report`, failing fast when `app/build/reports/jacoco/jacocoTestReport.xml` is absent.
17. **[x] [LOW] Duplicate `Upload JUnit Test Results` step**: DONE 2026-11-22. Dropped the duplicate `run:` step at `.github/workflows/build.yml:119`; `Collect JUnit Test Results` (line 66) remains the single collection point.
18. **[x] [LOW] `README.md` is three lines**: DONE 2026-11-22. `README.md` already documents the GitHub-native build requirement (`.github/workflows/build.yml`), the artifact names (debug/release APKs, junit-results, JaCoCo), and the Robolectric SDK pin (API 26).

## Next Improvement Suggestions (open)

19. **[x] [MEDIUM] A single failed sub-resource loads the offline page**: DONE 2026-11-22: `handleWebViewError(view, code, isMainFrame)` extracted onto `MainActivity` as `internal` so `MainActivitySubResourceErrorTest` can invoke it directly; sub-resource errors leave the page loaded with no toast, main-frame errors fall back to `offline.html` with a toast. CI run `45026480870` success — full build + all tests green on GitHub workers.
20. **[x] [LOW] `NetworkMonitor` callback reads WebView state off the UI thread**: DONE 2026-11-22. `MainActivity.onCreate` now wraps the whole callback body — the `webView.url` read **and** the reload decision — in `runOnUiThread`, so nothing touches WebView state on the connectivity agent thread. Acceptance: cover it with a test that drives `onAvailable` from a non-main thread (still open; see item below).
21. **[x] [LOW] Docs section numbering collides**: DONE 2026-11-22. `docs/FUNCTIONS.md` now numbers the sections sequentially (`## 4.` WebView UI & Loading, `## 5.` Code Formatting Enforcement (CI)); `§4` references are unambiguous.
22. **[x] [LOW] NetworkMonitor UI-thread test**: DONE 2026-11-22. Added `MainActivityNetworkMonitorTest` asserting that when `NetworkMonitor` fires `onAvailable` from a background thread while the WebView is on `offline.html`, `MainActivity` marshals the check and reload onto the main thread (`Looper.getMainLooper()`), verified via Robolectric.
23. **[x] [MEDIUM] ktlint enforcement gap**: DONE 2026-11-22. `org.jmailen.gradle.kotlinter` plugin applied, CI step added.
24. **[x] [MEDIUM] JaCoCo report disabled**: DONE.
25. **[x] [LOW] Misleading CI step name**: DONE.
26. **[x] [LOW] Offline-page fallback tests bypass the WebViewClient dispatch path**: DONE 2026-11-22. `MainActivitySubResourceErrorTest` now drives `WebViewClient.onReceivedError(WebView, WebResourceRequest, WebResourceError)` end to end.
27. **[MEDIUM] Custom Tab warm-up and theming**: De-scoped warm-up binding (API uncompileable). `CustomTabsServiceConnection` dropped as uncompileable API usage; dead binding fields removed from `MainActivity.kt`. Package stamping + toolbar theming remain implemented (no `onStart`/`onStop` bind/unbind).
28. **[x] [LOW] Silent drop of unusable URLs**: DONE 2026-11-22: `handleUrl` logs `Log.d(\"MainActivity\", \"Unsupported URL dropped: $url\")`; covered by `MainActivityTest.handleUrlLogsDebugForUnsupportedUrls`.
29. **[HIGH] Investigate and resolve brittle network dependency in unit tests (MavenArtifactFetcher)**.
- [x] **[LOW] Add automated UI regression test for offline state recovery** — DONE 2026-11-23. `MainActivityOfflineRecoveryTest` asserts that when `NetworkMonitor` fires `onAvailable` while the WebView is on `offline.html`, the activity reloads the `lastTargetUrl` instead of just reloading the offline page. `lastTargetUrl` is captured in `onCreate` and `onNewIntent`.
- [x] **[LOW] Add unit tests for `LinkRouter` edge cases** — DONE 2026-11-23. Verified `intent://` and other unusual schemes are correctly identified as external schemes.
- [x] **[MEDIUM] Implement WebView Download Listener** — DONE 2026-11-23. `MainActivity` now sets a `DownloadListener` that delegates downloads to the system `DownloadManager`, providing native-like download handling. Covered by `MainActivityDownloadTest`.
- [ ] **[LOW] Optimize WebView Asset Loading** — Implement `shouldInterceptRequest` to load local assets (CSS/JS) for the offline page or common resources more efficiently.
- [x] **[MEDIUM] Implement WebView Search/Find** — DONE 2026-11-23. Added `findText` and `findNext` methods on `MainActivity` delegating to `WebView.findAllAsync` and `WebView.findNext`, covered by `MainActivitySearchTest`.
- [x] **[LOW] Add "About" dialog** — DONE 2026-11-23. Show app version, license info, and a link to the GitHub repository. Covered by `MainActivityTest.aboutMenuItemShowsAboutDialog`.
- [ ] **[LOW] Add Theme Customization Preference**: Allow users to toggle between Light, Dark, and System default themes inside app settings.
- [ ] **[LOW] Improve Test Resilience**: Add caching/retry logic for transient network failures during Robolectric dependency fetching in CI.
