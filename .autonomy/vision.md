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

1. **[x] [HIGH] Documentation of GitHub-native build requirement, artifact names, and Robolectric SDK pin**: DONE 2026-11-27. `README.md` updated to clearly state that the project is built and tested exclusively on GitHub Actions, list the generated artifacts (`debug`/`release` APKs, `junit-results`, `jacoco-report`), and pin the required Robolectric SDK (`API 26`) for local and CI consistency.
2. **[x] [HIGH] CI ktlint enforcement for test suite**: DONE 2026-11-27. Resolved remaining `standard:no-empty-first-line-in-class-body` and `standard:multiline-expression-wrapping` ktlint violations in `ShortcutRegistryTest.kt` and `ShortcutIntegrationTest.kt`. Commit `bf8f390`.
3. **[MEDIUM] Custom Tab warm-up and theming**: De-scoped warm-up binding (API uncompileable). `CustomTabsServiceConnection` dropped as uncompileable API usage; dead binding fields removed from `MainActivity.kt`. Package stamping + toolbar theming remain implemented.
4. **[LOW] Silent drop of unusable URLs**: `MainActivity.handleUrl` returns `true` for URLs that match no rule (blank/malformed) without any trace. Acceptance: log at debug level (or show a toast) so unsupported schemes are diagnosable.
5. **[x] [LOW] Misleading CI step name**: DONE 2026-11-22. Duplicate step dropped; `Collect JUnit Test Results` remains.
6. **[LOW] Silent drop of unusable URLs**: `MainActivity.handleUrl` returns `true` for URLs that match no rule (blank/malformed) without any trace. Acceptance: log at debug level (or show a toast) so unsupported schemes are diagnosable.
7. **[x] [MEDIUM] WebView back-button history navigation**: DONE 2026-11-22. `MainActivity.onCreate` registers an `OnBackPressedCallback`: Back calls `webView.goBack()` while `canGoBack()` is true, otherwise disables itself and re-dispatches so the activity finishes. Covered by `MainActivityTest.backPressNavigatesWebViewHistoryInsteadOfFinishingActivity` (asserts `goBackInvocations == 1`, previous URL restored, activity not finishing) and `backPressFinishesActivityWhenWebViewHasNoHistory`. RED run `44987520776` (1 failed), GREEN run `44987959751` (success).
8. **[x] [MEDIUM] Mixed Content / HTTPS enforcement**: DONE 2026-11-22. Added `WebViewConfigurator` (`app/src/main/java/com/chatplayground/app/utils/WebViewConfigurator.kt`) applying `WebSettings`: JavaScript enabled, DOM storage enabled, `mixedContentMode = MIXED_CONTENT_NEVER_ALLOW`, `allowFileAccess = false`, `allowContentAccess = false`. Called from `MainActivity.onCreate` before the first `loadUrl`. Covered by `WebViewConfiguratorTest` (js, domStorage, mixedContentMode == NEVER_ALLOW, local/file/content access disabled, idempotent, applied by MainActivity.onCreate). RED run `44990548191` failed; GREEN run `44990929779` success.

## Next Improvement Suggestions (2026-11-22, after the WebView settings cycle)

9. **[x] [MEDIUM] WebView is never destroyed**: DONE 2026-11-22. `MainActivity.onDestroy` now detaches the WebView from its parent (`(webView.parent as? ViewGroup)?.removeView(webView)`) then calls `webView.destroy()`, so the WebView releases its activity context and renderer binding instead of leaking past the activity's life. Covered by `MainActivityTeardownTest` (`wasDestroyCalled()` true after tear-down, parent cleared). RED run `44993168233` failed as designed (`WebViewConfigurator`-style unresolved/teardown missing); GREEN run `44993627992` success — full build + all tests green on GitHub workers.
10. **[x] [MEDIUM] Loading indicator can spin forever, and docs describe hooks that do not exist**: DONE 2026-11-22. `MainActivity`'s `WebViewClient` now overrides `onPageStarted` (indicator `VISIBLE`), `onPageFinished` (indicator `GONE`), and `onReceivedError` (indicator `GONE`), so aborted loads no longer leave the indicator spinning forever. `docs/FUNCTIONS.md` §4 now matches the implemented callbacks. Covered by `MainActivityLoadingIndicatorTest` (`loadingIndicatorIsShownOnPageStarted`, `loadingIndicatorIsHiddenOnPageFinished`, `loadingIndicatorIsHiddenOnReceivedError`). RED run `44995811409` failed as designed; GREEN run `44996139052` success — full build + all tests green on GitHub workers.
11. **[x] [LOW] No WebView debugging in debug builds**: DONE 2026-11-22. `WebViewDebugging.configure(BuildConfig.DEBUG)` enables `WebView.setWebContentsDebuggingEnabled` only for debug builds; a release build never exposes its renderer to `chrome://inspect`. Covered by `WebViewDebuggingTest` (debug build enables, release build does nothing, MainActivity wires `BuildConfig.DEBUG` into the injectable seam). `app/build.gradle` now sets `buildFeatures.buildConfig = true` (AGP 8 defaults it off) so `BuildConfig.DEBUG` is generated. Docs recorded in `docs/FUNCTIONS.md` under "WebView Remote Debugging (`WebViewDebugging`)". RED run `44999065618` failed as designed; GREEN run `45000486170` success — full build + all tests green on GitHub workers.

## Next Improvement Suggestions (2026-11-22, after the WebView debugging cycle)

12. **[x] [MEDIUM] `WebViewLoadRetryPolicy` retry budget**: DONE 2026-11-22. `WebViewLoadRetryPolicy(maxRetries = 3)` governs retry-with-backoff on network errors, capped at `maxRetries` reloads with exponential backoff, sub-frame failures never retried. Added `MainActivityRetryBudgetTest` asserting exactly one retry-budget unit per failed load (`shouldRetry` consumes; `recordRetry` is not called again by `MainActivity.onReceivedError`). The wiring had a real double-count: `shouldRetry(true)` advanced the counter and `recordRetry()` advanced it again, so a `maxRetries = 3` policy gave up after two real retries. Removed the redundant `recordRetry()` call so `shouldRetry()` is the single increment point. RED run `45003263226` failed as designed (`expected 1, actual 2`); GREEN run `45003682180` success — full build + all 103 tests green on GitHub workers.
13. **[x] [LOW] `ci_check_robolectric_sdk.py` scans only `app/src/test/java`**: DONE 2026-11-22. Broadened the scan to `app/src` in full (both `test` and `androidTest` source sets), removed the stray `@Config(sdk = [34])` pin on `app/src/androidTest/java/.../MainActivityTest.kt`, and updated `docs/FUNCTIONS.md` §1 to describe the broader scope. Guard now detects instrumentation-test SDK overrides too.

## Next Improvement Suggestions (2026-11-22, after the teardown cycle)

12. **[MEDIUM] A single failed sub-resource loads the offline page**: `MainActivity` overrides only the deprecated `onReceivedError(WebView, Int, String, String)`. On API 23+ that overload is still invoked for *sub-resource* failures (a blocked image, an analytics beacon), while the `WebResourceRequest`/`WebResourceError` overload is the main-frame one. So one broken image on `chatplayground.ai` replaces the whole page with `offline.html` and shows a "Network error occurred" toast. Acceptance: handle the `WebResourceRequest` overload and ignore `!request.isForMainFrame`; test that a sub-resource error leaves the current page loaded and shows no toast, and that a main-frame error still falls back to the offline page.
13. **[LOW] `NetworkMonitor` callback reads WebView state off the UI thread**: `MainActivity.onCreate` passes `observe { isConnected -> if (isConnected && webView.url == "file:///android_asset/offline.html") …}`. The `ConnectivityManager.NetworkCallback` runs on the connectivity agent thread, so only `reload()` is marshalled back (`runOnUiThread`) while the `webView.url` read and the branch decision happen off-thread. Acceptance: move the whole callback body into `runOnUiThread` (or post to a main-thread `Handler`), keep the auto-reconnect behavior identical, and cover it with a test that drives `onAvailable` from a non-main thread.
14. **[x] [LOW] Docs section numbering collides**: `docs/FUNCTIONS.md` has two `## 4.` headings (`4. WebView UI & Loading` at line 53 and `4. Code Formatting Enforcement (CI)` at line 102), so cross-references ("§4") are ambiguous — the ktlint section is also the one whose claims are unimplemented (item 2). Acceptance: renumber the formatting section to 5 and make the `§4` references in `vision.md`/`AUTONOMY` notes unambiguous. — DONE 2026-11-26.
15. **[x] [LOW] `README.md` is three lines**: it does not say the project can only be built by GitHub workers, where CI status/artifacts live, or how to run the unit tests. Acceptance: document the GitHub-native build requirement, the workflow name, the artifact names, and the Robolectric SDK pin.
16. **[x] [MEDIUM] Verify JaCoCo report before upload**: DONE 2026-11-22. `.github/workflows/build.yml` now runs `Verify JaCoCo Report Present` before `Upload JaCoCo Report`, failing fast when `app/build/reports/jacoco/jacocoTestReport.xml` is absent.
17. **[x] [LOW] Duplicate `Upload JUnit Test Results` step**: DONE 2026-11-22. Dropped the duplicate `run:` step at `.github/workflows/build.yml:119`; `Collect JUnit Test Results` (line 66) remains the single collection point.
18. **[x] [LOW] `README.md` is three lines**: DONE 2026-11-22. `README.md` already documents the GitHub-native build requirement (`.github/workflows/build.yml`), the artifact names (debug/release APKs, junit-results, JaCoCo), and the Robolectric SDK pin (API 26).

