# Functions & CI/CD Documentation: ChatPlayground Android Wrapper

This document describes the core build, CI/CD, and application runtime functions for `chatplayground-app`.

## 1. Build Functions (GitHub Actions CI/CD)

### `build-android` Workflow (`.github/workflows/build.yml`)
- **Trigger:** Triggers automatically on every `push` (and `pull_request`) to the main branch, as well as manually via `workflow_dispatch`.
- **Environment:** Runs on `ubuntu-latest`.
- **Steps:**
  1. **Checkout Repository:** Checks out the source code using `actions/checkout@v4`.
  2. **Set up JDK:** Configures JDK 17 (Temurin distribution) and enables the Gradle build-cache using `actions/setup-java@v4` (`cache: 'gradle'`).
  3. **Set up Android SDK:** Provisions the Android command-line tools, license acceptance, and `ANDROID_HOME` / `ANDROID_SDK_ROOT` / PATH via `android-actions/setup-android@v3`, declaring the required components through its `packages` input (`platform-tools`, `platforms;android-34`, `build-tools;34.0.0`) with `cmdline-tools-version: latest`.
  4. **Grant Gradle Wrapper Execution Permission:** Ensures `gradlew` is executable (`chmod +x gradlew`).
  5. **Build Debug APK:** Compiles and assembles the debug build via `./gradlew assembleDebug --stacktrace --console=plain`, teeing the output to `gradle_build_debug.log`.
  6. **Build Debug APK Diagnostics** (on failure only): Writes the captured error markers and last log lines to the job summary and a public file annotation so the real Gradle error is inspectable without admin log access.
  7. **Run Unit Tests:** Executes the JVM unit-test suite via `./gradlew test --no-daemon --stacktrace --console=plain` (with a matching on-failure diagnostics step).
  8. **Build Release APK:** Assembles the (unsigned) release build via `./gradlew assembleRelease --stacktrace --console=plain`.
  9. **Upload Artifacts:** Uploads the compiled APK outputs (`app/build/outputs/apk/`) as build artifacts using `actions/upload-artifact@v4` (runs on success or failure).

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

## 4. Code Formatting Enforcement (CI)

### ktlint (`org.jmailen.gradle.kotlinter`)
- **Plugin**: `org.jmailen.gradle.kotlinter` version `4.3.0`, applied to the `app` module.
- **`./gradlew ktlintCheck`**: Verifies Kotlin source formatting for `src/main` and `src/test` source sets (`ignoreTestSourceSet = false`, no experimental rules). Runs as a dedicated CI step after `Run Lint`; failure blocks the pipeline.
- **`./gradlew ktlintFormat`**: Auto-fixes formatting violations locally before pushing.
