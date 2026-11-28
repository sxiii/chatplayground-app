# Functions & CI/CD Documentation: ChatPlayground Android Wrapper

This document describes the core build, CI/CD, and application runtime functions for `chatplayground-app`.

## 1. Build Functions (GitHub Actions CI/CD)

### `build-android` Workflow (`.github/workflows/build.yml`)
- **Trigger:** Triggers automatically on every `push` (and `pull_request`) to the main branch, as well as manually via `workflow_dispatch`.
- **Environment:** Runs on `ubuntu-latest`.
- **Steps:**
  1. **Checkout Repository:** Checks out the source code using `actions/checkout@v4`.
  2. **Set up JDK:** Configures JDK 17 (Temurin distribution) and enables the Gradle build-cache using `actions/setup-java@v5` (`cache: 'gradle'`).
  3. **Set up Android SDK:** Provisions the Android command-line tools, license acceptance, and `ANDROID_HOME` / `ANDROID_SDK_ROOT` / PATH via a SHA-pinned `android-actions/setup-android` action, declaring the required components through its `packages` input (`platform-tools`, `platforms;android-34`, `build-tools;34.0.0`) with `cmdline-tools-version: latest`.
  4. **Grant Gradle Wrapper Execution Permission:** Ensures `gradlew` is executable (`chmod +x gradlew`).
  5. **Build Debug APK:** Compiles and assembles the debug build via `./gradlew assembleDebug --stacktrace --console=plain`.
  6. **Assemble Android Test APK (Instrumentation Tests):** Compiles the `androidTest` source set to catch compilation errors in device-bound code without needing an emulator.
  7. **Run Unit Tests:** Executes the JVM unit-test suite via `./gradlew testDebugUnitTest --no-daemon --stacktrace --console=plain`. The step no longer swallows failures — any non-zero exit blocks the pipeline.
  8. **Collect JUnit Test Results:** Copies `app/build/test-results/**/*.xml` into `junit-results/` so per-suite failures are inspectable without admin log access.
  9. **Check Unit Test Results:** Scans `junit-results/*.xml` via `ci_check_tests.py` and fails the pipeline when any `<testsuite>` reports non-zero `failures` or `errors`, or when no JUnit reports exist at all.
  10. **Verify Dependencies:** Runs `./gradlew androidDependencies` to check resolution.
  11. **Generate JaCoCo Report:** Attempts to generate code coverage via `./gradlew jacocoTestReport`.
  12. **Run Lint:** Executes `./gradlew lint` and fails if any warnings are found in `lint-results.html`.
  13. **Build Release APK:** Assembles the (unsigned) release build via `./gradlew assembleRelease --stacktrace --console=plain`.
  14. **Upload Artifacts:** Uploads the compiled APK outputs and JaCoCo execution data using `actions/upload-artifact@v4`.

### `robolectric.properties` + `ci_check_robolectric_sdk.py` (test SDK guard)
- **`app/src/test/resources/robolectric.properties`** — the single source of truth for the Android SDK that Robolectric emulates in unit tests (`sdk=26`). Robolectric 4.18 cannot instantiate `WebView` at API 34 (`NoClassDefFoundError`), and Android Keystore is unavailable under Robolectric, so 26 is the pinned, verified baseline for every suite.
- **`ci_check_robolectric_sdk.py`** — guard run in CI after `Check Unit Test Results` (and usable locally). It fails the build when the properties file is missing, when its `sdk` value is not exactly `26`, or when any test source under `app/src` still pins an SDK through `@Config(sdk = …)` / `@Config(minSdk = …)` / `@Config(maxSdk = …)`. The scan covers `app/src` in full (both `test` and `androidTest` source sets).

### `autonomy/` guard (`ci_check_autonomy_dirs.py`)
- **Purpose:** Canonical autonomy state lives only in `.autonomy/` at the repository root.
- **Behaviour:** Fails when a directory named `autonomy` exists anywhere other than `.autonomy/` at the root, or when `.autonomy/` itself is missing.