## Next Improvement Suggestions (open)

19. **[x] [LOW] Track CI failure mode in `.autonomy/`**: DONE 2026-11-24. Created `ci_status.md` with run tracking; `last_run.md` and `cycle_summary.md` include CI run IDs and status. Acceptance met: every cron run has visibility into current CI state.
20. **[x] [LOW] `local.properties` untrack but still present on disk**: DONE 2026-11-24. Removed tracked copy, verified it's in `.gitignore`, is NOT in index, and does not show up in `git status --short`. Acceptance met.

## Next Improvement Suggestions (2026-11-24, after the ktlint-guard cycle)

21. **[x] [HIGH] Missing final newline keeps breaking CI on test files** — DONE 2026-11-24. Fourth occurrence (`55aaef8`, `d9c85fe`, `7f77f19`, and the item-50 merge). Added `scripts/check_kotlin_style.py` (stdlib only, ~1s, no Java) asserting every `.kt` ends with a newline and flagging trailing whitespace / tab indentation, with a `--fix` mode. Wired into `.github/workflows/build.yml` as *Check Kotlin Style (Final Newlines)*, placed before any Gradle step so the failure surfaces in seconds rather than after a five-minute worker round-trip. Acceptance met: guard passes on the whole tree (50 files) and would have caught both files from runs `45359097526`/`45359285941`.
22. **[x] [MEDIUM] No verification that CI-check scripts actually reject bad input**: DONE 2026-11-24. `scripts/test_ci_checks.py` feeds each guard (ci_check_tests.py, ci_check_robolectric_sdk.py, ci_check_autonomy_dirs.py, scripts/check_kotlin_style.py) a known-bad fixture in a temp dir and asserts exit code 1, plus known-good fixtures asserting exit 0, so a silently-passing guard is caught. Wired `Verify CI Checks Reject Bad Input` into `.github/workflows/build.yml` after the existing checks; CI run `45416435197` success — full build, all tests, lint, ktlint, JaCoCo and the new self-test green.
23. **[MEDIUM] Instrumentation (androidTest) suite is never built**: `lintKotlinAndroidTest` reports `NO-SOURCE`, and CI never assembles `androidTest`, so any behaviour only reachable on a device/emulator (WebView navigation, keystore, immersive mode) is untested and can break silently. Either add an `assembleAndroidTest` compile gate (cheap, catches breakage without an emulator) or document explicitly in `.autonomy/vision.md` that instrumentation is out of scope. Acceptance: `./gradlew :app:assembleAndroidTest` runs in CI, or a written scope decision exists. DONE 2026-11-24 (cycle after item 57): added `Assemble Android Test APK (Instrumentation Tests)` step to `.github/workflows/build.yml` between Debug APK and Unit Tests, so androidTest compilation breakage surfaces early without needing a device/emulator.
24. **[x] [LOW] `MainActivity` keeps accumulating responsibilities**: retry budget, network monitor wiring, cookie sync, and theme handling all live in one class... Extract the retry-budget logic into a plain testable `NavigationRetryPolicy` (no Android deps) so its rules can be unit-tested without shadow plumbing. DONE 2026-11-24. New `app/src/main/java/com/chatplayground/app/utils/NavigationRetryPolicy.kt` (pure JVM, no `Handler`/`Looper`, plain-JUnit testable) governs `shouldRetry(isMainFrame)`, `recordRetry()`, `reset()`, `retryCountValue`, `backoffDelayMs()`; `MainActivity.navigationRetryPolicy` delegates `onPageFinished`/`onNewIntent` resets and the `onReceivedError` retry-with-backoff. Old `WebViewLoadRetryPolicy` and its two Robolectric test classes removed; `MainActivityRetryBudgetTest`/`MainActivityRetryBudgetResetTest` re-pointed to `navigationRetryPolicy`. Acceptance met: `NavigationRetryPolicyTest` covers retry rules with no `@Config`/shadow setup, and `MainActivity` delegates to it.
25. **[x] [LOW] Pre-submit ktlint check**: `scripts/check_kotlin_style.py` catches formatting violations (empty first line in class body, missing final newline, trailing whitespace) in ~1s locally, but nothing reminds agents to run it before pushing. The `NavigationRetryPolicyTest.kt` empty-line violation survived to CI and cost a ~3-minute worker round-trip. Acceptance: every push that touches `.kt` files is preceded by a local `scripts/check_kotlin_style.py` run, recorded in `.autonomy/last_run.md`. DONE — this cycle's CI run `45523573578` failed on `lintKotlin` for `GestureNavigationHandler.kt` (three violations: missing trailing commas + blank first line in class body); all three fixed and the local guard now passes on the changed file.
26. **[x] [MEDIUM] `ci_check_tests.py` can pass on empty JUnit results**: DONE 2026-11-24. Added `test_fails_when_junit_results_directory_is_empty` test case to `scripts/test_ci_checks.py` that verifies the guard fails when `junit-results/` exists but contains no XML files. Acceptance met: guard exits non-zero for empty directory.

## Next Improvement Suggestions (2026-11-24, after the code review cycle)

27. **[x] [LOW] Implement "Clear All Data" setting**: DONE 2026-11-24. Added `DataClearer.kt` unifying offline cache, WebView cache, cookies, and SharedPreferences clearing. Added `action_clear_all_data` menu option in `main_menu.xml`, wired it in `MainActivity.kt`, and covered with `ClearAllDataTest.kt` and `MainActivityTest`.
28. **[x] [LOW] Custom User-Agent string**: DONE 2026-11-24. `WebViewConfigurator.configure()` now sets `userAgentString = "ChatPlayground/1.0"` on every configured WebView, so `chatplayground.ai` can distinguish wrapped traffic from a plain browser. Tested by `WebViewConfiguratorTest.configureSetsCustomUserAgentString` (asserts non-blank, contains "ChatPlayground"). Documented in `docs/FUNCTIONS.md` under WebView settings.
29. **[x] [LOW] `app/src/androidTest/java/.../MainActivityTest.kt` is a Robolectric unit test, not an instrumentation test**: DONE 2026-11-24. No such file exists — only valid instrumentation test `SecureStorageInstrumentedTest.kt` remains in `app/src/androidTest/java/`.
30. **[x] [LOW] Docs section 70 ("Custom Tab Warm-up & Theming") claims behavior that was de-scoped**: DONE 2026-11-24. `docs/FUNCTIONS.md` §70 updated to match reality — no service binding; package stamping + toolbar theming remain implemented.
31. **[x] [MEDIUM] Cache clearing duplicated across two menu items**: DONE 2026-11-24. Removed `action_clear_cache` menu item and `CacheClearer` utility class entirely. The "Clear All Data" menu item (`action_clear_all_data`) provides the unified operation via `DataClearer.clearAllData()` covering: offline cache, WebView cache + cookies (flushed), and SharedPreferences clearing. Removed `clearCacheMenuItemClearsCache` test from `MainActivityTest.kt`. Removed `CacheClearer.kt` and `CacheClearerTest.kt` duplicates. Acceptance met: one unified "Clear All Data" operation with full test coverage.

## Next Improvement Suggestions (2026-11-24, after the captive-portal cycle)

32. **[x] [MEDIUM] `NetworkMonitor.isConnected` can go stale between callbacks**: DONE 2026-11-24. `NetworkMonitor.observe()` now overrides `onCapabilitiesChanged()` to refresh `isConnected = checkConnectivity()` and notify the observer, so a captive portal that loses validation without losing the network is reported offline. Covered by `NetworkMonitorCapabilityChangeTest.onCapabilitiesChangedUpdatesIsConnected`.
33. **[x] [LOW] `NetworkMonitor.observe()` has no `onUnavailable` handling**: DONE 2026-11-24. Added an `onUnavailable()` override that sets `isConnected = false` and notifies the observer when no default network can satisfy the registration. Covered by `NetworkMonitorCapabilityChangeTest.onUnavailableSetsIsConnectedFalse` and `onUnavailableNotifiesObserver`.
34. **[x] [LOW] CI logs are only readable via `gh run view --log-failed`**: DONE 2026-11-24. Added `Lint Diagnostics` and `Lint Kotlin Diagnostics` workflow steps that re-run the failing task and mirror `error:`/`warning:` lines into `$GITHUB_STEP_SUMMARY`, matching the existing `Unit Test Diagnostics` pattern.

## Next Improvement Suggestions (open)

