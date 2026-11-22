# chatplayground-app

Native Android wrapper for [chatplayground.ai](https://chatplayground.ai), built with Jetpack Compose and a robust WebView architecture.

## Build Requirements & GitHub-Native CI/CD

This project is built and tested exclusively via **GitHub Actions** (`.github/workflows/build.yml`). Local Java/Gradle execution is not part of the standard workflow; all verification, unit tests (`testDebugUnitTest`), linting, and APK assembly (`assembleDebug`, `assembleRelease`) run on GitHub runners (`ubuntu-latest`).

### Key Artifacts
- **Debug APK**: `app/build/outputs/apk/debug/app-debug.apk`
- **Release APK (unsigned)**: `app/build/outputs/apk/release/app-release-unsigned.apk`
- **JUnit Test Reports**: Collected per suite into `junit-results/`
- **JaCoCo Coverage**: Execution data collected under `app/build/jacoco/`

### Test Environment & Robolectric Pin
- **Robolectric SDK**: Pinned to API 26 (`sdk=26`) in `app/src/test/resources/robolectric.properties`.
- **Consistency Guard**: `ci_check_robolectric_sdk.py` runs in CI to ensure no test source overrides the central SDK version.
