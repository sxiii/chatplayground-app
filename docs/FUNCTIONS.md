# Functions & CI/CD Documentation: ChatPlayground Android Wrapper

This document describes the core build and CI/CD functions for `chatplayground-app` prior to implementation.

## 1. Build Functions (GitHub Actions CI/CD)

### `build-android` Workflow (`.github/workflows/build.yml`)
- **Trigger:** Triggers automatically on every `push` (and `pull_request`) to the main branch, as well as manually via `workflow_dispatch`.
- **Environment:** Runs on `ubuntu-latest`.
- **Steps:**
  1. **Checkout Repository:** Checks out the source code using `actions/checkout@v4`.
  2. **Set up JDK:** Configures JDK 17 (Temurin distribution) using `actions/setup-java@v4`.
  3. **Set up Android SDK:** Configures Android SDK components (Build Tools, platforms for SDK 34) using `android-actions/setup-android@v3`.
  4. **Set up Gradle Cache:** Caches and restores Gradle dependencies and wrapper using `gradle/actions/setup-gradle@v3`.
  5. **Grant Gradle Wrapper Execution Permission:** Ensures `gradlew` is executable (`chmod +x gradlew`).
  6. **Build Debug APK:** Compiles and assembles the debug build via `./gradlew assembleDebug`.
  7. **Build Release APK (Unsigned/Signed):** Assembles the release build via `./gradlew assembleRelease` (or `assembleDebug` / `bundleRelease` as needed).
  8. **Upload Artifacts:** Uploads the compiled APK outputs (`app/build/outputs/apk/`) as build artifacts using `actions/upload-artifact@v4`.

## 2. Local Gradle Wrapper Utility
- **`./gradlew`**: Bootstraps Gradle using version 8.5 specified in `gradle/wrapper/gradle-wrapper.properties`.