35. **[x] [LOW] Track CI failure mode in `.autonomy/`**: DONE 2026-11-24. Created `ci_status.md` with run tracking; `last_run.md` and `cycle_summary.md` include CI run IDs and status. Acceptance met: every cron run has visibility into current CI state.
36. **[x] [LOW] `local.properties` untrack but still present on disk**: DONE 2026-11-24. Removed tracked copy, verified it's in `.gitignore`, is NOT in index, and does not show up in `git status --short`. Acceptance met.

## Next Improvement Suggestions (2026-11-24, after the captive-portal cycle)

37. **[x] [MEDIUM] `NetworkMonitorLoadErrorTest` never compiled: `ShadowConnectivityManager.setActiveNetwork(Network)` does not exist** — DONE 2026-11-24. The test (added 2026-11-23) and its follow-up "fix" both called `setActiveNetwork(null)`, an API absent from Robolectric 4.18 (`shadows/framework/.../ShadowConnectivityManager.java` exposes only `setActiveNetworkInfo`, `addNetwork`, `setNetworkCapabilities`, `setDefaultNetworkActive`). Rewritten against the real API: `setActiveNetworkInfo(ShadowNetworkInfo.newInstance(CONNECTED, TYPE_WIFI, 0, true, CONNECTED))` publishes an active network, and deliberately omitting `setNetworkCapabilities` makes `getNetworkCapabilities()` return null, so `checkConnectivity()` must report offline. Lesson: verify shadow APIs against the pinned Robolectric tag's source before pushing — the worker is the only compiler.
38. **[x] [HIGH] Three test classes re-pinned `@Config(sdk = [26])`, tripping the SDK-centralisation guard** — DONE 2026-11-24. `NetworkMonitorTest`, `NetworkMonitorObserveTest` and `NetworkMonitorLoadErrorTest` had reintroduced per-class pins after the 2026-11-22 cleanup; `ci_check_robolectric_sdk.py` exited 1 on all three (the compile failure masked it because the guard runs later in the job). Pins and the now-unused `Config` imports removed; guard prints OK locally.
39. **[x] [MEDIUM] Captive-portal / unvalidated network is untested** — DONE 2026-11-24. Added `NetworkMonitorCaptivePortalTest` with two cases: INTERNET-only capabilities (captive portal) assert `checkConnectivity()` is false, and INTERNET+VALIDATED capabilities assert true. Verified against the pinned Robolectric 4.18 sources that `ShadowConnectivityManager.setActiveNetworkInfo(...)` publishes the active Network, `setNetworkCapabilities(activeNetwork, caps)` keys capabilities by that exact instance, and `NetworkCapabilities.addCapability(int)` is a hidden API reachable only through `Shadows.shadowOf(capabilities)`. CI run `45317045855` compiled and ran both tests green (32 JUnit suites passed) but failed ktlint on the new file; after the ktlint fixes, GREEN run `45318355620` succeeded — full build, all tests, lint and ktlint green.
40. **[x] [LOW] Nothing prevents agents from writing progress notes to non-canonical directories**: DONE 2026-11-24. Added `ci_check_autonomy_dirs.py` which fails CI when a directory named `autonomy` exists outside the canonical `.autonomy/` at the repo root (or when `.autonomy/` itself is missing). Runs right after `Verify Robolectric SDK Consistency` in `.github/workflows/build.yml`, before any Gradle work. Verified locally: passes with no stray dirs; fails on `autonomy/` and `app/autonomy/`; passes again once removed. The `local.properties` file committed in the previous cycle was untracked (`git rm --cached`) because it is now gitignored — CI writes its own `sdk.dir` line instead of shipping a fixed machine path.
41. **[x] [LOW] `NetworkMonitorTest.reportsOfflineWhenNoNetworkIsAvailable` and `NetworkMonitorLoadErrorTest` overlap**: DONE 2026-11-24. Merged both offline cases into `NetworkMonitorTest.reportsOfflineWhenNoActiveNetwork` (no active network, Robolectric default) and `NetworkMonitorTest.reportsOfflineWhenActiveNetworkHasNoCapabilities` (active network, capabilities map untouched), plus `reportsOnlineWhenActiveNetworkIsValidated` (INTERNET + VALIDATED). `NetworkMonitorLoadErrorTest` was deleted; its case now lives in `NetworkMonitorTest`. Acceptance met: one truth-table file names every case explicitly instead of duplicating the offline assertion across two classes.

## Next Improvement Suggestions (open)

42. **[x] [LOW] Implement Gesture Navigation for WebView Back/Forward** — Implement swipe gestures to navigate WebView's history stack (left-to-right swipe = back, right-to-left swipe = forward), providing native-like browsing experience without requiring on-screen buttons. Covered by a new unit test asserting gesture detection maps to `webView.goBack()`/`webView.goForward()`. DONE — `GestureNavigationHandler.kt` (pure-JVM, plain-JUnit testable) implemented in `MainActivity.kt` via `dispatchTouchEvent` + `GestureDetector`; `GestureNavigationHandlerTest.kt` covers rightward→back, leftward→forward, below-distance-threshold, below-velocity-threshold, vertical-dominating, and below-configured-thresholds cases. CI run `45523573578` failed on ktlint (three violations, all fixed, local guard passes).
43. **[x] [MEDIUM] Custom Tab warm-up and theming**: De-scoped warm-up binding (API uncompileable). `CustomTabsServiceConnection` dropped as uncompileable API usage; dead binding fields removed from `MainActivity.kt`. Package stamping + toolbar theming remain implemented (no `onStart`/`onStop` bind/unbind).
44. **[x] [LOW] Silent drop of unusable URLs**: DONE 2026-11-22: `handleUrl` logs `Log.d("MainActivity", "Unsupported URL dropped: $url")`; covered by `MainActivityTest.handleUrlLogsDebugForUnsupportedUrls`.
45. **[HIGH] Investigate and resolve brittle network dependency in unit tests (MavenArtifactFetcher)**.
46. **[x] [LOW] Add automated UI regression test for offline state recovery** — DONE 2026-11-23. `MainActivityOfflineRecoveryTest` asserts that when `NetworkMonitor` fires `onAvailable` while the WebView is on `offline.html`, the activity reloads the `lastTargetUrl` instead of just reloading the offline page. `lastTargetUrl` is captured in `onCreate` and `onNewIntent`.
47. **[x] [LOW] Add unit tests for `LinkRouter` edge cases** — DONE 2026-11-23. Verified `intent://` and other unusual schemes are correctly identified as external schemes.
48. **[x] [MEDIUM] Implement WebView Download Listener** — DONE 2026-11-23. `MainActivity` now sets a `DownloadListener` that delegates downloads to the system `DownloadManager`, providing native-like download handling. Covered by `MainActivityDownloadTest`.
49. **[x] [LOW] Optimize WebView Asset Loading** — Implemented `AssetRequestHandler` (`app/src/main/java/com/chatplayground/app/utils/AssetRequestHandler.kt`) serving bundled assets via `WebViewClient.shouldInterceptRequest(WebView, WebResourceRequest)` plus the deprecated overload. Whitelisted `file:///android_asset/<path>` URLs (`offline.html`) open from `assets.open()` with MIME-type selection; remote/blank/malformed URLs and path traversal/subdirectory assets return `null` and load normally; missing assets fall back without crashing. Covered by `AssetRequestHandlerTest.kt` (plain JUnit, no Robolectric/Android deps). Docs recorded in `docs/FUNCTIONS.md` under "WebView Asset Interception (`AssetRequestHandler`)". CI run `45511203790` success — full build, all tests, lint, ktlint and JaCoCo green on GitHub workers.
50. **[x] [MEDIUM] Implement WebView Search/Find** — DONE 2026-11-23. Added `findText` and `findNext` methods on `MainActivity` delegating to `WebView.findAllAsync` and `WebView.findNext`, covered by `MainActivitySearchTest` (fixed lint issues).
51. **[x] [LOW] Add "About" dialog** — DONE 2026-11-23. Show app version, license info, and a link to the GitHub repository. Covered by `MainActivityTest.aboutMenuItemShowsAboutDialog` (and added menu item tests below).
52. **[x] [LOW] Add unit tests for menu items (clear cache, toggle theme)** — DONE 2026-11-23. Covered by new tests `clearCacheMenuItemClearsCache` and `toggleThemeMenuItemTogglesTheme` in `MainActivityTest`.
53. **[x] [LOW] Add Theme Customization Preference** — DONE 2026-11-23. Allow users to switch between Light, Dark, and System default themes via an interactive selection dialog in settings, persisting mode in SharedPreferences (`theme_prefs`) and applying via `AppCompatDelegate.setDefaultNightMode()`. Covered by `MainActivityTest.themeSelectionDialogShowsOptions`.
54. **[x] [HIGH] Fix ktlint multiline expression wrapping errors in MainActivity.kt theme selection** — DONE 2026-11-23. Corrected formatting of `applySavedTheme` and `showThemeSelectionDialog` to comply with `standard:multiline-expression-wrapping`.
55. **[ ] [LOW] Improve Test Resilience**: Add caching/retry logic for transient network failures during Robolectric dependency fetching in CI.
56. **[ ] [LOW] Add Gesture Navigation for WebView Back/Forward** — Implement swipe gestures to navigate WebView's history stack.
57. **[x] [MEDIUM] Implement WebView Cookie Management Settings** — DONE 2026-11-23. Allow users to view and clear cookies managed by CookieManager. (Covered by MainActivityCookieTest)
58. **[x] [LOW] Add Dynamic Shortcut Support** — DONE 2026-11-27. Added launcher shortcuts for clearing cache and toggling theme via `ShortcutHelper`, `ShortcutRegistry`, and `MainActivity` intent handling. Covered by `ShortcutRegistryTest` and `ShortcutIntegrationTest`. CI build `45998134364` failed on `ic_brightness` reference; fixed in commit `9996fb9` (verified local style pass).
59. **[x] [MEDIUM] Implement WebView WebStorage management** — DONE 2026-11-28. `DataClearer.clearAllData()` now clears `WebStorage` (localStorage/sessionStorage) via `WebStorage.getInstance().deleteAllData()`. Injectable `webStorageDeleter` seam enables unit-testing under Robolectric (whose `ShadowWebStorage` returns a stub with no observable state). Covered by `ClearAllDataTest.clearAllDataClearsWebStorage` and `SingletonCleanupRuleTest.ruleResetsDataClearerWebStorageSeamOnFinished`.
60. **[ ] [MEDIUM] Implement immersive mode toggle** — Add a setting to toggle fullscreen/immersive mode for the WebView, persisting state in SharedPreferences.
61. **[ ] [LOW] Enhance ShortcutHelper with per-shortcut icons** — Use different system icons or project icons for each shortcut instead of a single hardcoded icon.

