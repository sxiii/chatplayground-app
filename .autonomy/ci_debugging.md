# CI Build & Test Guide

The project is compiled and tested exclusively on GitHub Actions. Local
Gradle/JDK runs are not part of the supported workflow.

## Pipeline

`.github/workflows/build.yml` runs on every push to `main`/`master`, on pull
requests, and via **workflow_dispatch**. The job executes, in order:

1. **Checkout** (`actions/checkout@v4`).
2. **JDK 17** (`actions/setup-java@v5`, Temurin, Gradle cache enabled).
   Kotlin 1.9.0 does not support JVM target 21, so do not bump this to 21
   until Kotlin and AGP are upgraded together.
3. **Android SDK** (`android-actions/setup-android@v3`) with
   `platform-tools`, `platforms;android-34`, `build-tools;34.0.0`.
4. **`local.properties`** written from `$ANDROID_SDK_ROOT`, and
   `ANDROID_HOME`/`ANDROID_SDK_ROOT` exported to `$GITHUB_ENV`.
5. **`./gradlew assembleDebug`** (lifecycle logging, `--stacktrace`), tee'd to
   `gradle_build_debug.log`.
6. **`./gradlew test`** (unit tests, tee'd to `gradle_test.log`).
7. **JUnit XML collection** into `junit-results/`.
8. **`./gradlew lint`**, failing the job when `lint-results.html` contains
   `warning`.
9. **`./gradlew assembleRelease`**, then an explicit assertion that both APKs
   exist, then APK artifact upload.

## Reading a failure without admin log access

GitHub only exposes full job logs to repository admins. This repository
therefore publishes diagnostics into the **job summary** and as
annotations/artifacts, all publicly readable:

- **Build Debug APK Diagnostics** (on failure) appends error markers
  (`FAILURE:`, `error:`, `^e: `, `What went wrong`, `Could not resolve`,
  `unresolved reference`, `cannot find symbol`) and the last 40 log lines to
  `$GITHUB_STEP_SUMMARY`, and raises a `::error file=...` annotation with the
  first marker.
- **Unit Test Diagnostics** (on failure) appends `FAILED` / `> Task ... FAILED`
  markers and the last 40 lines of `gradle_test.log` to the summary.
- **Upload JUnit Test Results** (`if: always()`) copies every
  `app/build/test-results/**/*.xml` into `junit-results/`, so the step log
  lists each XML result file and the per-test failures.
- **Upload APK Artifacts** and **Upload Gradle Debug Log** (`if: always()`)
  preserve build outputs even on red runs.

Start at the run's **Summary** tab, then the failing step's log lines. The
summary lines are enough for compilation errors; JUnit XML names the failing
test and assertion.

## Common failure signatures

| Symptom | Likely cause |
|---|---|
| `unknown Kotlin JVM target: 21` | JDK bumped past Kotlin 1.9 support; use JDK 17. |
| `SDK location not found` | `local.properties` step missing or `ANDROID_SDK_ROOT` unset. |
| `Unresolved reference` in a test | Test source uses an API absent from the pinned AGP/Kotlin/Robolectric versions. |
| `Tests ... FAILED` with assertion text | Behavioural regression; read the JUnit XML artifact. |
| Lint step fails with no Gradle error | `lint-results.html` contains the word `warning`; open the report artifact. |

## Reproducing locally

Only needed for reading Kotlin code, not for validation. Install JDK 17 and
the Android SDK (command-line tools, platform 34, build-tools 34.0.0), set
`ANDROID_SDK_ROOT`, then run `./gradlew test`. Treat CI as the source of
truth: a change is verified only when a green run exists on GitHub.