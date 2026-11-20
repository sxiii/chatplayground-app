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
  6. **Run Unit Tests:** Executes the JVM unit-test suite via `./gradlew testDebugUnitTest --no-daemon --stacktrace --console=plain`.
  7. **Verify Dependencies:** Runs `./gradlew androidDependencies` to check resolution.
  8. **Generate JaCoCo Report:** Attempts to generate code coverage via `./gradlew jacocoTestReport`.
  9. **Run Lint:** Executes `./gradlew lint` and fails if any warnings are found in `lint-results.html`.
  10. **Build Release APK:** Assembles the (unsigned) release build via `./gradlew assembleRelease --stacktrace --console=plain`.
  11. **Upload Artifacts:** Uploads the compiled APK outputs and JaCoCo execution data using `actions/upload-artifact@v4`.

### `gradle.properties` (build configuration)
- **`android.useAndroidX=true`** — required because the app depends exclusively on AndroidX artifacts while AGP 8.2.0 defaults this flag to `false`. Without it, a clean CI runner fails configuration before any task runs.
- **`org.gradle.jvmargs`** — raises the Gradle daemon heap (`-Xmx2048m`) for Compose + Kotlin builds.
- **`org.gradle.caching=true`** / **`org.gradle.parallel=true`** — enable the local build cache and parallel task execution to speed up CI.

## 2. Local Gradle Wrapper Utility
- **`./gradlew`**: Bootstraps Gradle using version 8.5 specified in `gradle/wrapper/gradle-wrapper.properties`.

## 3. Application Runtime Functions (Documented prior to implementation)

### Offline Support & Caching (`OfflineCacheManager`)
- **`saveCachedResponse(url: String, data: String)`**: Caches response data for offline access in app cache storage.
- **`getCachedResponse(url: String): String?`**: Retrieves cached content when network connection is unavailable.
- **`clearCache()`**: Clears stored offline cache entries.

### Push Notifications (`NotificationService`)
- **`handleTokenRefresh(token: String)`**: Registers and updates Firebase Cloud Messaging (FCM) device tokens.
- **`displayNotification(title: String, body: String)`**: Displays push notifications in the notification tray when received.

### Network Monitoring (`NetworkMonitor`)
- **`isOnline(): Boolean`**: Checks the active network capabilities for an available Internet transport (cellular, Wi-Fi, Ethernet).
- **`observeNetworkState(callback: (Boolean) -> Unit)`**: Registers a `ConnectivityManager.NetworkCallback` to stream online/offline network transitions in real-time.

## 4. WebView UI & Loading (`MainActivity`)
- Loading Indicator: A `ProgressBar` overlay that is visible during page loads.
- `WebViewClient.onPageStarted`: Sets the loading indicator to `VISIBLE`.
- `WebViewClient.onPageFinished`: Sets the loading indicator to `GONE`.
- **Offline Fallback**: Overrides `onReceivedError` to load a local `offline.html` asset when a network error occurs, avoiding the default browser error page.
- **External Browser**: Provides a menu option to open the current WebView URL in the system's default browser via `Intent.ACTION_VIEW`.
- **Safe Link Interception**: Overrides `shouldOverrideUrlLoading` to intercept non-HTTP/HTTPS schemes (e.g., `mailto:`, `tel:`, `sms:`), preventing them from loading in the WebView and instead launching the appropriate external application via an `ACTION_VIEW` Intent. Returns `false` for HTTP/HTTPS to allow normal navigation.
- **Cache Management**: Provides a menu option to clear both the WebView's internal cache and the application's offline response cache.

### Secure Storage (`SecureStorage`)
- **`putString(context, key, value)`**: Encrypts and persists a string value under [key] using EncryptedSharedPreferences (AES-256-GCM master key).
- **`getString(context, key, default)`**: Returns the decrypted value for [key], or [default] when absent.
- **`remove(context, key)`**: Best-effort removal of [key]; no-op if absent.
- **`contains(context, key): Boolean`**: Returns true when [key] is present in secure storage; false otherwise.
- **`clear(context)`**: Wipes all stored values from secure storage; useful for a "Log out / Reset app" feature.

## 4. Code Formatting Enforcement (CI)

### ktlint (`org.jmailen.gradle.kotlinter`)
- **Plugin**: `org.jmailen.gradle.kotlinter` version `4.3.0`, applied to the `app` module.
- **`./gradlew ktlintCheck`**: Verifies Kotlin source formatting for `src/main` and `src/test` source sets (`ignoreTestSourceSet = false`, no experimental rules). Runs as a dedicated CI step after `Run Lint`; failure blocks the pipeline.
- **`./gradlew ktlintFormat`**: Auto-fixes formatting violations locally before pushing.