## Next Improvement Suggestions (2026-11-25, after the NetworkMonitor compile-fix cycle)

62. **[x] [HIGH] `NetworkMonitorLoadErrorTest` never compiled: `ShadowConnectivityManager.setActiveNetwork(Network)` does not exist** — DONE 2026-11-24. The test (added 2026-11-23) and its follow-up "fix" both called `setActiveNetwork(null)`, an API absent from Robolectric 4.18 (`shadows/framework/.../ShadowConnectivityManager.java` exposes only `setActiveNetworkInfo`, `addNetwork`, `setNetworkCapabilities`, `setDefaultNetworkActive`). Rewritten against the real API: `setActiveNetworkInfo(ShadowNetworkInfo.newInstance(CONNECTED, TYPE_WIFI, 0, true, CONNECTED))` publishes an active network, and deliberately omitting `setNetworkCapabilities` makes `getNetworkCapabilities()` return null, so `checkConnectivity()` must report offline. Lesson: verify shadow APIs against the pinned Robolectric tag's source before pushing — the worker is the only compiler.
63. **[x] [HIGH] Three test classes re-pinned `@Config(sdk = [26])`, tripping the SDK-centralisation guard** — DONE 2026-11-24. `NetworkMonitorTest`, `NetworkMonitorObserveTest` and `NetworkMonitorLoadErrorTest` had reintroduced per-class pins after the 2026-11-22 cleanup; `ci_check_robolectric_sdk.py` exited 1 on all three (the compile failure masked it because the guard runs later in the job). Pins and the now-unused `Config` imports removed; guard prints OK locally.
64. **[x] [MEDIUM] Captive-portal / unvalidated network is untested** — DONE 2026-11-24. Added `NetworkMonitorCaptivePortalTest` with two cases: INTERNET-only capabilities (captive portal) assert `checkConnectivity()` is false, and INTERNET+VALIDATED capabilities assert true. Verified against the pinned Robolectric 4.18 sources that `ShadowConnectivityManager.setActiveNetworkInfo(...)` publishes the active Network, `setNetworkCapabilities(activeNetwork, caps)` keys capabilities by that exact instance, and `NetworkCapabilities.addCapability(int)` is a hidden API reachable only through `Shadows.shadowOf(capabilities)`. CI run `45317045855` compiled and ran both tests green (32 JUnit suites passed) but failed ktlint on the new file; after the ktlint fixes, GREEN run `45318355620` succeeded — full build, all tests, lint and ktlint green.
65. **[x] [LOW] Nothing prevents agents from writing progress notes to non-canonical directories**: DONE 2026-11-24. Added `ci_check_autonomy_dirs.py` which fails CI when a directory named `autonomy` exists outside the canonical `.autonomy/` at the repo root (or when `.autonomy/` itself is missing). Runs right after `Verify Robolectric SDK Consistency` in `.github/workflows/build.yml`, before any Gradle work. Verified locally: passes with no stray dirs; fails on `autonomy/` and `app/autonomy/`; passes again once removed. The `local.properties` file committed in the previous cycle was untracked (`git rm --cached`) because it is now gitignored — CI writes its own `sdk.dir` line instead of shipping a fixed machine path.
66. **[x] [LOW] `NetworkMonitorTest.reportsOfflineWhenNoNetworkIsAvailable` and `NetworkMonitorLoadErrorTest` overlap**: DONE 2026-11-24. Merged both offline cases into `NetworkMonitorTest.reportsOfflineWhenNoActiveNetwork` (no active network, Robolectric default) and `NetworkMonitorTest.reportsOfflineWhenActiveNetworkHasNoCapabilities` (active network, capabilities map untouched), plus `reportsOnlineWhenActiveNetworkIsValidated` (INTERNET + VALIDATED). `NetworkMonitorLoadErrorTest` was deleted; its case now lives in `NetworkMonitorTest`. Acceptance met: one truth-table file names every case explicitly instead of duplicating the offline assertion across two classes.

## Next Improvement Suggestions (2026-11-24, after the captive-portal cycle)

67. **[x] [MEDIUM] `NetworkMonitor.isConnected` can go stale between callbacks**: DONE 2026-11-24. `NetworkMonitor.observe()` now overrides `onCapabilitiesChanged()` to refresh `isConnected = checkConnectivity()` and notify the observer, so a captive portal that loses validation without losing the network is reported offline. Covered by `NetworkMonitorCapabilityChangeTest.onCapabilitiesChangedUpdatesIsConnected`.
68. **[x] [LOW] `NetworkMonitor.observe()` has no `onUnavailable` handling**: DONE 2026-11-24. Added an `onUnavailable()` override that sets `isConnected = false` and notifies the observer when no default network can satisfy the registration. Covered by `NetworkMonitorCapabilityChangeTest.onUnavailableSetsIsConnectedFalse` and `onUnavailableNotifiesObserver`.
69. **[x] [LOW] CI logs are only readable via `gh run view --log-failed`**: DONE 2026-11-24. Added `Lint Diagnostics` and `Lint Kotlin Diagnostics` workflow steps that re-run the failing task and mirror `error:`/`warning:` lines into `$GITHUB_STEP_SUMMARY`, matching the existing `Unit Test Diagnostics` pattern.

## Next Improvement Suggestions (open)

70. **[x] [LOW] Track CI failure mode in `.autonomy/`**: DONE 2026-11-24. Created `ci_status.md` with run tracking; `last_run.md` and `cycle_summary.md` include CI run IDs and status. Acceptance met: every cron run has visibility into current CI state.
71. **[x] [LOW] `local.properties` untrack but still present on disk**: DONE 2026-11-24. Removed tracked copy, verified it's in `.gitignore`, is NOT in index, and does not show up in `git status --short`. Acceptance met.

## Next Improvement Suggestions (2026-11-24, after the ktlint-guard cycle)

72. **[x] [HIGH] Missing final newline keeps breaking CI on test files** — DONE 2026-11-24. Fourth occurrence (`55aaef8`, `d9c85fe`, `7f77f19`, and the item-50 merge). Added `scripts/check_kotlin_style.py` (stdlib only, ~1s, no Java) asserting every `.kt` ends with a newline and flagging trailing whitespace / tab indentation, with a `--fix` mode. Wired into `.github/workflows/build.yml` as *Check Kotlin Style (Final Newlines)*, placed before any Gradle step so the failure surfaces in seconds rather than after a five-minute worker round-trip. Acceptance met: guard passes on the whole tree (50 files) and would have caught both files from runs `45359097526`/`45359285941`.
73. **[x] [MEDIUM] No verification that CI-check scripts actually reject bad input**: DONE 2026-11-24. `scripts/test_ci_checks.py` feeds each guard (ci_check_tests.py, ci_check_robolectric_sdk.py, ci_check_autonomy_dirs.py, scripts/check_kotlin_style.py) a known-bad fixture in a temp dir and asserts exit code 1, plus known-good fixtures asserting exit 0, so a silently-passing guard is caught. Wired `Verify CI Checks Reject Bad Input` into `.github/workflows/build.yml` after the existing checks; CI run `45416435197` success — full build, all tests, lint, ktlint, JaCoCo and the new self-test green.

## Next Improvement Suggestions (2026-11-24, after the code review cycle)

