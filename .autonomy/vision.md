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

## Next Improvement Suggestions (2026-11-22)

1. **[MEDIUM] ktlint enforcement is documented but absent**: `docs/FUNCTIONS.md` §4 claims `org.jmailen.gradle.kotlinter` 4.3.0 is applied and a CI `ktlintCheck` step blocks the pipeline; neither exists in `app/build.gradle` or `.github/workflows/build.yml`. Acceptance: either implement it (with formatting fixed first, in a dedicated cycle) or correct the docs. Prefer correcting the docs in the same cycle that implements it.
2. **[MEDIUM] WebView Custom Tabs for external links**: route off-domain `http(s)` links through `androidx.browser` CustomTabsIntent so store/payment flows keep a trusted chrome and the back stack returns to the app.
3. **[MEDIUM] CI JaCoCo artifacts**: the `jacocoTestReport` task disables every report (`xml/html/csv.required = false`) while CI uploads `app/build/jacoco/*.exec`; AGP 8 writes coverage to `app/build/outputs/unit_test_coverage/`. Acceptance: one report format enabled and the upload path matches where AGP actually writes `.exec`/`coverage.ec`.
4. **[LOW] Lint baseline for pre-existing warnings**: add `app/lint-baseline.xml` via `android { lint { baseline = file("lint-baseline.xml") } }` so new lint warnings are attributable.
5. **[HIGH] Robolectric test-SDK consistency**: unit tests mix `@Config(sdk = [34])`, `[30]`, `[26]` and an implicit default. Robolectric 4.18 lacks full WebView/Keystore support at 34, which produced `NoClassDefFoundError` and `KeyStoreException`. Acceptance: every Robolectric test pins a supported SDK (or the SDK is set once in `testOptions`) and no test depends on an unpinned default.
6. **[MEDIUM] SecureStorage test seam for the preference store**: `SecureStorage` hard-codes `EncryptedSharedPreferences` + `MasterKeys` (AndroidKeyStore), which cannot run under Robolectric, so `SecureStorageTest` swallows every exception and asserts nothing. Acceptance: inject the prefs backing store behind an interface, default it to the encrypted implementation in production, and let tests use an in-memory implementation so the round-trip assertions are real.
7. **[LOW] Rename the misleading CI step**: the step named `Upload JUnit Test Results` is a `run:` step that only copies XMLs into `junit-results/`; either make it `actions/upload-artifact` or rename it to `Collect JUnit Test Results (post-failure)`. A duplicate `Collect JUnit Test Results` step also exists earlier in the job.

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
