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
2. **[MEDIUM] Docs claim ktlint enforcement that does not exist**: `docs/FUNCTIONS.md` §4 describes `org.jmailen.gradle.kotlinter` 4.3.0 applied to `app` plus a CI `ktlintCheck` step; neither `app/build.gradle` nor `.github/workflows/build.yml` contains it. Acceptance: implement it (formatting pass first) or delete §4.
3. **[MEDIUM] Custom Tab warm-up and theming**: `MainActivity.launchInCustomTab` builds a default `CustomTabsIntent` on every click — cold start, default toolbar, visible flip. Acceptance: one `CustomTabsClient` warm-up bound in `onStart` and released in `onStop`, toolbar colour taken from the app theme, and a test asserting the launched intent carries the browser package.
4. **[MEDIUM] JaCoCo report is disabled**: `jacocoTestReport` sets `xml.required = html.required = csv.required = false`, so CI uploads only raw `.exec` data (35 KB) and no human-readable coverage. Acceptance: enable `xml` (and `html`), upload `app/build/reports/jacoco/**`, fail if the report is absent.
5. **[LOW] Misleading CI step name**: `Upload JUnit Test Results` (`.github/workflows/build.yml:111`) is a `run:` step that only copies XMLs and duplicates `Collect JUnit Test Results` (line 66). Acceptance: rename it to `Collect JUnit Test Results (post-failure)` or drop the duplicate.
6. **[LOW] Silent drop of unusable URLs**: `MainActivity.handleUrl` returns `true` for URLs that match no rule (blank/malformed) without any trace. Acceptance: log at debug level (or show a toast) so unsupported schemes are diagnosable.
7. **[MEDIUM] WebView back-button history navigation**: pressing the system Back button closes the activity immediately, discarding in-WebView navigation history. Acceptance: override `onBackPressed` so Back first calls `webView.goBack()` when the WebView has a back entry; only falls through to `super` (activity finish) when there is no history. Covered by a `MainActivityTest` case asserting goBack is invoked and finish is not.
8. **[MEDIUM] Mixed Content / HTTPS enforcement**: `WebViewSettings` allow mixed content on several API levels, so an HTTPS page loading HTTP sub-resources triggers silent mixed-content warnings or blocked loads. Acceptance: set `WebViewSettings.MixedContentHandling` to `NEVER_ALLOW` (or `ALWAYS_ALLOW` gated behind a user setting), plus a unit test asserting the setting.

## Completed (2026-11-22, Custom Tabs cycle)
- **MEDIUM: WebView Custom Tabs for external links**: added `LinkRouter`
  (`isInAppUrl` / `isExternalWebUrl` / `isExternalScheme`, `java.net.URI` based) and
  routed `MainActivity.shouldOverrideUrlLoading` through `handleUrl`. Only
  user-initiated (`request.hasGesture()`) off-domain links leave the WebView, so
  OAuth redirects and form posts keep their in-WebView session; non-web schemes still
  go to `ACTION_VIEW`. `externalLinkLauncher` is the injectable seam;
  `androidx.browser:browser:1.8.0` added. CI run `44979464647`: green, 19 suites
  (new `LinkRouterTest` + 3 new `MainActivityTest` cases).
- RED evidence: run `44978726869` failed with `Unresolved reference: LinkRouter`
  and `externalLinkLauncher` before the implementation was pushed.

## Completed (2026-11-22)
- **HIGH: Unit-test failures now fail CI**: Removed `continue-on-error: true` from the `Run Unit Tests` step in `.github/workflows/build.yml`, added a `Collect JUnit Test Results` step that copies `app/build/test-results/**/*.xml` into `junit-results/`, and a `Check Unit Test Results` step (`ci_check_tests.py`) that fails the pipeline when any `<testsuite>` reports non-zero `failures`/`errors` or when no JUnit reports exist. Documented in `docs/FUNCTIONS.md` §1.
- **HIGH: Robolectric runtime test failures fixed** (2026-11-22 late cycle): `MainActivityLoadingIndicatorTest` pinned to `sdk = [26]` (WebView works there; `sdk = 34` threw `NoClassDefFoundError`); `NetworkMonitorObserveTest` repinned to `sdk = [26]` (an unpinned `@Config(minSdk = …)` made the whole class fail in `DefaultSdkProvider`) and now drives `onAvailable`/`onLost` through Robolectric's registered-callback set, asserting the no-leak invariant; `SecureStorage` gained an injectable `EncryptedStore` seam (`SecureStorage.storeFactory`) so biometric tests exercise real read/write behavior in memory instead of dying on AndroidKeyStore.
- Deep Link Integration: `DeepLinkResolver`, `https`/`www` App Link filters + `chatplayground://` scheme filter, `singleTop` re-delivery via `onNewIntent`, covered by `DeepLinkResolverTest`, `ManifestDeepLinkTest` and `MainActivityDeepLinkTest`.

## Suggestions carried over from 2026-11-21
1. **[MEDIUM] CI JaCoCo Artifacts**: "No files were found" errors for JaCoCo coverage artifacts — still open, see item 4 above.
2. **[x] [MEDIUM] Biometric Authentication for Secure Storage** — DONE 2026-11-21: `SecureStorage.authenticateWithBiometric` / `getStringWithBiometric` guard credential reads with `BiometricPrompt`.
3. **[MEDIUM] WebView Custom Tabs** — carried over as item 3 above.
4. **[x] [MEDIUM] Deep Link Integration** — DONE 2026-11-22.
5. **[LOW] ktlint / Android Lint baseline** — carried over as items 2 and 5 above.

## Completed (2026-11-21)
- Fixed CI lint gate false positive by removing faulty `grep -q "warning"` check against HTML lint reports. Gradle's native lint exit code now controls build success.
- Improved Android build stability (retry logic, Robolectric cache, API 24 compat).
- Added network error toast.

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