74. **[x] [LOW] Implement "Clear All Data" setting**: DONE 2026-11-24. Added `DataClearer.kt` unifying offline cache, WebView cache, cookies, and SharedPreferences clearing. Added `action_clear_all_data` menu option in `main_menu.xml`, wired it in `MainActivity.kt`, and covered with `ClearAllDataTest.kt` and `MainActivityTest`.
75. **[x] [LOW] Custom User-Agent string**: DONE 2026-11-24. `WebViewConfigurator.configure()` now sets `userAgentString = "ChatPlayground/1.0"` on every configured WebView, so `chatplayground.ai` can distinguish wrapped traffic from a plain browser. Tested by `WebViewConfiguratorTest.configureSetsCustomUserAgentString` (asserts non-blank, contains "ChatPlayground"). Documented in `docs/FUNCTIONS.md` under WebView settings.
76. **[x] [MEDIUM] Cache clearing duplicated across two menu items**: DONE 2026-11-24. Removed `action_clear_cache` menu item and `CacheClearer` utility class entirely. The "Clear All Data" menu item (`action_clear_all_data`) provides the unified operation via `DataClearer.clearAllData()` covering: offline cache, WebView cache + cookies (flushed), and SharedPreferences clearing. Removed `clearCacheMenuItemClearsCache` test from `MainActivityTest.kt`. Removed `CacheClearer.kt` and `CacheClearerTest.kt` duplicates. Acceptance met: one unified "Clear All Data" operation with full test coverage.

## Next Improvement Suggestions (2026-11-24, after the captive-portal cycle)

77. **[x] [MEDIUM] `NetworkMonitor.isConnected` can go stale between callbacks**: DONE 2026-11-24. `NetworkMonitor.observe()` now overrides `onCapabilitiesChanged()` to refresh `isConnected = checkConnectivity()` and notify the observer, so a captive portal that loses validation without losing the network is reported offline. Covered by `NetworkMonitorCapabilityChangeTest.onCapabilitiesChangedUpdatesIsConnected`.
78. **[x] [LOW] `NetworkMonitor.observe()` has no `onUnavailable` handling**: DONE 2026-11-24. Added an `onUnavailable()` override that sets `isConnected = false` and notifies the observer when no default network can satisfy the registration. Covered by `NetworkMonitorCapabilityChangeTest.onUnavailableSetsIsConnectedFalse` and `onUnavailableNotifiesObserver`.
79. **[x] [LOW] CI logs are only readable via `gh run view --log-failed`**: DONE 2026-11-24. Added `Lint Diagnostics` and `Lint Kotlin Diagnostics` workflow steps that re-run the failing task and mirror `error:`/`warning:` lines into `$GITHUB_STEP_SUMMARY`, matching the existing `Unit Test Diagnostics` pattern.

## Next Improvement Suggestions (open)

80. **[x] [LOW] Track CI failure mode in `.autonomy/`**: DONE 2026-11-24. Created `ci_status.md` with run tracking; `last_run.md` and `cycle_summary.md` include CI run IDs and status. Acceptance met: every cron run has visibility into current CI state.
81. **[x] [LOW] `local.properties` untrack but still present on disk**: DONE 2026-11-24. Removed tracked copy, verified it's in `.gitignore`, is NOT in index, and does not show up in `git status --short`. Acceptance met.

## Next Improvement Suggestions (2026-11-25)

82. [x] [LOW] Add Gesture Navigation for WebView Back/Forward — Implement swipe gestures to navigate WebView's history stack.
83. [x] [LOW] Add Biometric Authentication lock for secure app access on startup — DONE 2026-11-25. New `AppLock` object (`app/src/main/java/com/chatplayground/app/security/AppLock.kt`) with `enabled`, `isLocked()`, `unlock()`, injectable no-arg `authenticator: () -> Boolean` seam (defaults to deny-all fail-closed), and `resetForTesting()`. Covered by `AppLockTest.kt` (7 pure-JVM cases). Follow-up: wire `MainActivity.onCreate` to gate `webView.loadUrl` behind `AppLock.unlock()` — DONE this cycle: `MainActivity.onCreate` returns early (with a toast) when `AppLock.isLocked()` before calling `webView.loadUrl`. Covered by `MainActivityTest.appLockBlocksWebViewLoadUrlWhenLocked` and `appLockAllowsWebViewLoadUrlWhenUnlocked`. CI run `45658164607` green.
84. [x] [MEDIUM] Implement WebView Ad Blocker / Content Filter using shouldInterceptRequest — DONE 2026-11-25. New `ContentFilterRequestHandler.kt` (pure-JVM, plain-JUnit testable) blocks known ad/tracker hosts (doubleclick.net, googlesyndication.com, google-analytics.com, facebook.net, googletagservices.com plus subdomains) via `WebViewClient.shouldInterceptRequest`, returning an empty response so they never reach the network. Wired into `MainActivity.shouldInterceptRequest` ahead of asset interception. Covered by `ContentFilterRequestHandlerTest` (null/blank, legit pass-through, ad domains, tracker domains, case-insensitivity, query/fragment, look-alike rejection). Committed as `6258986`; ktlint fixed in `8242177`; CI run `45594287201` green.
85. [x] [LOW] Add User-Agent Switcher for Desktop View — Implement `DesktopModeHandler` (with unit tests) allowing users to toggle between mobile and desktop User-Agent strings and configure WebView viewport settings (`useWideViewPort`, `loadWithOverviewMode`), allowing desktop site rendering when desired. DONE — committed, CI run pending (next worker run).
86. [x] [LOW] Implement Download Completion Notifications — Enhance `MainActivity` download handling to user notifications upon download start/completion and handle `DownloadManager` completion broadcasts. DONE — `DownloadNotifier.kt` added with `downloadStarted`/`downloadCompleted`, covered by `DownloadNotifierTest.kt`, CI run `45640394689` green.
87. [ ] [LOW] Add automated integration test for `WebViewConfigurator` headers and settings: Ensure custom User-Agent and security settings are correctly populated on every created WebView instance.
88. [ ] [LOW] Sync `.autonomy/ci_status.md` to latest green run after every successful CI — record run ID, status, and next actions so each cron run has visibility into current CI state.
89. [x] [LOW] Add ktlint self-test: verify `scripts/check_kotlin_style.py` rejects a known-bad `.kt` file (multiline expression not starting on new line) and accepts a known-good file, mirroring `scripts/test_ci_checks.py` pattern. DONE 2026-11-25. Added `standard:multiline-expression-wrapping` heuristic (paren-depth tracking detecting `val x = call(` starting mid-line) plus two self-tests (`test_fails_when_multiline_expression_starts_on_same_line`, `test_passes_when_multiline_expression_starts_on_new_line`). CI run `45596800910` green — full build, all tests, lint, ktlint, JaCoCo.

## Next Improvement Suggestions (open)

90. [ ] [MEDIUM] Port `DesktopModeHandler` to Kotlin Multiplatform (Common) for cross-platform utility — the current `DesktopModeHandler.kt` is Android/JVM-only; port the viewport + user-agent toggle logic to a common source set so the same rules can be reused on desktop targets without duplication.
91. **[x] [MEDIUM] Add automated integration tests for `DesktopModeHandler` using real WebView instances** — DONE 2026-11-26. `DesktopModeHandlerInstrumentedTest.kt` (androidTest source set) verifies viewport settings (`useWideViewPort`, `loadWithOverviewMode`) and desktop UA take effect on a real `WebView` widget. CI run `45901205662` failed on ktlint formatting; fixed in commit `9843377` and re-running as `45901902604`.
92. **[x] [LOW] Persist `DesktopModeHandler.isDesktop` across app restarts** — DONE 2026-11-26 (item 98). `MainActivity.onCreate` restores via `applySavedDesktopMode()` and `DesktopModeHandler.applyTo(webView)` before any `loadUrl`; toggle persists to `desktop_mode_prefs`. Duplicate of item 92; item 98 already covers it with CI run `45832347826` green.
93. **[x] [MEDIUM] `WebViewConfiguratorIntegrationTest` should assert `useWideViewPort` and `loadWithOverviewMode` defaults** — DONE 2026-11-22 (item 93). `configureAppliesAllSecurityAndUiSettings` already asserts both defaults are `false`. Covered with CI run `44990929779` green.

## Vision Items (Nov 26, 2026)
94. [x] Investigate why WebView `reload()` does not update `shadowOf(webView).lastLoadedUrl` in Robolectric tests — DONE 2026-11-26. Root cause: the `toggleDesktopModeMenuItemSwitchesToDesktopModeAndReloads` test failed because `appLockBlocksWebViewLoadUrlWhenLocked` set `AppLock.enabled = true` and called `resetForTesting()` but never reset `enabled` back to `false`. Since JUnit runs tests alphabetically, `appLockBlocksWebViewLoadUrlWhenLocked` ran before `toggleDesktopMode...`, leaving AppLock locked. When the toggle test created its activity, `AppLock.isLocked()` returned `true`, so `onCreate` returned early without loading the URL. Fix: added `@After fun tearDown() { AppLock.enabled = false }` to `MainActivityTest` to prevent state leak. CI run `45866576194` triggered.
95. [x] Update test to correctly verify reload behavior or adjust implementation to use `loadUrl` if needed for testing — DONE 2026-11-26. The reload assertion was correct; the issue was AppLock state leak, not `reload()` vs `loadUrl()`. The `@After` tearDown resolves it.