### `scripts/check_kotlin_style.py` (Final Newlines & Formatting Guard)
- **Purpose:** Fast, pure-Python guard asserting every `.kt` file ends with exactly one newline, uses no tab indentation, and carries no trailing whitespace.
- **Workflow Position:** Executed as an early-stage check alongside other Python-based style check and audit guards. In the CI pipeline, the exact checks and build steps follow the order: Style Check (`check_kotlin_style.py`) → Gradle Compile/Assemble → Unit Tests → Formatting/Linting (`lintKotlin` and `lint`), ensuring syntax/style errors fail the build within seconds before expensive Android compile or Gradle tasks run.
- **Self-Test:** Includes a multiline-expression-wrapping heuristic that rejects code where a multiline call starts on the same line as its variable declaration, mirroring the project's ktlint policy.

## 2. Local Gradle Wrapper Utility
- `./gradlew`: Bootstraps Gradle using version 8.5 specified in `gradle/wrapper/gradle-wrapper.properties`.

## 3. Application Runtime Functions

### Offline Support & Caching (`OfflineCacheManager`)
- **`saveCachedResponse(url: String, data: String)`**: Caches response data for offline access in app cache storage.
- **`getCachedResponse(url: String): String?`**: Retrieves cached content when network connection is unavailable.
- **`clearCache()`**: Clears stored offline cache entries. (Now part of unified `DataClearer`).

### Push Notifications (`NotificationService`)
- **`handleTokenRefresh(token: String)`**: Registers and updates Firebase Cloud Messaging (FCM) device tokens.
- **`displayNotification(title: String, body: String)`**: Displays push notifications in the notification tray when received.

### Network Monitoring (`NetworkMonitor`)
- **`isOnline(): Boolean`**: Checks the active network capabilities for an available Internet transport (cellular, Wi-Fi, Ethernet).
- **`observeNetworkState(callback: (Boolean) -> Unit)`**: Registers a `ConnectivityManager.NetworkCallback` to stream online/offline network transitions in real-time. Overrides `onCapabilitiesChanged` to refresh validation (detecting captive portals) and `onUnavailable` to handle default-network loss.

### WebView UI & Loading (`MainActivity`)
- **Loading Indicator**: A `ProgressBar` overlay visible during page loads.
- **Offline Fallback**: Overrides `onReceivedError` to load `offline.html` when a main-frame network error occurs.
- **External Browser**: Menu option to open current URL in the system browser.
- **Safe Link Interception**: `shouldOverrideUrlLoading` intercepts non-HTTP/HTTPS schemes (`mailto:`, `tel:`) to launch system intents.
- **Custom Tabs for External Links**: User-initiated off-domain `http`/`https` links open in a secure Custom Tab with app-themed chrome.
- **Unified Data Clearing**: `action_clear_all_data` menu option delegates to `DataClearer` to wipe all caches, cookies, WebStorage (localStorage/sessionStorage), and SharedPreferences, then reset in-memory desktop mode and AppLock state. `DataClearer.webStorageDeleter` is an injectable seam (defaults to `WebStorage.getInstance().deleteAllData()`) so the clearing call can be unit-tested under Robolectric, whose `ShadowWebStorage` returns a stub with no observable state. `DataClearer.clearingStepObserver` is a test-only nullable callback seam that records the documented order without changing production behavior: offline cache, WebView cache, cookies, WebStorage, preferences, desktop mode, then AppLock.
- **Theme Selection**: Interactive dialog for Light/Dark/System themes, persisted in `theme_prefs`.
- **Back-Button History Navigation**: `OnBackPressedCallback` navigates `webView.goBack()` while history exists.
- **Lifecycle Teardown**: `onDestroy` detaches the WebView before calling `destroy()` to safely release the renderer binding.
- **Download Handling**: Sets a `DownloadListener` delegating to system `DownloadManager`.
- **Download Notifications**: `DownloadNotifier` sends system notifications on download start and completion.
- **Search/Find**: `findText` and `findNext` methods wrap `WebView.findAllAsync` and `WebView.findNext`.