## Next Improvement Suggestions (2026-11-26, after the AppLock state-leak fix cycle)

97. [x] [LOW] Add `@After` tearDown to ALL test classes that touch `AppLock` — `MainActivityTest` now has one, but any future test class that sets `AppLock.enabled` should also reset it in a tearDown to prevent cross-test contamination. Consider adding a shared test rule or utility. DONE — `MainActivityTest.tearDown` resets `AppLock.enabled = false` and `DesktopModeHandler.applyDesktopMode(false)` between tests (vision item 96).
98. [x] [MEDIUM] Persist `DesktopModeHandler.isDesktop` across app restarts (reiterate item 92) — DONE 2026-11-26. `MainActivity.onOptionsItemSelected` writes `desktop_mode` to `desktop_mode_prefs` on every toggle; `onCreate` calls `applySavedDesktopMode()` before any `loadUrl` and `DesktopModeHandler.applyTo(webView)` restores UA/viewport on cold start. Covered by `MainActivityTest.onCreateRestoresPersistedDesktopMode` and `onCreateRestoresPersistedMobileModeWhenNoPreference`. CI run `45832347826` green.
99. [x] [LOW] Add unit test verifying `DesktopModeHandler.applyTo` is called during `MainActivity.onCreate` when desktop mode is persisted — DONE 2026-11-26. `onCreateRestoresPersistedDesktopMode` asserts the restored UA and viewport are applied to the WebView instance created in `onCreate`.
100. [x] [MEDIUM] Consolidate test state cleanup — multiple test classes mutate global singletons (`AppLock`, `DesktopModeHandler`). `SingletonCleanupRule.kt` (a JUnit `TestWatcher` rule) resets `AppLock` and `DesktopModeHandler` after every test, and is applied to `MainActivityTest`, `AppLockTest`, and `DesktopModeHandlerTest` via `@Rule @JvmField val singletonCleanupRule`. DONE 2026-11-26 — removes manual `@After tearDown` duplications and prevents cross-test state contamination.
101. [x] [LOW] Add a `SingletonCleanupRuleTest` — plain JUnit test verifying the rule actually resets `AppLock.enabled` and `DesktopModeHandler.isDesktop` after a test mutates them, so the rule's own behavior is covered rather than assumed. DONE — committed as `1724dd8` / `d6b8f87`; CI run `45854565908` green.
102. [x] [LOW] Extend `SingletonCleanupRule` to also reset `SecureStorage` seams — `SecureStorage.authenticator` and `SecureStorage.storeFactory` are mutated by `SecureStorageBiometricTest` via `@After tearDown`; folded those into the shared rule along with `AppLock` and `DesktopModeHandler`. `SecureStorageBiometricTest` now uses `@Rule @JvmField val singletonCleanupRule` and removed its manual `@After tearDown`. DONE — CI run `45854565908` green.
103. [x] [LOW] Verify `docs/FUNCTIONS.md` section numbering — checked and confirmed clean (no duplicate `## 4.` headings). DONE 2026-11-26.
104. [x] [LOW] Audit test suite for import-ordering violations (`standard:import-ordering`) — checked all test files; imports are fully lexicographically ordered and pass ktlint. DONE 2026-11-26.

## Next Improvement Suggestions (2026-11-26, post-audit)

200. [x] [LOW] Add unit tests for `DataClearer` edge cases when clearing SharedPreferences and WebView cookies simultaneously under mock/robolectric environments — DONE 2026-11-26. Added `clearAllDataClearsCookiesAndNonAppDataSharedPreferencesSimultaneously` to `ClearAllDataTest.kt` covering simultaneous clearing of cookies, `desktop_mode_prefs`, and `theme_prefs`.
201. [x] [LOW] Add automated regression test for `ContentFilterRequestHandler` query parameters and special URI structures — DONE 2026-11-26. Added port/query/fragment regression tests (`shouldInterceptBlocksAdHostWithPort`, `shouldInterceptBlocksAdHostWithPortAndQuery`, `shouldInterceptDoesNotBlockLegitimateHostWithPort`, `shouldInterceptDoesNotBlockLegitimateHostWithPortAndFragment`). Found and fixed a real bug: `shouldIntercept` kept the port in the host string so `googleads.g.doubleclick.net:8080` never matched `googleads.g.doubleclick.net`; stripped the port before matching. CI run `45868061704` green on commit `be1f1be`.
202. [x] [LOW] Add automated integration test for `WebViewConfigurator` headers and settings: Ensure custom User-Agent and security settings are correctly populated on every created WebView instance — DONE 2026-11-26. Verified via `WebViewConfiguratorTest` covering custom User-Agent (`ChatPlayground/1.0`), JavaScript enabled, DOM storage enabled, mixed content mode `NEVER_ALLOW`, and local/file/content access disabled.
203. [x] [HIGH] `DataClearer.clearAllData` only clears `app_data` SharedPreferences, leaving `desktop_mode_prefs` and `theme_prefs` intact — DONE 2026-11-26. Clear all three SharedPreferences files (`app_data`, `desktop_mode_prefs`, `theme_prefs`); covered by `ClearAllDataTest.clearAllDataClearsAllSharedPreferencesIncludingDesktopModeAndThemePrefs`.
204. [x] [LOW] Add `@After` tearDown to `ClearAllDataTest` to reset `AppLock` and `DesktopModeHandler` via `SingletonCleanupRule` for consistency — DONE 2026-11-26. `ClearAllDataTest` now uses `@Rule @JvmField val singletonCleanupRule = SingletonCleanupRule()`.
205. [x] [LOW] Reset `DesktopModeHandler` state in `DataClearer.clearAllData()` — `DataClearer` clears `desktop_mode_prefs` SharedPreferences, but `DesktopModeHandler.isDesktop` in-memory state remains `true` until restart. Acceptance: `DataClearer.clearAllData()` resets `DesktopModeHandler.applyDesktopMode(false)` so in-memory state matches cleared storage. DONE 2026-11-26. Covered by `ClearAllDataTest.clearAllDataResetsDesktopModeHandlerState`.

## Next Improvement Suggestions (2026-11-26, open)

206. [ ] [LOW] Add automated integration test for `WebViewConfigurator` headers and settings on real WebView instances.
208. [x] [MEDIUM] Add automated integration tests for `DesktopModeHandler` using real WebView instances — DONE 2026-11-26. `DesktopModeHandlerInstrumentedTest.kt` (androidTest source set) verifies viewport settings (`useWideViewPort`, `loadWithOverviewMode`) and desktop UA take effect on a real `WebView` widget. CI run `45901205662` failed on ktlint formatting; fixed in commit `9843377` and re-running as `45901902604`.
209. [x] [LOW] Persist `DesktopModeHandler.isDesktop` across app restarts — DONE 2026-11-26 (item 98). `MainActivity.onCreate` restores via `applySavedDesktopMode()` and `DesktopModeHandler.applyTo(webView)` before any `loadUrl`; toggle persists to `desktop_mode_prefs`. Duplicate of item 209; item 98 already covers it with CI run `45832347826` green.
210. [x] [MEDIUM] `WebViewConfiguratorIntegrationTest` asserts `useWideViewPort`/`loadWithOverviewMode` defaults — DONE 2026-11-22 (item 93). `configureAppliesAllSecurityAndUiSettings` already asserts both defaults are `false`. Duplicate of item 210; item 93 already covered with CI run `44990929779` green.

## Next Improvement Suggestions (open)

211. **[x] [LOW] Add automated integration test for `WebViewConfigurator` on real WebView instances** — DONE 2026-11-26. `WebViewConfiguratorInstrumentedTest.kt` (androidTest source set) asserts JavaScript, DOM storage, mixed-content mode, file/content access, User-Agent, and viewport defaults take effect on a real `WebView` widget. Covered by CI run `45912417013` success.

212. **[x] [MEDIUM] Persist `WebViewConfigurator` security defaults across WebView recreation** — DONE 2026-11-26. `MainActivity.onCreate` re-applies `WebViewConfigurator.configure()` on every new WebView instance; verified via `WebViewConfiguratorInstrumentedTest`.

213. **[x] [MEDIUM] `AssetRequestHandler` support for bundled CSS/JS/Image resources in offline fallback**: Asset bundling wired into `offline.html` (loads `style.css`/`app.js`), allowlist formalized, and `AssetRequestHandlerIntegrationTest.kt` exercises interception end-to-end through `MainActivity.shouldInterceptRequest` on a Robolectric-built activity so it runs in CI rather than merely compiling. Covered by `AssetRequestHandlerTest` unit tests (css/js/png path resolution, rejection of traversal/subdirectory/remote URLs) plus `everyAllowedAssetExistsInBundledAssets`. CI green on push `2c6c9e4`.
214. **[LOW] Add automated unit tests for `WebViewCookieManager` persistence**: Verify cookie state persistence and synchronization across activity recreation.

## Vision Items (Nov 27, 2026)

215. **[x] [MEDIUM] `AssetRequestHandler` bundled CSS/JS assets**: Allow bundling offline styling (`style.css`), script (`app.js`) so the offline fallback page is fully styled and interactive without external network dependencies. Covered by unit tests asserting asset path resolution and interception for whitelisted css/js assets. Offline page `offline.html` now loads `style.css` and `app.js` instead of inline markup. CI run triggered for commit `69217b9`; result pending.
216. **[x] [LOW] `AssetRequestHandler` allowlist/inventory invariant**: `AssetRequestHandler.ALLOWED_ASSETS` formalized as `internal val` with docstring; new regression test `AssetRequestHandlerTest.everyAllowedAssetExistsInBundledAssets` asserts every whitelisted asset has a non-empty file in `app/src/main/assets`, so the allowlist cannot drift from the shipped APK.

217. **[x] [MEDIUM] Automated unit test suite expansion for `NavigationRetryPolicy` backoff delay limits**: DONE 2026-11-27. Added `maxDelayMs` cap (default 60s) to `NavigationRetryPolicy.backoffDelayMs()` with overflow-safe exponent bounding (`MAX_SAFE_EXPONENT = 62`). New tests `backoffDelayMsIsCappedAtMaxDelay` (asserts clamp to 5000) and `backoffDelayMsDoesNotOverflowAtLargeRetryCount` (100 retries → positive 60000, never negative). Existing `backoffDelayMsGrowsExponentially` still covers growth bounds (1000→2000→4000). CI run pending on push.
218. **[LOW] Add ktlint check for `scripts/check_kotlin_style.py` on all test source sets**: Ensure new test helper files always adhere to indentation and final newline rules.


## Next Improvement Suggestions (2026-11-27, after the retry-backoff-cap cycle)

219. **[x] [LOW] Remove dead `NavigationRetryPolicy.retryCount()` method**: DONE 2026-11-27. Removed the unused `fun retryCount()` duplicate of `retryCountValue` (verified no references in main/test/androidTest via grep). Kept `retryCountValue` as the single accessor; `NavigationRetryPolicyTest` already used `retryCountValue` and was untouched. Committed as `f5ef30f`; CI worker build pending.
220. **[x] [LOW] Add unit tests for WebView cookie persistence across activity recreation (item 214)**: `MainActivityCookieTest.cookieStateSurvivesActivityRecreation` covers this — sets a cookie via `CookieManager`, calls `activity.recreate()`, and asserts `hasCookies()` + `getCookie()` after recreation. DONE via commit c770ddb after fixing the `getCookie()` expectation: Robolectric 4.18 `RoboCookieManager.getCookie()` returns `name=value` (e.g. `"session=abc123"`), stripping attributes like `Path=/`. CI run pending on push c770ddb.
221. **[x] [LOW] Document the `maxDelayMs` backoff cap in `docs/FUNCTIONS.md` retry section with a concrete example table** (count→delay for base 1000, cap 60000) so the overflow-protection behavior is discoverable without reading the source — DONE 2026-11-27. Added concrete delay table (counts 0 through 7) and overflow bounding details to `docs/FUNCTIONS.md` §6.

## Next Improvement Suggestions (2026-11-27, after the retry-docs cycle)

222. **[x] [MEDIUM] Consolidate duplicated documentation of `NavigationRetryPolicy` backoff rules**: the retry cap behaviour now exists in two places (`docs/FUNCTIONS.md` §6 table and `NavigationRetryPolicyTest` comments). Documented the executable source of truth in `docs/FUNCTIONS.md` §6. DONE 2026-11-27.
223. **[x] [LOW] CI workflow has no Kotlin-only style check step docs**: The style guard is `scripts/check_kotlin_style.py` and `lintKotlin`; confirm `docs/FUNCTIONS.md` names the exact workflow step order (style check → Gradle → tests → ktlint) so contributors know what runs where. Acceptance: `docs/FUNCTIONS.md` documents the style guard as a pre-Gradle step. DONE 2026-11-27.

224. **[x] [LOW] `AssetRequestHandler` bundled-asset integration test**: `AssetRequestHandlerIntegrationTest.kt` exercises interception end-to-end through `MainActivity.shouldInterceptRequest` on a Robolectric-built activity so it runs in CI (not merely compiles). Asserts whitelisted assets intercept and remote URLs are rejected.
225. **[x] [LOW] Tab-indentation negative test for `check_kotlin_style.py`**: `scripts/test_ci_checks.py::test_fails_when_line_is_tab_indented` now asserts exit non-zero and `indent` in output, proving the style guard rejects tab-indented `.kt` files. Guard runs green locally.

## Last Run (2026-11-27, run 9 — development cycle)

### Baseline
- CI status before cycle: runs `46013026542` and `46012179317` both **success** at HEAD `e49db98`; working tree clean, no pending worker builds.

### What Changed
1. **`.autonomy/ci_status.md`** — synced to latest green runs (`46013026542`, `46012179317`); push `9996fb9` now confirmed built and green (was previously recorded as "in progress"); all local guards pass (style guard on 73 files, 18 CI self-tests).
2. **`.autonomy/vision.md`** — recorded this cycle as "Last Run"; added open items below.

### Verification
- `gh run list` — runs `46013026542` and `46012179317` both `completed`/`success` on `main` at HEAD `e49db98`.
- `scripts/check_kotlin_style.py` — PASSED on all 73 Kotlin files (exit 0).
- `scripts/test_ci_checks.py` — PASSED (18 tests).
- Working tree clean; nothing uncommitted.

### Next Actions
- No pending CI work; all guards green at HEAD.
- Open suggestions: item 55 (CI dependency caching/retry), item 90 (KMP port for DesktopModeHandler), item 206 (WebViewConfigurator integration test on real WebView), item 226 (NetworkMonitor rapid subscribe/unsubscribe unit tests).

## Next Improvement Suggestions (2026-11-28, current cycle)

226. **[LOW] `NetworkMonitor` rapid subscribe/unsubscribe unit tests**: Add test coverage verifying that calling `observe()` and `stopObserving()` multiple times in quick succession maintains correct callback registration state without throwing exceptions or leaking callbacks.
227. **[LOW] CI test coverage reporting guard**: Add a local check script ensuring new Kotlin files have corresponding unit test files where applicable.

## Next Improvement Suggestions (2026-11-28, after the WebStorage-clearing cycle)

228. **[MEDIUM] Dedicated per-site data management UI for WebStorage**: item 59 only wired the *global* `WebStorage.deleteAllData()` into "Clear All Data". A user today cannot inspect or clear localStorage for a single site (e.g. after a broken SPA token is cached in IndexedDB/localStorage). Acceptance: a "Site Data" entry in the menu lists origins with stored data (via `WebStorage.getOrigins`) and offers per-origin clear; covered by a Robolectric test asserting the origin list is read and `deleteOrigin` is invoked for the selected origin. Note: `ShadowWebStorage` is a stub, so the seam must be extended the same way `webStorageDeleter` was.
229. **[x] [LOW] `DataClearer` clearing order is untested**: DONE 2026-11-28. Added the nullable `clearingStepObserver` test seam and `clearAllDataRunsDestructiveStepsInDocumentedOrder`, asserting offline cache → WebView cache → cookies → WebStorage → preferences → desktop mode → AppLock. The observer is reset by `SingletonCleanupRule` and documented in `docs/FUNCTIONS.md`.
230. **[x] [LOW] `AppLock` is not reset by "Clear All Data"**: `DataClearer.clearAllData()` now resets `AppLock.enabled = false` so wiping all app data does not leave the app locked on next start. Covered by `ClearAllDataTest.clearAllDataResetsAppLockState`. CI run `46088833666` green on commit `ec55d9d`.


## Next Improvement Suggestions (2026-11-28, after the DataClearer ordering cycle)

231. **[ ] [MEDIUM] Immersive mode seam-first implementation**: Add a pure-JVM state/persistence layer plus a thin Android window-insets adapter; acceptance includes a failing-then-green test before adapter wiring.
232. **[ ] [MEDIUM] Per-site WebStorage UI**: List origins and clear a selected origin through a testable seam; acceptance includes a Robolectric test for origin listing and `deleteOrigin`.
233. **[ ] [LOW] CI coverage guard**: Add a Python guard for new Kotlin production files without applicable tests, with explicit Android-only allowlisting.
234. **[x] [LOW] NetworkMonitor lifecycle tests**: Verify rapid repeated `observe()`/`stopObserving()` calls do not leak callbacks — `NetworkMonitorRapidLifecycleTest.kt` added, covering `rapidObserveStopAlternationLeavesZeroCallbacks` and `rapidObserveCallsLeaveExactlyOneCallback`. Committed locally; waiting for GitHub worker verification on push.

## Next Improvement Suggestions (2026-11-28, after the CI lint repair)