### WebView Settings (`WebViewConfigurator`)
- **`configure(webView: WebView)`**: Applies app-wide policy: JavaScript enabled, DOM storage enabled, Mixed Content `NEVER_ALLOW`, local file/content access disabled, and custom User-Agent (`ChatPlayground/1.0`).

### Immersive Mode (`ImmersiveModeState`, `ImmersiveModeWindowAdapter`)
- **`ImmersiveModeState.load/toggle`**: Loads and persists the user's immersive-mode choice in `immersive_mode_prefs`.
- **`ImmersiveModeWindowAdapter.apply`**: Applies the persisted choice to the activity window by hiding or showing system bars through `WindowInsetsControllerCompat`; transient bars remain available with an edge swipe.
- **Menu integration**: `action_toggle_immersive_mode` toggles the persisted state, applies it immediately, and updates the menu title. The adapter is injectable behind a small window-controller interface so state and menu behavior remain testable without a device.

### Desktop Mode (`DesktopModeHandler`)
- **`applyDesktopMode(enabled: Boolean)`**: DERIVES User-Agent and viewport settings (`useWideViewPort`, `loadWithOverviewMode`) for a desktop-like browsing experience.
- **Integration**: Toggled via menu; persisted in `desktop_mode_prefs`; applied in `onCreate` before any load.

### Content Filtering (`ContentFilterRequestHandler`)
- **`shouldIntercept(url: String): Boolean`**: Pure-JVM filter blocking known ad/tracker hosts (Google Ads, DoubleClick, Facebook Net) via `shouldInterceptRequest`. Host matching is case-insensitive and ignores ports/queries.

### WebView Remote Debugging (`WebViewDebugging`)
- **`configure(debugBuild: Boolean)`**: Enables `setWebContentsDebuggingEnabled(true)` only for debug builds.

### Link Routing (`LinkRouter`)
- **`isInAppUrl(url: String?): Boolean`**: Predicate for `chatplayground.ai` hosts.
- **`isExternalWebUrl(url: String?): Boolean`**: Predicate for other web links (destined for Custom Tabs).

### Secure Storage (`SecureStorage`)
- **`putString` / `getString`**: Encrypted persistence using `EncryptedSharedPreferences` (AES-256-GCM).
- **`authenticateWithBiometric`**: Displays system biometric prompt.
- **`getStringWithBiometric`**: Decrypts value only after successful biometric authentication.

### App Lock (`AppLock`)
- **Startup Gate**: If `enabled`, gates the initial `webView.loadUrl` behind a successful biometric `unlock()`. Unlocked state persists for the process life.

### Immersive Mode (`ImmersiveModeState`, `ImmersiveModeWindowAdapter`)
- **`isImmersive`**: State indicating whether fullscreen immersive mode is enabled.
- **`load(prefs)` / `toggle(prefs)`**: Reads and persists the `immersive_mode` preference so the choice survives activity recreation.
- **`ImmersiveModeWindowAdapter.apply(window, immersive)`**: Uses `WindowInsetsControllerCompat` to hide/show system bars, with transient bars available through an edge swipe.
- **Integration**: `MainActivity` restores the setting before the first page load; the menu toggles it immediately and updates between "Enter Immersive Mode" and "Exit Immersive Mode".
- **Instrumentation coverage**: `ImmersiveModeWindowAdapterInstrumentedTest` applies both states to a real activity window and verifies the insets controller uses transient-by-swipe behavior while hiding and restoring system bars.

### Deep Links (`DeepLinkResolver`)
- **`resolve(url: String?): String?`**: Maps incoming deep link Intents (scheme `chatplayground://` or `https://chatplayground.ai`) to canonical WebView URLs.