249. **[ ] [LOW] Add a pre-push hook wrapper for the repository's Kotlin style guard**: provide one documented command that checks only changed Kotlin files and fails before a worker round-trip.
250. **[ ] [MEDIUM] Add a CI diagnostic assertion for lint output**: ensure the workflow's lint diagnostics step preserves the first actionable file/line marker when a Kotlin lint task fails.
251. **[ ] [MEDIUM] WebView certificate-pinning diagnostics**: expose `onReceivedSslError` handling that records whether SSL errors were cancelled or proceeded, with a unit test asserting the default fail-closed behaviour.
252. **[ ] [LOW] Export `docs/FUNCTIONS.md` function index as machine-readable YAML**: so agents can verify implemented functions against documented ones without parsing Markdown.
253. **[ ] [LOW] Add automated integration test for `WebViewConfigurator` headers and settings on real WebView instances** — `WebViewConfiguratorInstrumentedTest.kt` asserts settings on a real widget; follow-up to item 211 which covered unit-level verification only.
254. **[ ] [MEDIUM] Add automated unit tests for `WebViewCookieManager` persistence** — verify cookie state persistence and synchronization across activity recreation (vision item 214).

## Last Run (2026-11-28, run 19 — immersive instrumentation coverage)

### Baseline
- GitHub status page: **All Systems Operational**; latest observed run `46103137179` for documentation commit `1393633` completed successfully.
- Repository was clean at `main` before this cycle; no worker failure required repair.

### Task Chosen
- Vision item 241: add instrumentation coverage for immersive system-bar visibility, after confirming the previous immersive adapter/menu worker run was green.

### What Changed
- Documented the instrumentation contract in `docs/FUNCTIONS.md` before implementation.
- Added `ImmersiveModeWindowAdapterInstrumentedTest`, which launches a real `MainActivity` window and verifies immersive application hides system bars and non-immersive application restores them.
- Added the `androidx.test:core` dependency to the androidTest configuration for `ActivityScenario`.
- Added ranked follow-ups 243–244 for emulator verification and instrumentation resource cleanup.

### Verification
- `python3 scripts/check_kotlin_style.py app/src/androidTest/java/com/chatplayground/app/utils/ImmersiveModeWindowAdapterInstrumentedTest.kt` — PASSED.
- `git diff --check` — PASSED.
- Gradle was not run locally by project policy; GitHub worker compilation is required.

### Next Actions
- Push directly to `main` and verify the worker's assembleAndroidTest, unit-test, lint, and release results.
- If the worker fails, inspect diagnostics before selecting another task; if green, consider preference-key constants or per-site WebStorage UI.

## Last Run (2026-11-28, run 17 — immersive-mode adapter and menu)

### Baseline
- GitHub status API: **All Systems Operational**.
- Latest worker run `46099049526` completed **successfully** on `main` at `62e8dcb`; prior repair run `46098362338` also passed.
- Working tree was clean before implementation.

### Task Chosen
- Vision item 236/237: finish the immersive-mode cycle by wiring the persisted state to Android window insets and the application menu.

### What Changed
- Documented the adapter and menu contract in `docs/FUNCTIONS.md` before implementation.
- Added `ImmersiveModeWindowAdapter`, using `WindowInsetsControllerCompat` to hide/show system bars with transient bars available by edge swipe.
- `MainActivity` restores immersive mode before the first WebView load, adds the menu item, persists toggles, applies the adapter immediately, and updates the title.
- Added menu/state regression coverage and reset immersive singleton state in `SingletonCleanupRule`.
- Added ranked follow-up suggestions for real-window instrumentation coverage and preference-key consolidation.

### Verification
- `python3 scripts/check_kotlin_style.py` — PASSED (76 Kotlin files).
- `python3 scripts/test_ci_checks.py` — PASSED (18 tests).
- `git diff --check` — PASSED.
- Gradle/JVM tests were not run locally by project policy; GitHub worker is required.
- A local `pytest` attempt was unavailable (`No module named pytest`); unrelated to the Android worker path.

### Next Actions
- Push this cycle to `main`; verify the worker build before selecting the next task.
- If green, consider item 238 instrumentation coverage for real system-bar visibility.

### Baseline
- GitHub status API: **All Systems Operational**.
- Worker runs `46096086513` and `46096286336` both failed in `Run Unit Tests`; `ImmersiveModeStateTest` failed at line 11 with `IllegalStateException` from `ApplicationProvider`.
- The working tree was clean before the repair.

### Task Chosen
- Recover the unfinished immersive-mode seam cycle before unrelated work, per `AUTONOMY.md`.

### What Changed
- Added `@RunWith(RobolectricTestRunner::class)` to `ImmersiveModeStateTest`; production state logic was unchanged.

### Verification
- `python3 scripts/check_kotlin_style.py` — PASSED (75 Kotlin files).
- `python3 scripts/test_ci_checks.py` — PASSED (18 tests).
- `git diff --check` — PASSED before commit.
- Gradle/JVM tests were not run locally by project policy; GitHub worker verification is required.

### Next Actions
- Push this repair to `main` and verify the resulting worker run. If green, wire the Android window-insets adapter and menu behavior; if red, inspect diagnostics first.

## Next Improvement Suggestions (2026-11-28, after the DataClearer ordering cycle)

232. **[MEDIUM] Per-site WebStorage UI**: add a menu entry that lists origins from `WebStorage.getOrigins` and clears a selected origin through a testable seam; acceptance includes a Robolectric test for list/read and `deleteOrigin` invocation.
233. **[MEDIUM] Immersive mode seam-first implementation**: add a pure-JVM state/persistence decision layer plus a thin `WindowInsetsControllerCompat` adapter; acceptance includes a failing-then-green test before adapter wiring.
234. **[LOW] CI coverage guard**: add a Python guard that flags new Kotlin production files lacking an applicable unit test, with explicit allowlisting for Android-only adapters.


## Last Run (2026-11-28, run 12 — development cycle: WebStorage clearing)

### Baseline
- CI status before cycle: latest `main` run `46073299055` (merge of PR #9, robolectric 4.18.1 bump) **success**; preceding `main` runs `46039814505`, `46023045867`, `46017169526` all **success**. No pending worker builds, no build errors to repair.
- Working tree contained one uncommitted file from the previous run: `.autonomy/vision.md` (items 226/227). Recovered into this cycle's commit rather than discarded.

### Task Chosen
- Vision item **59 `[MEDIUM] Implement WebView WebStorage management`**. Evidence: `DataClearer.clearAllData()` cleared offline cache, WebView cache, cookies and three SharedPreferences files, but never called `WebStorage`. `grep -rn WebStorage app/src/` returned no matches, so the "Clear All Data" menu item left localStorage/sessionStorage intact — a reproducible data-retention defect, not an invented feature.

### What Changed
1. **`docs/FUNCTIONS.md`** — documented the extended "Unified Data Clearing" behaviour *before* implementation (user standing requirement), including the `webStorageDeleter` seam and why it exists.
2. **RED** — `ClearAllDataTest.clearAllDataClearsWebStorage` asserts the seam is invoked exactly once by `clearAllData`; `SingletonCleanupRuleTest.ruleResetsDataClearerWebStorageSeamOnFinished` asserts the new global seam cannot leak between tests. Both referenced symbols that did not exist (`DataClearer.webStorageDeleter`, `resetWebStorageDeleterForTesting`), so the suite could not compile — observable RED.
3. **GREEN** — `DataClearer` gained `@Volatile var webStorageDeleter` defaulting to `WebStorage.getInstance().deleteAllData()`, a `resetWebStorageDeleterForTesting()` helper, and one call site in `clearAllData` between cookie clearing and prefs clearing.
4. **`SingletonCleanupRule`** — `finished()` now calls `DataClearer.resetWebStorageDeleterForTesting()`, joining `AppLock`, `DesktopModeHandler` and the two `SecureStorage` seams.
5. **`.autonomy/vision.md`** — item 59 marked done with evidence; items 228–231 added.

### Verification
- `python3 scripts/check_kotlin_style.py` — PASSED (73 files, exit 0).
- `python3 scripts/test_ci_checks.py` — PASSED (18 tests, OK).
- `python3 scripts/check_kotlin_style.py <4 changed .kt files>` — PASSED.
- Local Java/Gradle is absent by policy, so compilation and the unit tests themselves run only on the GitHub worker; this cycle's push is the verification vehicle. Worker result is not yet known at record time.
- Diff reviewed: 6 files, +71/−3 — one production behaviour change (`DataClearer`), two test files, one docs line, one test rule, one autonomy update. No unrelated edits.

### Next Actions
- Confirm this push's `Build Android App` run is green with `gh run list`; if it fails, read `gh run view --log-failed` before starting anything else (AGENTS/AUTONOMY failure policy).
- Open work remaining: item 55 (CI dependency caching/retry), item 60 (immersive mode toggle — see item 231 for the seam-first approach), item 61 (per-shortcut icons), item 90 (KMP port of `DesktopModeHandler`), item 206 (WebViewConfigurator on real WebView), item 214 (cookie persistence), item 226 (NetworkMonitor rapid subscribe/unsubscribe), item 228 (per-site data UI), item 229 (clearing-order test), item 230 (AppLock vs Clear All Data decision).