### Site Data Management (`WebStorageManager`)
- **`getOrigins(callback)`**: Fetches a map of origins (e.g., `https://chatplayground.ai`) that have stored data (localStorage/IndexedDB) and their usage.
- **`deleteOrigin(origin)`**: Clears all WebStorage for a specific origin.
- **`deleteAllData()`**: Clears WebStorage for all origins (delegated to by `DataClearer`).
- **`showSiteDataManagementDialog(activity)`**: Displays an interactive dialog listing origins with data, allowing per-site or global clearing.
- **Testability**: Uses injectable seams (`originFetcher`, `originDeleter`, `allDataDeleter`) so the UI and logic can be verified under Robolectric where the real `WebStorage` is a stub.

## 4. Code Formatting Enforcement (CI)

### ktlint (`org.jmailen.kotlinter`)
- **Plugin**: `org.jmailen.kotlinter` version `4.3.0`.
- **`./gradlew :app:lintKotlin`**: Verifies Kotlin source formatting for `src/main` and `src/test`.

## 5. WebView Asset Interception (`AssetRequestHandler`)
- **`assetPathFor(url: String): String?`**: Maps `file:///android_asset/<path>` to bundled assets (`offline.html`). Intercepted in `shouldInterceptRequest` to serve bytes directly from the APK.

## 6. Navigation Retry Policy (`NavigationRetryPolicy`)
- **Retry Budget**: Exponential backoff retry (default 3 retries) for main-frame load failures. Reset on successful commit or new Intent. Sub-frame failures never retry.
- **`backoffDelayMs()`**: Returns `baseDelayMs * 2^retryCount` for the next retry, clamped at `maxDelayMs`. Defaults: `baseDelayMs = 1000`, `maxDelayMs = 60000`. (See `NavigationRetryPolicyTest` for executable fixtures and cap assertions).
- **Overflow protection**: The exponent is bounded (`MAX_SAFE_EXPONENT = 62`) so a very large retry count cannot overflow a `Long`; the delay always clamps to the positive `maxDelayMs` cap and never wraps negative.

| retryCount | baseDelayMs 1000 | Computed | Applied delay |
|------------|------------------|----------|---------------|
| 0 | 1000 | 1000 × 2⁰ = 1000 | 1000 ms |
| 1 | 1000 | 1000 × 2¹ = 2000 | 2000 ms |
| 2 | 1000 | 1000 × 2² = 4000 | 4000 ms |
| 3 | 1000 | 1000 × 2³ = 8000 | 8000 ms |
| 4 | 1000 | 1000 × 2⁴ = 16000 | 16000 ms |
| 5 | 1000 | 1000 × 2⁵ = 32000 | 32000 ms |
| 6 | 1000 | 1000 × 2⁶ = 64000 | 60000 ms (capped at maxDelayMs) |
| 7 | 1000 | 1000 × 2⁷ = 128000 | 60000 ms (capped at maxDelayMs) |

## 7. WebView Gesture Navigation (`GestureNavigationHandler`)
- **Swipe Gestures**: Detects horizontal flings for history navigation (Right = Back, Left = Forward).
- **Content Awareness**: `canContentScrollHorizontally()` keeps swipe events for code blocks and carousels inside the WebView instead of navigating history.

## 8. Preference Keys (`PreferenceKeys`)

`PreferenceKeys` is the single source of truth for the SharedPreferences file
and key names used by theme, desktop-mode, immersive-mode, and general app data
settings. Production code uses these constants instead of repeating string
literals, preventing a read/write mismatch when a setting is persisted or
cleared. This is a constants-only utility and has no runtime side effects.

## 9. Test Utility: Singleton Cleanup (`SingletonCleanupRule`)
- **TestWatcher**: Shared JUnit rule that resets `AppLock`, `DesktopModeHandler`, and `SecureStorage` seams after every test to prevent cross-test state contamination.
